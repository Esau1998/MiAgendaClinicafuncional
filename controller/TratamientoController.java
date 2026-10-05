package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.model.Tratamiento;
import org.esfe.miagendaclinica.service.HistorialClinicoService;
import org.esfe.miagendaclinica.service.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tratamientos")
public class TratamientoController {

    @Autowired
    private TratamientoService tratamientoService;

    @Autowired
    private HistorialClinicoService historialClinicoService;

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam Long historialId, Model model) {
        Tratamiento tratamiento = new Tratamiento();
        historialClinicoService.buscarPorId(historialId).ifPresent(tratamiento::setHistorialClinico);
        model.addAttribute("tratamiento", tratamiento);
        return "tratamientos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Tratamiento tratamiento) {
        tratamientoService.guardar(tratamiento);
        return "redirect:/historial/tratamientos/" + tratamiento.getHistorialClinico().getId();
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, @RequestParam Long historialId) {
        tratamientoService.eliminar(id);
        return "redirect:/historial/tratamientos/" + historialId;
    }
}
