package org.esfe.miagendaclinica.repository;

import org.esfe.miagendaclinica.model.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Long> {
    List<Tratamiento> findByHistorialClinicoId(Long historialId);
}
