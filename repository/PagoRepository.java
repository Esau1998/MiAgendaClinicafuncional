package org.esfe.miagendaclinica.repository;

import org.esfe.miagendaclinica.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByPacienteId(Long pacienteId);
    List<Pago> findByEstado(String estado);
}
