package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se busca un recurso que no existe en la BD.
 *
 * @ResponseStatus(NOT_FOUND) es opcional aquí porque el GlobalExceptionHandler
 * ya lo maneja y formatea la respuesta. Lo dejamos como documentación de intención.
 *
 * Uso:
 *   throw new ResourceNotFoundException("Producto no encontrado con id: " + id);
 *   throw new ResourceNotFoundException("Usuario", "email", email);
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor simple con mensaje libre.
     * Ej: throw new ResourceNotFoundException("Producto no encontrado con id: 42");
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor semántico para mensajes más descriptivos.
     * Ej: throw new ResourceNotFoundException("Product", "id", 42);
     * Resultado: "Product no encontrado con id: '42'"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
