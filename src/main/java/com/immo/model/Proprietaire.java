package com.immo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "proprietaire")
public class Proprietaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Nom obligatoire avec validation
    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = true, length = 100)
    private String prenom;

    @Pattern(regexp = "^(\\+261|0)[0-9]{9}$", message = "Le numero doit etre de la forme +261342123236 ou 0326124546")
    @NotBlank(message = "Le numero de telephone obligatoire")
    @Column(nullable = false, length = 20)
    private String telephone;

    // ✅ Adresse optionnelle
    @Size(max = 200, message = "L'adresse ne peut pas dépasser 200 caractères")
    @Column(length = 200)
    private String adresse;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Maison> maisons;

    @PrePersist
    protected void onCreate() {
        if (utilisateur != null) {
            dateCreation = LocalDateTime.now();
            utilisateur.setRole("ROLE_PROPRIETAIRE");
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
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
