package fr.gamegauge.gamegauge_api.exception;

// 1. Importer les classes de Log4j2
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 2. Initialiser le logger
    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Gère les exceptions de validation levées par l'annotation @Valid.
     *
     * @param ex L'exception capturée.
     * @return une Map contenant les champs invalides et les messages d'erreur correspondants.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // 3. Logguer l'erreur de validation
        logger.warn("Échec de la validation de la requête : {}", errors);

        return errors;
    }

    /**
     * Gère les exceptions liées à des états illégaux (ex: email déjà utilisé).
     *
     * @param ex L'exception capturée.
     * @return Le message de l'exception.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex) {
        // 4. Logguer l'erreur de conflit
        logger.warn("Conflit métier détecté : {}", ex.getMessage());

        return ex.getMessage();
    }
}