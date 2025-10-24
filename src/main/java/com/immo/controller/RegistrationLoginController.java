package com.immo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.immo.config.JwtUtils;
import com.immo.dto.ProprietaireRequest;
import com.immo.dto.ProprietaireResponse;
import com.immo.dto.UtilisateurRequest;
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
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<ProprietaireResponse> register(@Valid @RequestBody ProprietaireRequest proprietaire) {
        cloudinaryService.createProprietaire(proprietaire);
        ProprietaireResponse saved = proprietaireService.register(proprietaire);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UtilisateurRequest utilisateur) {
        try {
            Authentication authentication =  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    utilisateur.getEmail(),
                    utilisateur.getPassword()
                )
            );
            if (authentication.isAuthenticated()) {

                Optional<ProprietaireResponse> proprietaire = proprietaireService.getProprietaireByEmail(
                    utilisateur.getEmail()
                );

                Map<String, Object> authData = new HashMap<>();
                authData.put("proprietaire", proprietaire.get());
                authData.put("token", jwtUtils.generateToken(utilisateur.getEmail()));
                authData.put("type", "Bearer");
                return ResponseEntity.ok(authData);
            }
            throw new AuthException("email ou password incorrect");
        } catch (Exception e) {
            throw new AuthException("email ou password incorrect");
        }
    }
}
