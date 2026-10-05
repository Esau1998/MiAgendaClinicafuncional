package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.service.CitaService;
import org.esfe.miagendaclinica.service.MedicoService;
import org.esfe.miagendaclinica.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private CitaService citaService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalPacientes", pacienteService.contarActivos());
        model.addAttribute("totalMedicos", medicoService.contarActivos());
        model.addAttribute("citasHoy", citaService.contarCitasDeHoy());
        model.addAttribute("citasPendientes", citaService.contarPendientes());
        model.addAttribute("proximasCitas", citaService.proximas());
        return "dashboard";
    }
}
