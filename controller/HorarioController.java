package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.model.Horario;
import org.esfe.miagendaclinica.service.HorarioService;
import org.esfe.miagendaclinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/horarios")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private MedicoService medicoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("horarios", horarioService.listar());
        return "horarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("horario", new Horario());
        model.addAttribute("medicos", medicoService.listarActivos());
        return "horarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("horario", horarioService.buscarPorId(id).orElse(new Horario()));
        model.addAttribute("medicos", medicoService.listarActivos());
        return "horarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Horario horario) {
        horarioService.guardar(horario);
        return "redirect:/horarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        horarioService.eliminar(id);
        return "redirect:/horarios";
    }
}
