package com.Agile.demo.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée lors de la violation de règles métier ou de validation
 * Retourne un statut HTTP 400 (Bad Request)
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    /**
     * Constructeur avec message simple
     * @param message Message d'erreur
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructeur avec message et cause
     * @param message Message d'erreur
     * @param cause Cause de l'exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructeur avec message formaté
     * @param format Format du message (style String.format)
     * @param args Arguments pour le format
     */
    public ValidationException(String format, Object... args) {
        super(String.format(format, args));
    }
}