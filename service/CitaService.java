package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Cita;
import org.esfe.miagendaclinica.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    public List<Cita> listar() {
        return citaRepository.findAll();
    }

    public List<Cita> listarPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }

    public List<Cita> listarPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    public List<Cita> listarPorMedico(Long medicoId) {
        return citaRepository.findByMedicoId(medicoId);
    }

    public Optional<Cita> buscarPorId(Long id) {
        return citaRepository.findById(id);
    }

    /**
     * Regla de negocio: un medico no puede tener dos citas exactamente
     * en el mismo dia y hora.
     */
    public boolean existeChoqueDeHorario(Long medicoId, LocalDate fecha, LocalTime hora) {
        return existeChoqueDeHorario(medicoId, fecha, hora, null);
    }

    /**
     * Igual que el anterior pero ignora las citas canceladas y, al editar,
     * la propia cita (excluirCitaId).
     */
    public boolean existeChoqueDeHorario(Long medicoId, LocalDate fecha, LocalTime hora, Long excluirCitaId) {
        if (medicoId == null || fecha == null || hora == null) return false;
        LocalTime limpia = hora.withSecond(0).withNano(0);
        return citaRepository.findByMedicoIdAndFecha(medicoId, fecha).stream()
                .filter(c -> !"CANCELADA".equals(c.getEstado()))
                .filter(c -> excluirCitaId == null || !excluirCitaId.equals(c.getId()))
                .anyMatch(c -> c.getHora() != null && c.getHora().withSecond(0).withNano(0).equals(limpia));
    }

    public List<Cita> proximas() {
        return citaRepository.findTop8ByFechaGreaterThanEqualAndEstadoInOrderByFechaAscHoraAsc(
                LocalDate.now(), List.of("PENDIENTE", "CONFIRMADA"));
    }

    public Cita guardar(Cita cita) {
        return citaRepository.save(cita);
    }

    public void cambiarEstado(Long id, String estado) {
        citaRepository.findById(id).ifPresent(c -> {
            c.setEstado(estado);
            citaRepository.save(c);
        });
    }

    public long contarCitasDeHoy() {
        return citaRepository.countByFecha(LocalDate.now());
    }

    public long contarPendientes() {
        return citaRepository.countByEstado("PENDIENTE");
    }
}
