package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Cita;
import org.esfe.miagendaclinica.model.Medico;
import org.esfe.miagendaclinica.model.Paciente;
import org.esfe.miagendaclinica.repository.CitaRepository;
import org.esfe.miagendaclinica.repository.MedicoRepository;
import org.esfe.miagendaclinica.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Registra las citas solicitadas desde el catalogo publico: crea (o reutiliza)
 * al paciente y deja la cita en estado PENDIENTE para que la clinica la confirme.
 */
@Service
public class SolicitudCitaService {

    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private DisponibilidadService disponibilidadService;

    /** Lanza IllegalArgumentException con un mensaje listo para mostrar al visitante. */
    @Transactional
    public synchronized Cita solicitar(Long medicoId, String nombre, String apellido, String telefono,
                                       String correo, LocalDate fecha, LocalTime hora, String motivo) {

        Medico medico = medicoRepository.findById(medicoId == null ? -1L : medicoId)
                .filter(m -> "ACTIVO".equals(m.getEstado()))
                .orElseThrow(() -> new IllegalArgumentException("El médico seleccionado ya no está disponible."));

        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("Escribe tu nombre y tu apellido.");
        }
        if (nombre.trim().length() > 80 || apellido.trim().length() > 80) {
            throw new IllegalArgumentException("El nombre y el apellido no pueden superar 80 caracteres.");
        }
        if (telefono == null || !TELEFONO.matcher(telefono.trim()).matches()) {
            throw new IllegalArgumentException("Escribe un teléfono válido (solo números, de 7 a 20 dígitos).");
        }
        String correoLimpio = (correo == null || correo.isBlank()) ? null : correo.trim().toLowerCase();
        if (correoLimpio != null && (correoLimpio.length() > 100 || !EMAIL.matcher(correoLimpio).matches())) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (motivo != null && motivo.trim().length() > 255) {
            throw new IllegalArgumentException("El motivo no puede superar 255 caracteres.");
        }

        String error = disponibilidadService.validar(medico.getId(), fecha, hora);
        if (error != null) throw new IllegalArgumentException(error);

        Paciente paciente = null;
        if (correoLimpio != null) {
            Optional<Paciente> existente = pacienteRepository.findFirstByCorreoIgnoreCase(correoLimpio);
            if (existente.isPresent()) paciente = existente.get();
        }
        if (paciente == null) {
            paciente = new Paciente();
            paciente.setNombre(nombre.trim());
            paciente.setApellido(apellido.trim());
            paciente.setTelefono(telefono.trim());
            paciente.setCorreo(correoLimpio);
            paciente.setEstado("ACTIVO");
            paciente = pacienteRepository.save(paciente);
        }

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setEspecialidad(medico.getEspecialidad());
        cita.setFecha(fecha);
        cita.setHora(hora.withSecond(0).withNano(0));
        cita.setMotivo(motivo == null || motivo.isBlank() ? null : motivo.trim());
        cita.setEstado("PENDIENTE");
        cita.setObservaciones("Solicitada desde el catálogo web. Teléfono de contacto: " + telefono.trim());
        return citaRepository.save(cita);
    }
}
