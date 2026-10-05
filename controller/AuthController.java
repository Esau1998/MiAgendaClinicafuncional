package org.esfe.miagendaclinica.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.esfe.miagendaclinica.model.PasswordResetToken;
import org.esfe.miagendaclinica.model.Rol;
import org.esfe.miagendaclinica.model.Usuario;
import org.esfe.miagendaclinica.service.PasswordResetService;
import org.esfe.miagendaclinica.service.RolService;
import org.esfe.miagendaclinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Inicio de sesion, cierre de sesion, creacion de cuenta y recuperacion de contrasena.
 */
@Controller
public class AuthController {

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MS = 5 * 60 * 1000L;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Autowired
    private PasswordResetService resetService;

    @Value("${app.registro.habilitado:true}")
    private boolean registroHabilitado;

    @Value("${app.registro.activar-automaticamente:true}")
    private boolean activarAutomaticamente;

    @Value("${app.recuperacion.mostrar-enlace:true}")
    private boolean mostrarEnlace;

    // identificador -> [intentos fallidos, bloqueado hasta (ms)]
    private final Map<String, long[]> intentos = new ConcurrentHashMap<>();

    private boolean hayCuenta(HttpSession session) {
        return session != null && session.getAttribute("usuarioId") != null;
    }

    // ------------------------------------------------------------------ inicio / login

    @GetMapping("/")
    public String raiz(HttpSession session) {
        return hayCuenta(session) ? "redirect:/dashboard" : "redirect:/catalogo";
    }

    @GetMapping("/login")
    public String mostrarLogin(HttpSession session, Model model) {
        if (hayCuenta(session)) return "redirect:/dashboard";
        model.addAttribute("registroHabilitado", registroHabilitado);
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String username,
                                @RequestParam String password,
                                HttpServletRequest request,
                                Model model) {
        model.addAttribute("registroHabilitado", registroHabilitado);
        String clave = username == null ? "" : username.trim().toLowerCase();

        long[] estado = intentos.get(clave);
        if (estado != null && estado[1] > System.currentTimeMillis()) {
            long minutos = Math.max(1, (estado[1] - System.currentTimeMillis()) / 60000 + 1);
            model.addAttribute("error", "Demasiados intentos fallidos. Intenta de nuevo en " + minutos + " minuto(s).");
            return "login";
        }

        Optional<Usuario> opt = usuarioService.autenticar(username, password);
        if (opt.isEmpty()) {
            long[] e = intentos.computeIfAbsent(clave, k -> new long[]{0, 0});
            e[0]++;
            if (e[0] >= MAX_INTENTOS) {
                e[0] = 0;
                e[1] = System.currentTimeMillis() + BLOQUEO_MS;
            }
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            return "login";
        }

        Usuario usuario = opt.get();
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            model.addAttribute("error", "Tu cuenta está inactiva. Pide a un administrador que la active.");
            return "login";
        }
        intentos.remove(clave);

        // Sesion nueva en cada inicio: permite cambiar de cuenta sin conflictos
        HttpSession vieja = request.getSession(false);
        String destino = null;
        if (vieja != null) {
            destino = (String) vieja.getAttribute("destino");
            vieja.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNombre", usuario.getNombreCompleto());
        session.setAttribute("usuarioUsername", usuario.getUsername());
        session.setAttribute("rol", usuario.getRol().getNombre());

        if (destino != null && destino.startsWith("/") && !destino.startsWith("//")
                && !destino.startsWith("/login") && !destino.startsWith("/logout")) {
            return "redirect:" + destino;
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) session.invalidate();
        return "redirect:/login?salio";
    }

    // ------------------------------------------------------------------ crear cuenta

    @GetMapping("/registro")
    public String mostrarRegistro(HttpSession session, Model model, RedirectAttributes ra) {
        if (hayCuenta(session)) return "redirect:/dashboard";
        if (!registroHabilitado) {
            ra.addFlashAttribute("error", "El registro de cuentas está deshabilitado. Contacta a un administrador.");
            return "redirect:/login";
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String nombreCompleto,
                                   @RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String confirmar,
                                   Model model,
                                   RedirectAttributes ra) {
        if (!registroHabilitado) return "redirect:/login";

        model.addAttribute("nombreCompleto", nombreCompleto);
        model.addAttribute("username", username);
        model.addAttribute("email", email);

        String error = usuarioService.validarDatos(null, nombreCompleto, username, email, true);
        if (error == null) error = usuarioService.validarPassword(password, confirmar);

        Optional<Rol> rol = rolService.buscarPorNombre("RECEPCIONISTA");
        if (error == null && rol.isEmpty()) error = "No se encontró el rol base. Reinicia la aplicación e inténtalo de nuevo.";

        if (error != null) {
            model.addAttribute("error", error);
            return "registro";
        }

        Usuario u = new Usuario();
        u.setNombreCompleto(nombreCompleto.trim());
        u.setUsername(username.trim());
        u.setEmail(usuarioService.normalizarEmail(email));
        u.setPassword(usuarioService.cifrar(password));
        u.setRol(rol.get());
        u.setActivo(activarAutomaticamente);
        u.setFechaCreacion(LocalDateTime.now());
        usuarioService.guardar(u);

        if (activarAutomaticamente) {
            ra.addFlashAttribute("exito", "¡Cuenta creada! Ya puedes iniciar sesión con tu usuario y contraseña.");
        } else {
            ra.addFlashAttribute("exito", "Cuenta creada. Un administrador debe activarla antes de que puedas ingresar.");
        }
        return "redirect:/login";
    }

    // ------------------------------------------------------------------ olvide mi contrasena

    @GetMapping("/olvide-password")
    public String mostrarOlvide(HttpSession session) {
        if (hayCuenta(session)) return "redirect:/perfil";
        return "olvide-password";
    }

    @PostMapping("/olvide-password")
    public String procesarOlvide(@RequestParam String identificador, Model model) {
        PasswordResetService.Resultado r = resetService.solicitar(identificador);

        model.addAttribute("mensaje",
                "Si la cuenta existe, te enviamos las instrucciones para restablecer tu contraseña. Revisa tu correo (también la carpeta de spam).");

        if (r.usuarioEncontrado() && !r.correoEnviado() && mostrarEnlace) {
            model.addAttribute("enlace", r.enlace());
        }
        return "olvide-password";
    }

    @GetMapping("/restablecer-password")
    public String mostrarRestablecer(@RequestParam(required = false) String token, Model model) {
        boolean valido = resetService.validar(token).isPresent();
        model.addAttribute("tokenValido", valido);
        model.addAttribute("token", token);
        return "restablecer-password";
    }

    @PostMapping("/restablecer-password")
    public String procesarRestablecer(@RequestParam String token,
                                      @RequestParam String password,
                                      @RequestParam String confirmar,
                                      Model model,
                                      RedirectAttributes ra) {
        Optional<PasswordResetToken> opt = resetService.validar(token);
        if (opt.isEmpty()) {
            model.addAttribute("tokenValido", false);
            model.addAttribute("token", token);
            return "restablecer-password";
        }

        String error = usuarioService.validarPassword(password, confirmar);
        if (error != null) {
            model.addAttribute("tokenValido", true);
            model.addAttribute("token", token);
            model.addAttribute("error", error);
            return "restablecer-password";
        }

        resetService.restablecer(opt.get(), password);
        ra.addFlashAttribute("exito", "Tu contraseña fue actualizada. Ya puedes iniciar sesión con la nueva.");
        return "redirect:/login";
    }
}
