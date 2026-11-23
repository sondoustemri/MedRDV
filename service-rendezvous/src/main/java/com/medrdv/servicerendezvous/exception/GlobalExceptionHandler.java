package com.medrdv.servicerendezvous.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Gère l'idempotence (Opération déjà traitée) et les refus métier
    // DOIT ÊTRE AVANT RuntimeException pour être prioritaire
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value()); // 400
        body.put("error", "Refus Métier");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 2. Gère les erreurs de type "Service Indisponible" (Circuit Breaker)
    @ExceptionHandler(ServiceExterneIndisponibleException.class)
    public ResponseEntity<Map<String, Object>> handleServiceIndisponible(ServiceExterneIndisponibleException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value()); // 503
        body.put("error", "Service Indisponible");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    // 3. Gère les erreurs Feign (404)
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleFeignNotFound(FeignException.NotFound ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value()); // 404
        body.put("error", "Ressource Introuvable");
        body.put("message", "Ressource externe introuvable (Patient, Docteur ou Créneau).");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 4. Gère les autres erreurs Feign (503, 502, etc.)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());

        if (ex.status() == -1) {
            body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            body.put("error", "Service Indisponible");
            body.put("message", "Échec de communication avec un service distant.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
        if (ex.status() == 503) {
            body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            body.put("error", "Service Indisponible");
            body.put("message", "Service distant indisponible.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
        body.put("status", HttpStatus.BAD_GATEWAY.value());
        body.put("error", "Bad Gateway");
        body.put("message", "Erreur HTTP du service distant : " + ex.status());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    // 5. Gère les autres RuntimeException (ex: Rendez-vous introuvable)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value()); // 400
        body.put("error", "Erreur Métier");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 6. Gère toutes les autres erreurs inattendues
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value()); // 500
        body.put("error", "Erreur Interne");
        body.put("message", "Une erreur inattendue est survenue : " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}