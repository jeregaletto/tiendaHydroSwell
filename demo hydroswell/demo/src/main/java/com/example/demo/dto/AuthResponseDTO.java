package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Respuesta del endpoint de autenticación.
 * Contiene el JWT y los datos básicos del usuario para que el frontend
 * pueda mostrar el nombre/rol sin necesidad de una segunda llamada.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {

    /** JWT Bearer token a incluir en el header Authorization. */
    private String token;

    /** Tipo de token (siempre "Bearer"). */
    @Builder.Default
    private String tokenType = "Bearer";

    private Long   userId;
    private String email;
    private String fullName;
    private String role;
}
