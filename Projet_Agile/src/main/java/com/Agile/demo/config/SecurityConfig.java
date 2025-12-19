package com.Agile.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration de sécurité pour l'encodage des mots de passe
 */
@Configuration
public class SecurityConfig {
    private static SecurityConfig securityConfig;

    private SecurityConfig() {}

    public static SecurityConfig getInstance(){
    if(securityConfig==null){
        securityConfig = new SecurityConfig();
    }
    return securityConfig;
    }
    /**
     * Bean pour l'encodage des mots de passe
     * Utilise BCrypt qui est un algorithme de hachage sécurisé
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}