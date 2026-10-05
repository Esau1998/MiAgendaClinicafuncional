package org.esfe.miagendaclinica.repository;

import org.esfe.miagendaclinica.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByFecha(LocalDate fecha);
    List<Cita> findByPacienteId(Long pacienteId);
    List<Cita> findByMedicoId(Long medicoId);
    List<Cita> findByEstado(String estado);
    boolean existsByMedicoIdAndFechaAndHora(Long medicoId, LocalDate fecha, LocalTime hora);
    long countByFecha(LocalDate fecha);
    long countByEstado(String estado);
    List<Cita> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha);
    List<Cita> findTop8ByFechaGreaterThanEqualAndEstadoInOrderByFechaAscHoraAsc(LocalDate fecha, Collection<String> estados);
}
