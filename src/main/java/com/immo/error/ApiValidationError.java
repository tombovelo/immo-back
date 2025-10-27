package com.immo.error;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiValidationError extends ApiBaseError {
    
    private Map<String, String> errors = new HashMap<>();

    public ApiValidationError() {
        super();
    }

    // ✅ Constructeur pour les cas simples
    public ApiValidationError(String message, int status) {
        super(message, status);
    }

    // ✅ Méthode utilitaire pour ajouter une erreur
    public void addValidationError(String field, String message) {
        this.errors.put(field, message);
    }

    // ✅ Vérifier s'il y a des erreurs de validation
    public boolean hasValidationErrors() {
        return !errors.isEmpty();
    }
}
