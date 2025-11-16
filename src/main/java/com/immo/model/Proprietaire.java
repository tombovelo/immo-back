package com.immo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "proprietaire")
public class Proprietaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = true)
    private String dossier;

    @Column(nullable = true)
    private String urlProfile;

    @Column(nullable = true)
    private String cloudinaryPublicId;

    @Column(nullable = true, length = 100)
    private String prenom;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(length = 200)
    private String adresse;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @OneToOne(cascade = CascadeType.ALL) //orphanRemoval : ne peuvent pas exister sans leur parent
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("proprietaire-maisons") // Côté "parent" de la relation (sera affiché).
    private List<Maison> maisons;

    @PrePersist
    protected void onCreate() {
        if (utilisateur != null) {
            dateCreation = LocalDateTime.now();
            utilisateur.setRole("ROLE_PROPRIETAIRE");//ROLE_PROPRIETAIRE
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDossier() { return dossier; }
    public void setDossier(String dossier) { this.dossier = dossier; }
    public String getUrlProfile() { return urlProfile; }
    public void setUrlProfile(String urlProfile) { this.urlProfile = urlProfile; }
    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setUtilisateur(Utilisateur utilisateur) {this.utilisateur = utilisateur;}
    public Utilisateur getUtilisateur() {return this.utilisateur;}
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public List<Maison> getMaisons() { return maisons; }
    public void setMaisons(List<Maison> maisons) { this.maisons = maisons; }
}
