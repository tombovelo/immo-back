package com.immo.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class PhotoResponse {
    private Long id;
    private String nomFichier;
    private String cloudinaryPublicId;
    private String cloudinaryUrl;
    private String description;
    private Integer ordre = 0;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    @JsonIgnoreProperties({"photos"})
    private AlbumResponse album;
    
}
