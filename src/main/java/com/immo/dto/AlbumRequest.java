package com.immo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlbumRequest {
    @NotBlank(message = "nom de l'album obligatoire")
    private String nomAlbum;
    private String description;
    @NotNull(message = "maison obligatoire")
    private Long maisonId;
}
