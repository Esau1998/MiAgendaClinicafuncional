package org.esfe.miagendaclinica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String sexo; // M, F, OTRO

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String contactoEmergencia;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO
}
