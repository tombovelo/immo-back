package com.immo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "photo")
public class Photo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de fichier est obligatoire")
    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    //@NotBlank(message = "L'id cloudinary est obligatoire")
    @Column(name = "cloudinary_public_id", nullable = true)
    private String cloudinaryPublicId;

    //@NotBlank(message = "L'url est obligatoire")
    @Column(name = "cloudinary_url", nullable = true)
    private String cloudinaryUrl;

    @Column(length = 500)
    private String description;

    private Integer ordre = 0;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @NotNull(message = "L'album est obligatoire")
    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    @JsonBackReference("album-photos") // Côté "enfant" de la relation afficher
    private Album album;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }
    
    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }
    
    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }
    
    public String getCloudinaryUrl() { return cloudinaryUrl; }
    public void setCloudinaryUrl(String cloudinaryUrl) { this.cloudinaryUrl = cloudinaryUrl; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification;}
}