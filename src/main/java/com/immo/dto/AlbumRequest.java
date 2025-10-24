package com.immo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlbumRequest {
    @NotBlank(message = "Le nom de l'album est obligatoire")
    private String nomAlbum;
    private String description;
    @NotNull(message = "La maison est obligatoire")
    private Long maisonId;
}
