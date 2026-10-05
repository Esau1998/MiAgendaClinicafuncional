package org.esfe.miagendaclinica.repository;

import org.esfe.miagendaclinica.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findByEstado(String estado);
    Optional<Paciente> findFirstByCorreoIgnoreCase(String correo);
    List<Paciente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}
