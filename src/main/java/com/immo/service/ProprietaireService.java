package com.immo.service;

import com.immo.config.JwtUtils;
import com.immo.dto.AuthResponse;
import com.immo.dto.ProprietaireRequest;
import com.immo.dto.ProprietaireResponse;
import com.immo.dto.UserProfile;
import com.immo.error.NotFoundException;
import com.immo.model.Proprietaire;
import com.immo.model.Utilisateur;
import com.immo.repository.ProprietaireRepository;
import com.immo.utils.Utils;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProprietaireService {

    private final ProprietaireRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public List<ProprietaireResponse> findAll() {
        return repository.findAll().stream()
            .map(Utils::convertToResponse)  // Ajouter "this::"
            .collect(Collectors.toList());
    }

    public Optional<Proprietaire> findById(Long id) { 
        return repository.findById(id); 
    }

    public Optional<ProprietaireResponse> findResponseById(Long id) {
        return repository.findById(id)
            .map(Utils::convertToResponse);
    }

    public void deleteById(Long id) { 
        repository.deleteById(id); 
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public AuthResponse register(ProprietaireRequest proprietaireRequest) {
        
        Proprietaire proprietaire = new Proprietaire();
        Utilisateur utilisateur = new Utilisateur();
        
        // Remplir utilisateur
        utilisateur.setEmail(proprietaireRequest.getUtilisateur().getEmail());

        utilisateur.setPassword(
            passwordEncoder.encode(proprietaireRequest.getUtilisateur().getPassword())
        );

        if (proprietaireRequest.getUtilisateur().getRole() != null && 
            !proprietaireRequest.getUtilisateur().getRole().isEmpty()) {
            utilisateur.setRole(proprietaireRequest.getUtilisateur().getRole());
        }

        // Associer utilisateur au propriétaire
        proprietaire.setUtilisateur(utilisateur);

        // Remplir les infos du propriétaire
        proprietaire.setAdresse(proprietaireRequest.getAdresse());
        proprietaire.setNom(proprietaireRequest.getNom());
        proprietaire.setPrenom(proprietaireRequest.getPrenom());
        proprietaire.setTelephone(proprietaireRequest.getTelephone());

        Proprietaire saved = repository.save(proprietaire);
        UserProfile userProfile = Utils.mapToUserProfile(saved);
        AuthResponse authResponse = Utils.mapToAuthResponse(userProfile, "Bearer", null);
        return authResponse;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public ProprietaireResponse update(Long id, ProprietaireRequest request) {
        Proprietaire existing = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Aucun propriétaire trouvé avec id : " + id));

        // Mettre à jour les champs simples
        existing.setNom(request.getNom());
        existing.setPrenom(request.getPrenom());
        existing.setAdresse(request.getAdresse());
        existing.setTelephone(request.getTelephone());

        // Mettre à jour l'utilisateur associé
        Utilisateur utilisateur = existing.getUtilisateur();
        if (utilisateur == null) {
            utilisateur = new Utilisateur();
            existing.setUtilisateur(utilisateur);
        }

        // ⚠️ Conserver l'id existant
        utilisateur.setEmail(request.getUtilisateur().getEmail());

        // Ne mettre à jour le mot de passe que si l'utilisateur fournit un nouveau mot de passe
        if (request.getUtilisateur().getPassword() != null && !request.getUtilisateur().getPassword().isEmpty()) {
            utilisateur.setPassword(passwordEncoder.encode(request.getUtilisateur().getPassword()));
        }

        if (request.getUtilisateur().getRole() != null && !request.getUtilisateur().getRole().isEmpty()) {
            utilisateur.setRole(request.getUtilisateur().getRole());
        }

        // Sauvegarder l'entité complète
        Proprietaire updated = repository.save(existing);
        return Utils.convertToResponse(updated);
    }

    public Optional<ProprietaireResponse> getProprietaireByEmail(String email) {
        return repository.findByUtilisateur_Email(email)
            .map(Utils::convertToResponse);
    }

    public Optional<AuthResponse> getLoginResponseByEmail(String email) {
        return repository.findByUtilisateur_Email(email)
            .map(proprietaire -> {
                String token = jwtUtils.generateToken(email);
                UserProfile userProfile = Utils.mapToUserProfile(proprietaire);
                AuthResponse authResponse = Utils.mapToAuthResponse(userProfile, "Bearer", token);
                return authResponse;
            });
    }



}

