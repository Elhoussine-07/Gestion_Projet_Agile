package com.Agile.demo.exception;

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

}