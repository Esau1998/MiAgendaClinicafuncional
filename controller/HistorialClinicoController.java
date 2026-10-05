package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.model.HistorialClinico;
import org.esfe.miagendaclinica.service.HistorialClinicoService;
import org.esfe.miagendaclinica.service.MedicoService;
import org.esfe.miagendaclinica.service.PacienteService;
import org.esfe.miagendaclinica.service.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/historial")
public class HistorialClinicoController {

    @Autowired
    private HistorialClinicoService historialClinicoService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private TratamientoService tratamientoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("historiales", historialClinicoService.listar());
        return "historial/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long pacienteId, Model model) {
        HistorialClinico historial = new HistorialClinico();
        if (pacienteId != null) {
            pacienteService.buscarPorId(pacienteId).ifPresent(historial::setPaciente);
        }
        model.addAttribute("historial", historial);
        model.addAttribute("pacientes", pacienteService.listarActivos());
        model.addAttribute("medicos", medicoService.listarActivos());
        return "historial/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute HistorialClinico historial) {
        historialClinicoService.guardar(historial);
        return "redirect:/pacientes/historial/" + historial.getPaciente().getId();
    }

    @GetMapping("/tratamientos/{id}")
    public String verTratamientos(@PathVariable Long id, Model model) {
        model.addAttribute("historial", historialClinicoService.buscarPorId(id).orElse(new HistorialClinico()));
        model.addAttribute("tratamientos", tratamientoService.listarPorHistorial(id));
        return "tratamientos/lista";
    }
}
