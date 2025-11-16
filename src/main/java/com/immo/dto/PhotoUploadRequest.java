package com.immo.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class PhotoUploadRequest {

    @NotNull(message = "Album obligatoire")
    private Long albumId;

    @NotNull(message = "fichier obligatoire")
    private MultipartFile file;

    private String description;
    private Integer ordre;
}

