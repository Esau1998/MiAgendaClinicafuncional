package org.esfe.miagendaclinica.config;

import org.esfe.miagendaclinica.model.*;
import org.esfe.miagendaclinica.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Al iniciar la aplicacion:
 *  1. Crea los roles base y el usuario administrador (admin / admin123) si no existen.
 *  2. Convierte a BCrypt cualquier contrasena antigua guardada en texto plano.
 *  3. Si la base esta vacia y app.datos-demo=true, carga especialidades,
 *     medicos y horarios de ejemplo para que el catalogo se vea completo.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.datos-demo:true}")
    private boolean datosDemo;

    @Override
    public void run(String... args) {
        Rol admin = rolRepository.findByNombre("ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(new Rol("ADMINISTRADOR")));
        rolRepository.findByNombre("MEDICO")
                .orElseGet(() -> rolRepository.save(new Rol("MEDICO")));
        rolRepository.findByNombre("RECEPCIONISTA")
                .orElseGet(() -> rolRepository.save(new Rol("RECEPCIONISTA")));

        if (usuarioRepository.findByUsernameIgnoreCase("admin").isEmpty()) {
            Usuario usuarioAdmin = new Usuario();
            usuarioAdmin.setUsername("admin");
            usuarioAdmin.setPassword(passwordEncoder.encode("admin123"));
            usuarioAdmin.setNombreCompleto("Administrador General");
            usuarioAdmin.setRol(admin);
            usuarioAdmin.setActivo(true);
            usuarioRepository.save(usuarioAdmin);
        }

        for (Usuario u : usuarioRepository.findAll()) {
            if (u.getPassword() != null && !u.getPassword().startsWith("$2")) {
                u.setPassword(passwordEncoder.encode(u.getPassword()));
                usuarioRepository.save(u);
            }
        }

        if (datosDemo && especialidadRepository.count() == 0 && medicoRepository.count() == 0) {
            cargarDatosDemo();
        }
    }

    private void cargarDatosDemo() {
        Especialidad general = especialidad("Medicina General",
                "Consulta integral, control de enfermedades comunes y chequeos preventivos.");
        Especialidad pediatria = especialidad("Pediatría",
                "Atención médica para bebés, niños y adolescentes.");
        Especialidad cardiologia = especialidad("Cardiología",
                "Diagnóstico y seguimiento de enfermedades del corazón y la presión arterial.");
        Especialidad dermatologia = especialidad("Dermatología",
                "Cuidado de la piel, el cabello y las uñas.");
        Especialidad odontologia = especialidad("Odontología",
                "Limpieza, restauración y salud bucal para toda la familia.");
        Especialidad ginecologia = especialidad("Ginecología",
                "Salud de la mujer, control prenatal y planificación familiar.");

        Medico m1 = medico("Carlos", "Menjívar", general, "JVPM-1001", "7000-0001", "cmenjivar@miagendaclinica.local",
                "Médico general con enfoque en prevención y control de enfermedades crónicas.");
        Medico m2 = medico("Sofía", "Hernández", pediatria, "JVPM-1002", "7000-0002", "shernandez@miagendaclinica.local",
                "Pediatra dedicada al crecimiento y desarrollo saludable de los más pequeños.");
        Medico m3 = medico("Ricardo", "Alvarado", cardiologia, "JVPM-1003", "7000-0003", "ralvarado@miagendaclinica.local",
                "Cardiólogo con experiencia en hipertensión, electrocardiogramas y chequeos del corazón.");
        Medico m4 = medico("Daniela", "Rivas", dermatologia, "JVPM-1004", "7000-0004", "drivas@miagendaclinica.local",
                "Dermatóloga especializada en acné, alergias de la piel y cuidado dermatológico.");
        Medico m5 = medico("Mauricio", "Cruz", odontologia, "JVPM-1005", "7000-0005", "mcruz@miagendaclinica.local",
                "Odontólogo general: limpiezas, resinas, extracciones y control periódico.");
        Medico m6 = medico("Valeria", "Martínez", ginecologia, "JVPM-1006", "7000-0006", "vmartinez@miagendaclinica.local",
                "Ginecóloga y obstetra con enfoque en control prenatal y salud preventiva.");

        horario(m1, "LUNES", "08:00", "12:00");
        horario(m1, "MIERCOLES", "08:00", "12:00");
        horario(m1, "VIERNES", "08:00", "12:00");
        horario(m1, "MARTES", "14:00", "17:00");

        horario(m2, "LUNES", "14:00", "18:00");
        horario(m2, "MARTES", "08:00", "12:00");
        horario(m2, "JUEVES", "08:00", "12:00");
        horario(m2, "SABADO", "08:00", "11:00");

        horario(m3, "MARTES", "09:00", "13:00");
        horario(m3, "JUEVES", "14:00", "18:00");

        horario(m4, "MIERCOLES", "13:00", "17:00");
        horario(m4, "VIERNES", "09:00", "13:00");

        horario(m5, "LUNES", "09:00", "13:00");
        horario(m5, "MIERCOLES", "14:00", "18:00");
        horario(m5, "SABADO", "08:00", "12:00");

        horario(m6, "MARTES", "14:00", "18:00");
        horario(m6, "JUEVES", "09:00", "13:00");
        horario(m6, "VIERNES", "14:00", "17:00");
    }

    private Especialidad especialidad(String nombre, String descripcion) {
        Especialidad e = new Especialidad();
        e.setNombre(nombre);
        e.setDescripcion(descripcion);
        return especialidadRepository.save(e);
    }

    private Medico medico(String nombre, String apellido, Especialidad esp, String numero,
                          String telefono, String correo, String descripcion) {
        Medico m = new Medico();
        m.setNombre(nombre);
        m.setApellido(apellido);
        m.setEspecialidad(esp);
        m.setNumeroProfesional(numero);
        m.setTelefono(telefono);
        m.setCorreo(correo);
        m.setDescripcion(descripcion);
        m.setEstado("ACTIVO");
        return medicoRepository.save(m);
    }

    private void horario(Medico medico, String dia, String inicio, String fin) {
        Horario h = new Horario();
        h.setMedico(medico);
        h.setDia(dia);
        h.setHoraInicio(LocalTime.parse(inicio));
        h.setHoraFin(LocalTime.parse(fin));
        h.setEstado("ACTIVO");
        horarioRepository.save(h);
    }
}
