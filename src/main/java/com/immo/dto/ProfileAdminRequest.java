package com.immo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileAdminRequest {
    @Email(message = "format email invalide")
    private String email;
    private  String role;
    @Size(min = 6, message = "mot de passe au moins 6 caractères")
    private String password;
}
