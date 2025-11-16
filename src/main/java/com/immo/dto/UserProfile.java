package com.immo.dto;

import lombok.Data;

@Data
public class UserProfile {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String urlProfile;
}