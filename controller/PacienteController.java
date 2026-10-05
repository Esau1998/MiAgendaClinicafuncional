package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.model.Paciente;
import org.esfe.miagendaclinica.service.CitaService;
import org.esfe.miagendaclinica.service.HistorialClinicoService;
import org.esfe.miagendaclinica.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private CitaService citaService;

    @Autowired
    private HistorialClinicoService historialClinicoService;

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("pacientes",
                (buscar != null && !buscar.isBlank())
                        ? pacienteService.buscar(buscar)
                        : pacienteService.listar());
        model.addAttribute("buscar", buscar);
        return "pacientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("paciente", new Paciente());
        return "pacientes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", pacienteService.buscarPorId(id).orElse(new Paciente()));
        return "pacientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Paciente paciente) {
        pacienteService.guardar(paciente);
        return "redirect:/pacientes";
    }

    @GetMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id) {
        pacienteService.desactivar(id);
        return "redirect:/pacientes";
    }

    @GetMapping("/historial/{id}")
    public String verHistorial(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", pacienteService.buscarPorId(id).orElse(new Paciente()));
        model.addAttribute("historiales", historialClinicoService.listarPorPaciente(id));
        return "historial/lista";
    }

    @GetMapping("/citas/{id}")
    public String verCitas(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", pacienteService.buscarPorId(id).orElse(new Paciente()));
        model.addAttribute("citas", citaService.listarPorPaciente(id));
        return "citas/lista";
    }
}
