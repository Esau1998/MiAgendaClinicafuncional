package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.model.Especialidad;
import org.esfe.miagendaclinica.service.EspecialidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/especialidades")
public class EspecialidadController {

    @Autowired
    private EspecialidadService especialidadService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("especialidades", especialidadService.listar());
        return "especialidades/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("especialidad", new Especialidad());
        return "especialidades/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("especialidad", especialidadService.buscarPorId(id).orElse(new Especialidad()));
        return "especialidades/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Especialidad especialidad) {
        especialidadService.guardar(especialidad);
        return "redirect:/especialidades";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        especialidadService.eliminar(id);
        return "redirect:/especialidades";
    }
}
