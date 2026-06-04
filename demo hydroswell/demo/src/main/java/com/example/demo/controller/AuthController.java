package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService; // Ajustá al paquete de tu EmailService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ➡️ ENDPOINT DE REGISTRO
    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(@RequestBody User request) {
        // Validamos si ya existe el mail
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("El email ya está registrado.");
        }

        // Generamos el token único (UUID)
        String token = UUID.randomUUID().toString();

        // Armamos el usuario usando Builder de Lombok
        User nuevoUsuario = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(request.getPasswordHash()) // Idealmente encriptada con BCrypt luego
                .role(User.Role.CUSTOMER) // Rol por defecto
                .isActive(false) // No está activo hasta validar
                .verificationToken(token)
                .build();

        userRepository.save(nuevoUsuario);

        // Enviamos el correo real usando tu servicio
        emailService.enviarCorreoVerificacion(nuevoUsuario.getEmail(), token);

        return ResponseEntity.ok("Registro exitoso. Revisá tu correo para activar tu cuenta de HydroSwell.");
    }
    @PostMapping("/login")
    public ResponseEntity<String> loginUsuario(@RequestBody User request) {
    return userRepository.findByEmail(request.getEmail())
            .map(user -> {
                // 1. Verificamos si la cuenta está activa
                if (!user.getIsActive()) {
                    return ResponseEntity.badRequest().body("Error: Debés verificar tu correo antes de iniciar sesión.");
                }
                
                // 2. Verificamos la contraseña (aquí luego usarías BCrypt)
                if (!user.getPasswordHash().equals(request.getPasswordHash())) {
                    return ResponseEntity.badRequest().body("Error: Contraseña incorrecta.");
                }
                
                return ResponseEntity.ok("Login exitoso. ¡Bienvenido!");
            })
            .orElse(ResponseEntity.badRequest().body("Error: El usuario no existe."));
    }
    // ➡️ ENDPOINT DE VERIFICACIÓN (Al hacer clic en el link del mail)
    @GetMapping("/verify")
    public ResponseEntity<String> verificarCuenta(@RequestParam("token") String token) {
        return userRepository.findByVerificationToken(token)
                .map(user -> {
                    user.setIsActive(true); // Activamos la cuenta
                    user.setVerificationToken(null); // Limpiamos el token
                    userRepository.save(user);
                    return ResponseEntity.ok("¡Cuenta verificada con éxito! Ya podés iniciar sesión en HydroSwell.");
                })
                .orElse(ResponseEntity.badRequest().body("Error: Token inválido o expirado."));
    }
}