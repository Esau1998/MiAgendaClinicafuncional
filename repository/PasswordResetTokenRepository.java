package org.esfe.miagendaclinica.repository;

import org.esfe.miagendaclinica.model.PasswordResetToken;
import org.esfe.miagendaclinica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    List<PasswordResetToken> findByUsuarioAndUsadoFalse(Usuario usuario);
    void deleteByUsuario(Usuario usuario);
}
