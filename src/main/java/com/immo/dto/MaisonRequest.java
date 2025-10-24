package com.immo.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MaisonRequest {
    
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    private String codePostal;

    @NotNull(message = "Le nombre de pièces est obligatoire")
    @Positive(message = "Le nombre de pièces doit être positif")
    private Integer nombrePieces;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Double prix;

    private String description;

    @NotNull(message = "La latitude est obligatoire")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    private Double longitude;

    private Boolean visible;
    
    @NotNull(message = "Le propriétaire est obligatoire")
    private Long proprietaireId;
    
    @NotNull(message = "Le type de transaction est obligatoire")
    private Long typeTransactionId;
}
