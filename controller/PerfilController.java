package org.esfe.miagendaclinica.controller;

import jakarta.servlet.http.HttpSession;
import org.esfe.miagendaclinica.model.Usuario;
import org.esfe.miagendaclinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/** Pagina "Mi perfil": datos personales y cambio de contrasena del usuario con sesion iniciada. */
@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    private Optional<Usuario> usuarioActual(HttpSession session) {
        Object id = session.getAttribute("usuarioId");
        if (id == null) return Optional.empty();
        return usuarioService.buscarPorId((Long) id);
    }

    @GetMapping
    public String ver(HttpSession session, Model model) {
        Optional<Usuario> opt = usuarioActual(session);
        if (opt.isEmpty()) return "redirect:/logout";
        model.addAttribute("perfil", opt.get());
        return "perfil";
    }

    @PostMapping("/datos")
    public String guardarDatos(@RequestParam String nombreCompleto,
                               @RequestParam(required = false) String email,
                               HttpSession session,
                               RedirectAttributes ra) {
        Optional<Usuario> opt = usuarioActual(session);
        if (opt.isEmpty()) return "redirect:/logout";
        Usuario u = opt.get();

        String error = usuarioService.validarDatos(u.getId(), nombreCompleto, u.getUsername(), email, false);
        if (error != null) {
            ra.addFlashAttribute("errorDatos", error);
            return "redirect:/perfil";
        }
        u.setNombreCompleto(nombreCompleto.trim());
        u.setEmail(usuarioService.normalizarEmail(email));
        usuarioService.guardar(u);
        session.setAttribute("usuarioNombre", u.getNombreCompleto());
        ra.addFlashAttribute("exitoDatos", "Tus datos fueron actualizados.");
        return "redirect:/perfil";
    }

    @PostMapping("/password")
    public String cambiarPassword(@RequestParam String actual,
                                  @RequestParam String password,
                                  @RequestParam String confirmar,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        Optional<Usuario> opt = usuarioActual(session);
        if (opt.isEmpty()) return "redirect:/logout";
        Usuario u = opt.get();

        if (!usuarioService.coincidePassword(u, actual)) {
            ra.addFlashAttribute("errorPassword", "La contraseña actual no es correcta.");
            return "redirect:/perfil";
        }
        String error = usuarioService.validarPassword(password, confirmar);
        if (error != null) {
            ra.addFlashAttribute("errorPassword", error);
            return "redirect:/perfil";
        }
        usuarioService.cambiarPassword(u, password);
        ra.addFlashAttribute("exitoPassword", "Tu contraseña fue cambiada correctamente.");
        return "redirect:/perfil";
    }
}
