package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("citas", reporteService.reporteCitas());
        model.addAttribute("porMedico", reporteService.reportePorMedico());
        model.addAttribute("porEspecialidad", reporteService.reportePorEspecialidad());
        model.addAttribute("pagos", reporteService.reportePagos());
        return "reportes/index";
    }
}
