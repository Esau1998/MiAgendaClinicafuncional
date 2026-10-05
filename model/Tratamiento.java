package org.esfe.miagendaclinica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tratamientos")
@Getter
@Setter
@NoArgsConstructor
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "historial_id", nullable = false)
    private HistorialClinico historialClinico;

    @Column(nullable = false, length = 150)
    private String medicamento;

    @Column(length = 255)
    private String indicaciones;

    @Column(length = 60)
    private String dosis;

    @Column(length = 60)
    private String frecuencia;

    @Column(length = 60)
    private String duracion;

    @Column(length = 255)
    private String observaciones;
}
