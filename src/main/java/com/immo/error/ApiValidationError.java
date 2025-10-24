package com.immo.error;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiValidationError extends ApiBaseError {
    
    private Map<String, String> validationErrors = new HashMap<>();

    public ApiValidationError() {
        super();
    }

    // ✅ Constructeur pour les cas simples
    public ApiValidationError(String message, int status) {
        super(message, status);
    }

    // ✅ Méthode utilitaire pour ajouter une erreur
    public void addValidationError(String field, String errorMessage) {
        this.validationErrors.put(field, errorMessage);
    }

    // ✅ Vérifier s'il y a des erreurs de validation
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
}
