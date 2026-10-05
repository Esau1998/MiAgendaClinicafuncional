package org.esfe.miagendaclinica.controller;

import jakarta.servlet.http.HttpSession;
import org.esfe.miagendaclinica.dto.MedicoCatalogo;
import org.esfe.miagendaclinica.model.Cita;
import org.esfe.miagendaclinica.model.Especialidad;
import org.esfe.miagendaclinica.model.Horario;
import org.esfe.miagendaclinica.model.Medico;
import org.esfe.miagendaclinica.service.DisponibilidadService;
import org.esfe.miagendaclinica.service.EspecialidadService;
import org.esfe.miagendaclinica.service.HorarioService;
import org.esfe.miagendaclinica.service.MedicoService;
import org.esfe.miagendaclinica.service.SolicitudCitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Catalogo publico: muestra especialidades y medicos activos, permite
 * filtrar y solicitar una cita sin necesidad de iniciar sesion.
 */
@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    private static final List<String> ORDEN_DIAS =
            List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");
    private static final Map<String, String> NOMBRE_DIA = Map.of(
            "LUNES", "Lun", "MARTES", "Mar", "MIERCOLES", "Mié", "JUEVES", "Jue",
            "VIERNES", "Vie", "SABADO", "Sáb", "DOMINGO", "Dom");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private EspecialidadService especialidadService;

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private SolicitudCitaService solicitudCitaService;

    @Value("${app.registro.habilitado:true}")
    private boolean registroHabilitado;

    @GetMapping
    public String catalogo(HttpSession session, Model model) {
        List<MedicoCatalogo> tarjetas = new ArrayList<>();
        Map<Long, Integer> conteo = new HashMap<>();

        for (Medico m : medicoService.listarActivos()) {
            List<Horario> activos = horarioService.listarPorMedico(m.getId()).stream()
                    .filter(h -> "ACTIVO".equalsIgnoreCase(h.getEstado()))
                    .sorted(Comparator.comparingInt((Horario h) -> ORDEN_DIAS.indexOf(normalizar(h.getDia())))
                            .thenComparing(Horario::getHoraInicio))
                    .toList();

            List<String> horarios = new ArrayList<>();
            for (Horario h : activos) {
                String dia = NOMBRE_DIA.getOrDefault(normalizar(h.getDia()), h.getDia());
                horarios.add(dia + " " + h.getHoraInicio().format(HORA) + " - " + h.getHoraFin().format(HORA));
            }

            String nombre = m.getNombre().trim() + " " + m.getApellido().trim();
            String iniciales = ("" + inicial(m.getNombre()) + inicial(m.getApellido())).toUpperCase();
            String foto = m.getFotoUrl() == null || m.getFotoUrl().isBlank() ? null : m.getFotoUrl().trim();
            Especialidad esp = m.getEspecialidad();

            tarjetas.add(new MedicoCatalogo(m.getId(), nombre, esp.getId(), esp.getNombre(),
                    m.getDescripcion(), foto, iniciales, horarios,
                    normalizar(nombre + " " + esp.getNombre()).toLowerCase()));
            conteo.merge(esp.getId(), 1, Integer::sum);
        }

        List<Especialidad> especialidades = especialidadService.listar().stream()
                .filter(e -> conteo.containsKey(e.getId()))
                .sorted(Comparator.comparing(Especialidad::getNombre))
                .toList();

        model.addAttribute("medicos", tarjetas);
        model.addAttribute("especialidades", especialidades);
        model.addAttribute("conteo", conteo);
        model.addAttribute("logueado", session.getAttribute("usuarioId") != null);
        model.addAttribute("registroHabilitado", registroHabilitado);
        model.addAttribute("minFecha", LocalDate.now().toString());
        model.addAttribute("maxFecha", LocalDate.now().plusDays(DisponibilidadService.DIAS_MAXIMOS).toString());
        return "catalogo";
    }

    /** Devuelve las horas libres de un medico en una fecha (lo usa el formulario de solicitud). */
    @GetMapping("/api/disponibilidad")
    @ResponseBody
    public Map<String, Object> disponibilidad(@RequestParam Long medicoId, @RequestParam String fecha) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        List<String> horas = new ArrayList<>();
        try {
            LocalDate f = LocalDate.parse(fecha);
            Optional<Medico> medico = medicoService.buscarPorId(medicoId)
                    .filter(m -> "ACTIVO".equals(m.getEstado()));
            if (medico.isEmpty()) {
                respuesta.put("mensaje", "El médico seleccionado ya no está disponible.");
            } else if (f.isBefore(LocalDate.now())) {
                respuesta.put("mensaje", "La fecha no puede ser anterior a hoy.");
            } else if (f.isAfter(LocalDate.now().plusDays(DisponibilidadService.DIAS_MAXIMOS))) {
                respuesta.put("mensaje", "Solo se puede agendar dentro de los próximos "
                        + DisponibilidadService.DIAS_MAXIMOS + " días.");
            } else {
                for (LocalTime t : disponibilidadService.horasLibres(medicoId, f)) horas.add(t.format(HORA));
                if (horas.isEmpty()) respuesta.put("mensaje", "No hay horas libres ese día. Prueba con otra fecha.");
            }
        } catch (DateTimeParseException e) {
            respuesta.put("mensaje", "Fecha no válida.");
        }
        respuesta.put("horas", horas);
        return respuesta;
    }

    @PostMapping("/solicitar")
    public String solicitar(@RequestParam Long medicoId,
                            @RequestParam String nombre,
                            @RequestParam String apellido,
                            @RequestParam String telefono,
                            @RequestParam(required = false) String correo,
                            @RequestParam String fecha,
                            @RequestParam String hora,
                            @RequestParam(required = false) String motivo,
                            RedirectAttributes ra) {
        try {
            LocalDate f = LocalDate.parse(fecha);
            LocalTime h = LocalTime.parse(hora);
            Cita cita = solicitudCitaService.solicitar(medicoId, nombre, apellido, telefono, correo, f, h, motivo);
            ra.addFlashAttribute("exito", "¡Listo, " + nombre.trim() + "! Tu cita con Dr(a). "
                    + cita.getMedico().getNombre() + " " + cita.getMedico().getApellido()
                    + " quedó registrada para el " + cita.getFecha().format(FECHA_LARGA)
                    + " a las " + cita.getHora().format(HORA)
                    + ". Estado: PENDIENTE; la clínica la confirmará contigo.");
        } catch (DateTimeParseException e) {
            ra.addFlashAttribute("error", "La fecha o la hora no son válidas.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo#especialistas";
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim().toUpperCase();
    }

    private static String inicial(String texto) {
        return texto == null || texto.isBlank() ? "" : texto.trim().substring(0, 1);
    }
}
