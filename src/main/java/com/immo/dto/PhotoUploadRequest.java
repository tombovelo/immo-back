package com.immo.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class PhotoUploadRequest {

    @NotNull(message = "Album obligatoire")
    private Long albumId;

    @NotNull(message = "fichier obligatoire")
    private MultipartFile file;

    private String description;
    private Integer ordre;

    // Getters & setters
    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
}

