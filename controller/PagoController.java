package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.model.Pago;
import org.esfe.miagendaclinica.service.CitaService;
import org.esfe.miagendaclinica.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private CitaService citaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pagos", pagoService.listar());
        return "pagos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("pago", new Pago());
        model.addAttribute("citas", citaService.listar());
        return "pagos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Pago pago) {
        pagoService.guardar(pago);
        return "redirect:/pagos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return "redirect:/pagos";
    }
}
