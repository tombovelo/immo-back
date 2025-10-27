package com.immo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    private String adresse;
    @NotNull(message = "utilisateur est obligatoire")
    @Valid
    private UtilisateurRequest utilisateur;
}
