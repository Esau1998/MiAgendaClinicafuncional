package org.esfe.miagendaclinica.service;

import org.esfe.miagendaclinica.model.Usuario;
import org.esfe.miagendaclinica.repository.PasswordResetTokenRepository;
import org.esfe.miagendaclinica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{4,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /** Busca por nombre de usuario o por correo (sin distinguir mayusculas). */
    public Optional<Usuario> buscarPorIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) return Optional.empty();
        String valor = identificador.trim();
        Optional<Usuario> porUsuario = usuarioRepository.findByUsernameIgnoreCase(valor);
        if (porUsuario.isPresent()) return porUsuario;
        return usuarioRepository.findByEmailIgnoreCase(valor);
    }

    /**
     * Valida credenciales. Las contrasenas se comparan con BCrypt.
     * Si encuentra una contrasena antigua en texto plano (de versiones previas)
     * y es correcta, la convierte a BCrypt automaticamente.
     */
    public Optional<Usuario> autenticar(String identificador, String password) {
        if (password == null) return Optional.empty();
        Optional<Usuario> opt = buscarPorIdentificador(identificador);
        if (opt.isEmpty()) return Optional.empty();

        Usuario u = opt.get();
        String guardada = u.getPassword();
        if (guardada == null) return Optional.empty();

        if (guardada.startsWith("$2")) {
            return passwordEncoder.matches(password, guardada) ? opt : Optional.empty();
        }
        if (guardada.equals(password)) {
            u.setPassword(passwordEncoder.encode(password));
            usuarioRepository.save(u);
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public boolean coincidePassword(Usuario u, String password) {
        if (u.getPassword() == null || password == null) return false;
        if (u.getPassword().startsWith("$2")) return passwordEncoder.matches(password, u.getPassword());
        return u.getPassword().equals(password);
    }

    // ---------- Validaciones (devuelven el mensaje de error o null si todo esta bien) ----------

    public String validarDatos(Long idActual, String nombre, String username, String email, boolean emailObligatorio) {
        if (nombre == null || nombre.isBlank()) return "El nombre completo es obligatorio.";
        if (nombre.trim().length() > 100) return "El nombre completo no puede superar 100 caracteres.";

        if (username == null || !USERNAME.matcher(username.trim()).matches()) {
            return "El usuario debe tener entre 4 y 30 caracteres (letras, números, punto, guion o guion bajo, sin espacios).";
        }
        Optional<Usuario> porUsername = usuarioRepository.findByUsernameIgnoreCase(username.trim());
        if (porUsername.isPresent() && !porUsername.get().getId().equals(idActual)) {
            return "Ese nombre de usuario ya está en uso.";
        }

        boolean hayEmail = email != null && !email.isBlank();
        if (!hayEmail && emailObligatorio) return "El correo electrónico es obligatorio.";
        if (hayEmail) {
            String correo = email.trim();
            if (correo.length() > 100 || !EMAIL.matcher(correo).matches()) return "El correo electrónico no es válido.";
            Optional<Usuario> porEmail = usuarioRepository.findByEmailIgnoreCase(correo);
            if (porEmail.isPresent() && !porEmail.get().getId().equals(idActual)) {
                return "Ese correo electrónico ya está registrado.";
            }
        }
        return null;
    }

    public String validarPassword(String password, String confirmar) {
        if (password == null || password.isEmpty()) return "La contraseña es obligatoria.";
        if (password.length() < 8) return "La contraseña debe tener al menos 8 caracteres.";
        if (password.length() > 64) return "La contraseña no puede superar 64 caracteres.";
        boolean letra = password.chars().anyMatch(Character::isLetter);
        boolean numero = password.chars().anyMatch(Character::isDigit);
        if (!letra || !numero) return "La contraseña debe combinar letras y números.";
        if (confirmar != null && !password.equals(confirmar)) return "Las contraseñas no coinciden.";
        return null;
    }

    // ---------- Escritura ----------

    public String normalizarEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase();
    }

    public String cifrar(String passwordPlana) {
        return passwordEncoder.encode(passwordPlana);
    }

    /** Guarda un usuario ya armado (la contrasena debe venir ya cifrada). */
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void cambiarPassword(Usuario usuario, String passwordPlana) {
        usuario.setPassword(passwordEncoder.encode(passwordPlana));
        usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorMedico(Long medicoId) {
        return usuarioRepository.findByMedicoId(medicoId);
    }

    public long contarAdministradoresActivos() {
        return usuarioRepository.countByRolNombreAndActivoTrue("ADMINISTRADOR");
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            tokenRepository.deleteByUsuario(u);
            usuarioRepository.delete(u);
        });
    }
}
