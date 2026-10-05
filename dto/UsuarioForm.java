package org.esfe.miagendaclinica.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos del formulario de usuarios (administrador). Se usa un objeto simple
 * en lugar de la entidad para evitar problemas al enlazar rol y medico.
 */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioForm {
    private Long id;
    private String nombreCompleto;
    private String username;
    private String email;
    private String password;
    private Long rolId;
    private Long medicoId;
    private boolean activo = true;
}
