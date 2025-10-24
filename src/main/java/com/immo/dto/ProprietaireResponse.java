package com.immo.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class ProprietaireResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private LocalDateTime dateCreation;
    private UtilisateurResponse utilisateur;
    @JsonIgnoreProperties({"proprietaire"})
    private List<MaisonResponse> maisons;
}
    
