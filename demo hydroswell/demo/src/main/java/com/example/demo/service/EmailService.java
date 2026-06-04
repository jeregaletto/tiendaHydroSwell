package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoVerificacion(String emailDestino, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("tu-correo@gmail.com");
        mensaje.setTo(emailDestino);
        mensaje.setSubject("Bienvenido a HydroSwell - Verificá tu cuenta");
        
        String urlVerificacion = "http://localhost:3000/verificar?token=" + token;
        System.out.println("🔥 EL LINK GENERADO ES: " + urlVerificacion);
        
        mensaje.setText("¡Hola! Gracias por sumarte a la comunidad HydroSwell.\n\n" +
                       "Para activar tu cuenta y empezar a comprar, hacé clic acá:\n" + 
                       urlVerificacion);

        mailSender.send(mensaje);
        System.out.println("Correo de verificación enviado a: " + emailDestino);
    }
}
