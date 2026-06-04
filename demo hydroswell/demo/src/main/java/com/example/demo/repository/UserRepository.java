package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Busca por email (identificador único de login). */
    Optional<User> findByEmail(String email);

    /** Verificación de email duplicado en el registro. */
    boolean existsByEmail(String email);

    /**Busca al usuario por el token recibido en su email
     * para activar su cuenta */
    Optional<User> findByVerificationToken(String token);
}
