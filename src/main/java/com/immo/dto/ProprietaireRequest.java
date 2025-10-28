package com.immo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProprietaireRequest {
    // ✅ Nom obligatoire avec validation
    @NotBlank(message = "nom obligatoire")
    private String nom;
    private String prenom;
    @Pattern(regexp = "^(\\+261|0)[0-9]{9}$", message = "Numéro invalide (+261XXXXXXXXX ou 0XXXXXXXXX)")
    @NotBlank(message = "telephone obligatoire")
    private String telephone;
    private String adresse;
    @NotNull(message = "utilisateur obligatoire")
    @Valid
    private UtilisateurRequest utilisateur;
}
