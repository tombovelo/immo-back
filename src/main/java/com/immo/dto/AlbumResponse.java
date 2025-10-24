package com.immo.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class AlbumResponse {
    private Long id;
    private String nomAlbum;
    private String path;
    private String description;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    @JsonIgnoreProperties({"albums"})
    private MaisonResponse maison;
    @JsonIgnoreProperties("album")
    private List<PhotoResponse> photos;
}
