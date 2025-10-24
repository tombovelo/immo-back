package com.immo.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.immo.model.TypeTransaction;

import lombok.Data;

@Data
public class MaisonResponse {
    private Long id;
    private String adresse;
    private String ville;
    private String codePostal;
    private Integer nombrePieces;
    private Double prix;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dateCreation;
    private Boolean visible = true;
    @JsonIgnoreProperties({"maisons"})
    private ProprietaireResponse proprietaire;
    private TypeTransaction typeTransaction;
    @JsonIgnoreProperties("maison")
    private List<AlbumResponse> albums;
}
