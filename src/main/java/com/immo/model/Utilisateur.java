package com.immo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'eamil est obligatoire")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "le mots de passe est obligatoire")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @PrePersist
    public void onCreate() {
        if (this.role == null || this.role.trim().isEmpty()) {
            this.role = "ROLE_PROPRIETAIRE";
        }
    }
}



