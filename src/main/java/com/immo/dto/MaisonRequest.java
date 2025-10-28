package com.immo.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MaisonRequest {
    
    private String adresse;

    @NotBlank(message = "ville obligatoire")
    private String ville;

    private String codePostal;

    @NotNull(message = "nombre de pièces obligatoire")
    @Positive(message = "Le nombre de pièces doit être positif")
    private Integer nombrePieces;

    @NotNull(message = "prix obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Double prix;

    private String description;

    @NotNull(message = "latitude obligatoire")
    private Double latitude;

    @NotNull(message = "longitude obligatoire")
    private Double longitude;

    private Boolean visible;
    
    @NotNull(message = "propriétaire obligatoire")
    private Long proprietaireId;
    
    @NotNull(message = "type de transaction obligatoire")
    private Long typeTransactionId;
}
