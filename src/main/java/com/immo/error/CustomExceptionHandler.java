package com.immo.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class CustomExceptionHandler {
    
    // 1. Gestion des ressources non trouvées (404)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiNotFoundError> handleNotFoundException(NotFoundException exc) {
        ApiNotFoundError apiError = new ApiNotFoundError(exc.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiBaseError> handleFileUploadException(FileUploadException exc) {
        ApiBaseError apiError = new ApiBaseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 2. Gestion des erreurs de validation (400 - Bad Request) ok
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiValidationError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ApiValidationError error = new ApiValidationError(
            "Erreurs de validation", 
            HttpStatus.BAD_REQUEST.value()
        );
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> 
            error.addValidationError(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Gestion des paramètres URL incorrects (400 - Bad Request)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiNotFoundError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        String message;
        if (requiredType == null) {
            message = "Paramètre incorrect";
        } else {
            message = "Paramètre incorrect: " + ex.getName() + " doit être de type " + requiredType.getSimpleName();
        }
        ApiNotFoundError apiError = new ApiNotFoundError(message, HttpStatus.BAD_REQUEST.value());
        apiError.setStatus(HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // 5. Gestion de toutes les autres exceptions (500 - Internal Server Error) ok
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiBaseError> handleAuthException(AuthException exc) {
        String message = exc.getMessage();
        ApiBaseError apiError = new ApiBaseError(message, HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    
    // 5. Gestion de toutes les autres exceptions (500 - Internal Server Error) ok
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiNotFoundError> handleOtherException(Exception exc) {
        String message = "Erreur interne du serveur: " + exc.getMessage();
        ApiNotFoundError apiError = new ApiNotFoundError(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiNotFoundError> handleDataIntegrityViolationException(DataIntegrityViolationException exc) {
        String message = "Violation d'intégrité des données";
        String causeMessage = exc.getMostSpecificCause().getMessage();
        if (causeMessage != null && causeMessage.contains("Key")) {
            List<Map<String, String>> fields = extractFieldsAndValuesFromMessage(causeMessage);
            // On regroupe les champs et les valeurs dans une phrase claire
            StringBuilder sb = new StringBuilder();
            sb.append(fields.stream()
                    .map(entry -> String.format("%s : %s", entry.get("field"), entry.get("value")))
                    .collect(Collectors.joining(", ")));
            sb.append(" déjà existant(s).");
            message = sb.toString();
        }
        ApiNotFoundError apiError = new ApiNotFoundError(message, HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    private List<Map<String, String>> extractFieldsAndValuesFromMessage(String message) {
        List<Map<String, String>> results = new ArrayList<>();
        Pattern pattern = Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            // Liste des champs et valeurs séparés par des virgules
            String[] fields = matcher.group(1).split("\\s*,\\s*");
            String[] values = matcher.group(2).split("\\s*,\\s*");
            for (int i = 0; i < fields.length && i < values.length; i++) {
                Map<String, String> map = new HashMap<>();
                map.put("field", fields[i].trim());
                map.put("value", values[i].trim());
                results.add(map);
            }
        }
        // 🔒 Si rien n’a été trouvé
        if (results.isEmpty()) {
            Map<String, String> unknown = new HashMap<>();
            unknown.put("field", "inconnu");
            unknown.put("value", "inconnu");
            results.add(unknown);
        }
        return results;
    }
}