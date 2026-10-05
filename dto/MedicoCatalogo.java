package org.esfe.miagendaclinica.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** Datos de un medico tal como se muestra en el catalogo publico. */
@Getter
@AllArgsConstructor
public class MedicoCatalogo {
    private Long id;
    private String nombreCompleto;
    private Long especialidadId;
    private String especialidad;
    private String descripcion;
    private String fotoUrl;
    private String iniciales;
    private List<String> horarios;
    private String textoBusqueda;
}
