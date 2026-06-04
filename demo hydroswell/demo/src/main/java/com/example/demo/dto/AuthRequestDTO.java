package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para el endpoint POST /api/v1/auth/login
 *
 * Se usa el mismo DTO para login y como base para el registro.
 * Si necesitas campos adicionales en el registro (fullName, etc.),
 * extiende esta clase con RegisterRequestDTO.
 */
@Data
public class AuthRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}
