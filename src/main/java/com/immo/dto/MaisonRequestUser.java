package com.immo.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MaisonRequestUser {
    private String adresse;
    private String ville;
    private String codePostal;
    @Positive(message = "Le nombre de pièces doit être positif")
    private Integer nombrePieces;
    @Positive(message = "Le prix doit être positif")
    private Double prix;
    private String description;
    private Double latitude;
    private Double longitude;
    private Boolean visible;
    private Long typeTransactionId;
    private MultipartFile file;
}
