package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO de registro — extiende AuthRequestDTO con el campo fullName.
 * Endpoint: POST /api/v1/auth/register
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterRequestDTO extends AuthRequestDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    private String fullName;
}
