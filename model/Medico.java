package org.esfe.miagendaclinica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medicos")
@Getter
@Setter
@NoArgsConstructor
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @ManyToOne
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    @Column(length = 40)
    private String numeroProfesional;

    // Texto corto que se muestra en el catalogo publico
    @Column(length = 600)
    private String descripcion;

    // URL de la foto (opcional). Si esta vacia se muestran las iniciales.
    @Column(length = 500)
    private String fotoUrl;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO
}
