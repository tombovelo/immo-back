package com.immo.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String type;
    private String email;
    private String role;
    private UserProfile userProfile;
}

