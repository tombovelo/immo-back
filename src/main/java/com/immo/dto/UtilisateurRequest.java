package com.immo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UtilisateurRequest {
    @NotBlank(message = "Email obligatoire")
    @Email(message = "L'email doit être valide (ex: user@domain.com)")
    private String email;
    private String role;
    private String password;
}
