package com.immo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "maison")
public class Maison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String adresse;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column(length = 10)
    private String codePostal;

    @Column(nullable = false)
    private Integer nombrePieces;

    @Column(nullable = false)
    private Double prix;

    @Column(length = 500)
    private String description;

    @Column(name = "coordonnees", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point coordinate;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private Boolean visible = true;

    @ManyToOne
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire proprietaire;

    @ManyToOne
    @JoinColumn(name = "type_transaction_id", nullable = false)
    private TypeTransaction typeTransaction;

    @OneToMany(mappedBy = "maison", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Album> albums;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Proprietaire getProprietaire() { return proprietaire; }
    public void setProprietaire(Proprietaire proprietaire) { this.proprietaire = proprietaire; }
    public TypeTransaction getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(TypeTransaction typeTransaction) { this.typeTransaction = typeTransaction; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    public Integer getNombrePieces() { return nombrePieces; }
    public void setNombrePieces(Integer nombrePieces) { this.nombrePieces = nombrePieces; }
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Point getCoordinate() {return coordinate; }
    public void setCoordinate(Point coordinate) {this.coordinate = coordinate;}
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }
    public List<Album> getAlbums() { return albums; }
    public void setAlbums(List<Album> albums) { this.albums = albums; }
}

