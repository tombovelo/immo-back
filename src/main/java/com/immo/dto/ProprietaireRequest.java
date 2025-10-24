package com.immo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProprietaireRequest {
    // ✅ Nom obligatoire avec validation
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    private String prenom;
    @Pattern(regexp = "^(\\+261|0)[0-9]{9}$", message = "Le numero doit etre de la forme +261342123236 ou 0326124546")
    @NotBlank(message = "Le numero de telephone obligatoire")
    private String telephone;
    @Size(max = 200, message = "L'adresse ne peut pas dépasser 200 caractères")
    private String adresse;
    @NotNull(message = "utilisateur est obligatoire")
    private UtilisateurRequest utilisateur;
}
