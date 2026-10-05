package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.model.Cita;
import org.esfe.miagendaclinica.service.CitaService;
import org.esfe.miagendaclinica.service.EspecialidadService;
import org.esfe.miagendaclinica.service.MedicoService;
import org.esfe.miagendaclinica.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private EspecialidadService especialidadService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("citas", citaService.listar());
        return "citas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("cita", new Cita());
        cargarCombos(model);
        return "citas/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cita", citaService.buscarPorId(id).orElse(new Cita()));
        cargarCombos(model);
        return "citas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cita cita, Model model) {
        // Regla: no permitir dos citas del mismo medico en el mismo dia y hora
        if (cita.getMedico() != null
                && citaService.existeChoqueDeHorario(cita.getMedico().getId(), cita.getFecha(), cita.getHora(), cita.getId())) {
            model.addAttribute("error", "Ese médico ya tiene una cita registrada en esa fecha y hora.");
            model.addAttribute("cita", cita);
            cargarCombos(model);
            return "citas/formulario";
        }
        citaService.guardar(cita);
        return "redirect:/citas";
    }

    @GetMapping("/estado/{id}/{estado}")
    public String cambiarEstado(@PathVariable Long id, @PathVariable String estado) {
        if (java.util.List.of("PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA", "NO_ASISTIO").contains(estado)) {
            citaService.cambiarEstado(id, estado);
        }
        return "redirect:/citas";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("pacientes", pacienteService.listarActivos());
        model.addAttribute("medicos", medicoService.listarActivos());
        model.addAttribute("especialidades", especialidadService.listar());
    }
}
