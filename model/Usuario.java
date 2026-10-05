package org.esfe.miagendaclinica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    // Siempre se guarda cifrada con BCrypt (nunca en texto plano)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String nombreCompleto;

    // Se usa para recuperar la contrasena
    @Column(unique = true, length = 100)
    private String email;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private Boolean activo = true;

    // Si el usuario es un medico, se enlaza aqui (opcional)
    @OneToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
