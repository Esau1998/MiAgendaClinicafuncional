package org.esfe.miagendaclinica.repository;

import org.esfe.miagendaclinica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsernameIgnoreCase(String username);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    Optional<Usuario> findByMedicoId(Long medicoId);
    long countByRolNombreAndActivoTrue(String nombre);
}
