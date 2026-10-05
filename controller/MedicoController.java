package org.esfe.miagendaclinica.controller;

import org.esfe.miagendaclinica.model.Medico;
import org.esfe.miagendaclinica.service.EspecialidadService;
import org.esfe.miagendaclinica.service.HorarioService;
import org.esfe.miagendaclinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private EspecialidadService especialidadService;

    @Autowired
    private HorarioService horarioService;

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("medicos",
                (buscar != null && !buscar.isBlank())
                        ? medicoService.buscar(buscar)
                        : medicoService.listar());
        model.addAttribute("buscar", buscar);
        return "medicos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("medico", new Medico());
        model.addAttribute("especialidades", especialidadService.listar());
        return "medicos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("medico", medicoService.buscarPorId(id).orElse(new Medico()));
        model.addAttribute("especialidades", especialidadService.listar());
        return "medicos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Medico medico) {
        String foto = medico.getFotoUrl() == null ? "" : medico.getFotoUrl().trim();
        boolean urlValida = foto.startsWith("http://") || foto.startsWith("https://") || foto.startsWith("/img/");
        medico.setFotoUrl(urlValida ? foto : null);
        if (medico.getDescripcion() != null && medico.getDescripcion().isBlank()) medico.setDescripcion(null);
        medicoService.guardar(medico);
        return "redirect:/medicos";
    }

    @GetMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id) {
        medicoService.desactivar(id);
        return "redirect:/medicos";
    }

    @GetMapping("/horarios/{id}")
    public String verHorarios(@PathVariable Long id, Model model) {
        model.addAttribute("medico", medicoService.buscarPorId(id).orElse(new Medico()));
        model.addAttribute("horarios", horarioService.listarPorMedico(id));
        return "horarios/lista";
    }
}
