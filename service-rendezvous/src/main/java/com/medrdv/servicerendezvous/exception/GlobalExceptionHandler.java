package com.medrdv.servicerendezvous.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erreurs métier (ex: créneau déjà pris, patient suspendu)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBusinessException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Ressource introuvable (404 via Feign)
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<String> handleFeignNotFound(FeignException.NotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ressource externe introuvable (Patient, Docteur ou Créneau).");
    }

    // Erreurs de communication (Timeout, Connexion refusée)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleFeignException(FeignException ex) {
        if (ex.status() == -1) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Échec de communication avec un service distant.");
        }
        if (ex.status() == 503) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service distant indisponible.");
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Erreur HTTP du service distant : " + ex.status());
    }
}