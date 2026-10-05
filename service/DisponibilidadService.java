package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Cita;
import org.esfe.miagendaclinica.model.Horario;
import org.esfe.miagendaclinica.repository.CitaRepository;
import org.esfe.miagendaclinica.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Calcula las horas libres de un medico en una fecha, a partir de sus
 * horarios activos y de las citas ya registradas (las canceladas no cuentan).
 */
@Service
public class DisponibilidadService {

    public static final int DURACION_MINUTOS = 30;
    public static final int DIAS_MAXIMOS = 90;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    public static String nombreDia(LocalDate fecha) {
        switch (fecha.getDayOfWeek()) {
            case MONDAY: return "LUNES";
            case TUESDAY: return "MARTES";
            case WEDNESDAY: return "MIERCOLES";
            case THURSDAY: return "JUEVES";
            case FRIDAY: return "VIERNES";
            case SATURDAY: return "SABADO";
            default: return "DOMINGO";
        }
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase();
    }

    public List<LocalTime> horasLibres(Long medicoId, LocalDate fecha) {
        String dia = nombreDia(fecha);

        List<Horario> horarios = horarioRepository.findByMedicoId(medicoId).stream()
                .filter(h -> "ACTIVO".equalsIgnoreCase(h.getEstado()))
                .filter(h -> normalizar(h.getDia()).equals(dia))
                .toList();

        Set<LocalTime> ocupadas = new HashSet<>();
        for (Cita c : citaRepository.findByMedicoIdAndFecha(medicoId, fecha)) {
            if (!"CANCELADA".equals(c.getEstado()) && c.getHora() != null) {
                ocupadas.add(c.getHora().withSecond(0).withNano(0));
            }
        }

        boolean esHoy = fecha.equals(LocalDate.now());
        LocalTime ahora = LocalTime.now();
        TreeSet<LocalTime> libres = new TreeSet<>();

        for (Horario h : horarios) {
            LocalTime t = h.getHoraInicio().withSecond(0).withNano(0);
            LocalTime fin = h.getHoraFin();
            while (true) {
                LocalTime siguiente = t.plusMinutes(DURACION_MINUTOS);
                if (siguiente.isBefore(t) || siguiente.isAfter(fin)) break;
                boolean pasada = esHoy && !t.isAfter(ahora);
                if (!ocupadas.contains(t) && !pasada) libres.add(t);
                if (siguiente.equals(fin)) break;
                t = siguiente;
            }
        }
        return List.copyOf(libres);
    }

    /** Devuelve el mensaje de error, o null si la fecha y hora se pueden reservar. */
    public String validar(Long medicoId, LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) return "Selecciona la fecha y la hora de la cita.";
        if (fecha.isBefore(LocalDate.now())) return "La fecha no puede ser anterior a hoy.";
        if (fecha.isAfter(LocalDate.now().plusDays(DIAS_MAXIMOS))) {
            return "Solo se pueden agendar citas dentro de los próximos " + DIAS_MAXIMOS + " días.";
        }
        LocalTime limpia = hora.withSecond(0).withNano(0);
        if (!horasLibres(medicoId, fecha).contains(limpia)) {
            return "Ese horario ya no está disponible. Elige otra hora.";
        }
        return null;
    }
}
