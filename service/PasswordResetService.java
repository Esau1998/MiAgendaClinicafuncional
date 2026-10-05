package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.PasswordResetToken;
import org.esfe.miagendaclinica.model.Usuario;
import org.esfe.miagendaclinica.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Flujo de "Olvide mi contrasena": genera un token de un solo uso con
 * vencimiento, lo envia por correo (si hay SMTP configurado) y permite
 * establecer una contrasena nueva.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    public record Resultado(boolean usuarioEncontrado, boolean correoEnviado, String enlace) { }

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.recuperacion.minutos-validez:30}")
    private long minutosValidez;

    @Value("${app.mail.from:no-reply@miagendaclinica.local}")
    private String remitente;

    // Solo existe si configuraste spring.mail.host en application.properties
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Transactional
    public Resultado solicitar(String identificador) {
        Optional<Usuario> opt = usuarioService.buscarPorIdentificador(identificador);
        if (opt.isEmpty() || !Boolean.TRUE.equals(opt.get().getActivo())) {
            return new Resultado(false, false, null);
        }
        Usuario usuario = opt.get();

        // Invalida solicitudes anteriores pendientes
        for (PasswordResetToken anterior : tokenRepository.findByUsuarioAndUsadoFalse(usuario)) {
            anterior.setUsado(true);
            tokenRepository.save(anterior);
        }

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String tokenPlano = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash(hash(tokenPlano));
        token.setUsuario(usuario);
        token.setExpiracion(LocalDateTime.now().plusMinutes(minutosValidez));
        token.setUsado(false);
        tokenRepository.save(token);

        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String enlace = base + "/restablecer-password?token=" + tokenPlano;

        boolean enviado = enviarCorreo(usuario, enlace);
        return new Resultado(true, enviado, enlace);
    }

    public Optional<PasswordResetToken> validar(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isBlank()) return Optional.empty();
        return tokenRepository.findByTokenHash(hash(tokenPlano.trim()))
                .filter(t -> !Boolean.TRUE.equals(t.getUsado()))
                .filter(t -> t.getExpiracion().isAfter(LocalDateTime.now()))
                .filter(t -> Boolean.TRUE.equals(t.getUsuario().getActivo()));
    }

    @Transactional
    public void restablecer(PasswordResetToken token, String nuevaPassword) {
        usuarioService.cambiarPassword(token.getUsuario(), nuevaPassword);
        token.setUsado(true);
        tokenRepository.save(token);
    }

    private boolean enviarCorreo(Usuario usuario, String enlace) {
        if (mailSender == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) return false;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remitente);
            msg.setTo(usuario.getEmail());
            msg.setSubject("Recuperar contraseña - Mi Agenda de Clínica");
            msg.setText("Hola " + usuario.getNombreCompleto() + ",\n\n"
                    + "Recibimos una solicitud para restablecer tu contraseña.\n"
                    + "Usa este enlace (válido por " + minutosValidez + " minutos y de un solo uso):\n\n"
                    + enlace + "\n\n"
                    + "Si no fuiste tú, ignora este mensaje: tu contraseña seguirá igual.\n\n"
                    + "Mi Agenda de Clínica");
            mailSender.send(msg);
            return true;
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de recuperación: {}", e.getMessage());
            return false;
        }
    }

    private static String hash(String valor) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
