package com.immo.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.immo.dto.AuthResponse;
import com.immo.dto.LoginRequest;
import com.immo.dto.ProprietaireRequest;
import com.immo.error.AuthException;
import com.immo.service.CloudinaryService;
import com.immo.service.ProprietaireService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationLoginController {

    private final ProprietaireService proprietaireService;
    private final AuthenticationManager authenticationManager;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody ProprietaireRequest proprietaire) {
        cloudinaryService.createProprietaire(proprietaire);
        AuthResponse saved = proprietaireService.register(proprietaire);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication =  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            if (authentication.isAuthenticated()) {
                Optional<AuthResponse> authResponse = proprietaireService.getLoginResponseByEmail(
                    loginRequest.getEmail()
                );
                return ResponseEntity.ok(authResponse);
            }
            throw new AuthException("email ou password incorrect");
        } catch (Exception e) {
            throw new AuthException("email ou password incorrect");
        }
    }
}
