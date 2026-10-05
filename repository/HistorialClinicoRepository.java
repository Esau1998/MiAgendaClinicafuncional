package org.esfe.miagendaclinica.repository;

import org.esfe.miagendaclinica.model.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {
    List<HistorialClinico> findByPacienteIdOrderByFechaDesc(Long pacienteId);
}
