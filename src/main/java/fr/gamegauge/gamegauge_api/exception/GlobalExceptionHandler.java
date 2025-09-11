package fr.gamegauge.gamegauge_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire d'exceptions global pour l'application.
 * Capture certaines exceptions et les transforme en réponses HTTP claires pour le client.
 */
@RestControllerAdvice // Permet de partager la gestion d'exceptions sur plusieurs contrôleurs.
public class GlobalExceptionHandler {

    /**
     * Gère les exceptions de validation levées par l'annotation @Valid.
     *
     * @param ex L'exception capturée.
     * @return une Map contenant les champs invalides et les messages d'erreur correspondants.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Définit le code de statut HTTP à 400 Bad Request.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * Gère les exceptions liées à des états illégaux (ex: email déjà utilisé).
     *
     * @param ex L'exception capturée.
     * @return Le message de l'exception.
     */
    @ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict est approprié pour un email/username déjà existant.
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex) {
        return ex.getMessage();
    }
}