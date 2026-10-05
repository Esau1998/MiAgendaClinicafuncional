package org.esfe.miagendaclinica.controller;

import jakarta.servlet.http.HttpSession;
import org.esfe.miagendaclinica.dto.UsuarioForm;
import org.esfe.miagendaclinica.model.Medico;
import org.esfe.miagendaclinica.model.Rol;
import org.esfe.miagendaclinica.model.Usuario;
import org.esfe.miagendaclinica.service.MedicoService;
import org.esfe.miagendaclinica.service.RolService;
import org.esfe.miagendaclinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

/** Gestion de usuarios (solo ADMINISTRADOR; lo exige SesionFiltro). */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Autowired
    private MedicoService medicoService;

    private void cargarCombos(Model model) {
        model.addAttribute("roles", rolService.listar());
        model.addAttribute("medicos", medicoService.listar());
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new UsuarioForm());
        cargarCombos(model);
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Usuario> opt = usuarioService.buscarPorId(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/usuarios";
        }
        Usuario u = opt.get();
        UsuarioForm form = new UsuarioForm();
        form.setId(u.getId());
        form.setNombreCompleto(u.getNombreCompleto());
        form.setUsername(u.getUsername());
        form.setEmail(u.getEmail());
        form.setRolId(u.getRol() != null ? u.getRol().getId() : null);
        form.setMedicoId(u.getMedico() != null ? u.getMedico().getId() : null);
        form.setActivo(Boolean.TRUE.equals(u.getActivo()));
        model.addAttribute("form", form);
        cargarCombos(model);
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("form") UsuarioForm form, Model model, RedirectAttributes ra) {
        boolean esNuevo = form.getId() == null;
        Usuario existente = null;
        if (!esNuevo) {
            existente = usuarioService.buscarPorId(form.getId()).orElse(null);
            if (existente == null) {
                ra.addFlashAttribute("error", "El usuario no existe.");
                return "redirect:/usuarios";
            }
        }

        String error = usuarioService.validarDatos(form.getId(), form.getNombreCompleto(),
                form.getUsername(), form.getEmail(), false);

        Rol rol = null;
        if (error == null) {
            rol = form.getRolId() == null ? null : rolService.listar().stream()
                    .filter(r -> r.getId().equals(form.getRolId())).findFirst().orElse(null);
            if (rol == null) error = "Selecciona un rol válido.";
        }

        boolean hayPassword = form.getPassword() != null && !form.getPassword().isEmpty();
        if (error == null && (esNuevo || hayPassword)) {
            error = usuarioService.validarPassword(form.getPassword(), null);
        }

        Medico medico = null;
        if (error == null && form.getMedicoId() != null) {
            medico = medicoService.buscarPorId(form.getMedicoId()).orElse(null);
            if (medico == null) {
                error = "El médico seleccionado no existe.";
            } else if (!"MEDICO".equals(rol.getNombre())) {
                error = "Solo los usuarios con rol MEDICO pueden asociarse a un médico.";
            } else {
                Optional<Usuario> otro = usuarioService.buscarPorMedico(medico.getId());
                if (otro.isPresent() && !otro.get().getId().equals(form.getId())) {
                    error = "Ese médico ya está asociado al usuario \"" + otro.get().getUsername() + "\".";
                }
            }
        }

        if (error == null && existente != null) {
            boolean eraAdminActivo = "ADMINISTRADOR".equals(existente.getRol().getNombre())
                    && Boolean.TRUE.equals(existente.getActivo());
            boolean seguiraAdminActivo = "ADMINISTRADOR".equals(rol.getNombre()) && form.isActivo();
            if (eraAdminActivo && !seguiraAdminActivo && usuarioService.contarAdministradoresActivos() <= 1) {
                error = "Debe existir al menos un administrador activo.";
            }
        }

        if (error != null) {
            model.addAttribute("error", error);
            cargarCombos(model);
            return "usuarios/formulario";
        }

        Usuario u = esNuevo ? new Usuario() : existente;
        u.setNombreCompleto(form.getNombreCompleto().trim());
        u.setUsername(form.getUsername().trim());
        u.setEmail(usuarioService.normalizarEmail(form.getEmail()));
        u.setRol(rol);
        u.setMedico(medico);
        u.setActivo(form.isActivo());
        if (esNuevo) {
            u.setFechaCreacion(LocalDateTime.now());
            u.setPassword(usuarioService.cifrar(form.getPassword()));
        } else if (hayPassword) {
            u.setPassword(usuarioService.cifrar(form.getPassword()));
        }
        usuarioService.guardar(u);

        ra.addFlashAttribute("exito", esNuevo
                ? "Usuario \"" + u.getUsername() + "\" creado correctamente."
                : "Usuario \"" + u.getUsername() + "\" actualizado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Optional<Usuario> opt = usuarioService.buscarPorId(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/usuarios";
        }
        Usuario u = opt.get();
        boolean activo = Boolean.TRUE.equals(u.getActivo());

        if (activo && id.equals(session.getAttribute("usuarioId"))) {
            ra.addFlashAttribute("error", "No puedes desactivar tu propia cuenta.");
            return "redirect:/usuarios";
        }
        if (activo && "ADMINISTRADOR".equals(u.getRol().getNombre()) && usuarioService.contarAdministradoresActivos() <= 1) {
            ra.addFlashAttribute("error", "Debe existir al menos un administrador activo.");
            return "redirect:/usuarios";
        }
        u.setActivo(!activo);
        usuarioService.guardar(u);
        ra.addFlashAttribute("exito", "Usuario \"" + u.getUsername() + "\" " + (activo ? "desactivado." : "activado."));
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Optional<Usuario> opt = usuarioService.buscarPorId(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/usuarios";
        }
        Usuario u = opt.get();

        if (id.equals(session.getAttribute("usuarioId"))) {
            ra.addFlashAttribute("error", "No puedes eliminar tu propia cuenta.");
            return "redirect:/usuarios";
        }
        if ("ADMINISTRADOR".equals(u.getRol().getNombre()) && Boolean.TRUE.equals(u.getActivo())
                && usuarioService.contarAdministradoresActivos() <= 1) {
            ra.addFlashAttribute("error", "Debe existir al menos un administrador activo.");
            return "redirect:/usuarios";
        }
        usuarioService.eliminar(id);
        ra.addFlashAttribute("exito", "Usuario \"" + u.getUsername() + "\" eliminado.");
        return "redirect:/usuarios";
    }
}
