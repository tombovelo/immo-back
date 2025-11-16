package com.immo.service;

import com.immo.config.JwtUtils;
import com.immo.dto.AuthResponse;
import com.immo.dto.ProfileAdminRequest;
import com.immo.dto.ProfileUserRequest;
import com.immo.dto.ProprietaireAdminRequest;
import com.immo.dto.ProprietaireRequest;
import com.immo.dto.ProprietaireResponse;
import com.immo.dto.UserProfile;
import com.immo.error.FileUploadException;
import com.immo.error.NotFoundException;
import com.immo.model.Proprietaire;
import com.immo.model.Utilisateur;
import com.immo.repository.ProprietaireRepository;
import com.immo.repository.UtilisateurRepository;
import com.immo.utils.Utils;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProprietaireService {

    private final ProprietaireRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OutboxEventService outboxEventService;
    private final UtilisateurRepository utilisateurRepository;

    public List<ProprietaireResponse> findAll() {
        return repository.findAll().stream()
                .map(Utils::convertToResponse) // Ajouter "this::"
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

    @Transactional(rollbackFor = Exception.class)
    public AuthResponse register(ProprietaireRequest proprietaireRequest) {
        try {
            String token = null;
            Proprietaire proprietaire = new Proprietaire();
            Utilisateur utilisateur = new Utilisateur();

            // Remplir utilisateur
            if (proprietaireRequest.getUtilisateur() != null) {
                String email = proprietaireRequest.getUtilisateur().getEmail();
                // ✅ VALIDATION : Vérifier si l'email existe déjà
                if (utilisateurRepository.existsByEmail(email)) {
                    throw new RuntimeException("Un utilisateur avec l'email '" + email + "' existe déjà.");
                }
                token = jwtUtils.generateToken(email);
                utilisateur.setEmail(email);
                utilisateur.setPassword(passwordEncoder.encode(proprietaireRequest.getUtilisateur().getPassword()));
            }

            // Associer utilisateur au propriétaire
            proprietaire.setUtilisateur(utilisateur);

            // Remplir les infos du propriétaire
            proprietaire.setAdresse(proprietaireRequest.getAdresse());
            proprietaire.setNom(proprietaireRequest.getNom());
            proprietaire.setPrenom(proprietaireRequest.getPrenom());
            proprietaire.setTelephone(proprietaireRequest.getTelephone());

            // 1. Sauvegarder le propriétaire pour obtenir un ID
            Proprietaire saved = repository.save(proprietaire);

            // 2. Gérer l'upload de la photo de profil via le pattern Outbox
            if (proprietaireRequest.getFile() != null && !proprietaireRequest.getFile().isEmpty()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("proprietaireId", saved.getId());
                payload.put("fileContent", proprietaireRequest.getFile().getBytes());
                // On crée une "promesse" d'upload pour la photo de profil
                outboxEventService.createAndSaveEvent("PROPRIETAIRE_PHOTO_UPLOAD_REQUESTED", payload);
            }
            UserProfile userProfile = Utils.mapToUserProfile(saved);
            AuthResponse authResponse = Utils.mapToAuthResponse(userProfile, "Bearer", token);
            return authResponse;
        } catch (IOException e) {
            throw new FileUploadException("Erreur lors de l'enregistrement du propriétaire", e);
        }

    }

    public Optional<ProprietaireResponse> getProprietaireByEmail(String email) {
        return repository.findByUtilisateur_Email(email)
                .map(Utils::convertToResponse);
    }

    public AuthResponse getLoginResponseByEmail(String email) {
        Proprietaire proprietaire = repository.findByUtilisateur_Email(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec l'email: " + email));
        String token = jwtUtils.generateToken(email);
        UserProfile userProfile = Utils.mapToUserProfile(proprietaire);
        AuthResponse authResponse = Utils.mapToAuthResponse(userProfile, "Bearer", token);
        return authResponse;
    }

    ////////////////////////////////// NOUVEAU METHODE
    ////////////////////////////////// //////////////////////////////////

    public Optional<ProprietaireResponse> getMyProfile(Long userId) {
        return repository.findByUtilisateur_Id(userId)
                .map(Utils::convertToResponse); // On mappe directement vers le DTO
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthResponse update(Long userId, ProprietaireRequest request) {
        try {

            // 1. Trouver le propriétaire via l'ID de l'utilisateur authentifié
            Proprietaire existingProprietaire = repository.findByUtilisateur_Id(userId)
                    .orElseThrow(() -> new NotFoundException(
                            "Aucun profil propriétaire associé à l'utilisateur avec l'ID: " + userId));

            // 2. Mettre à jour les informations du profil Proprietaire (uniquement les
            // champs fournis)
            if (request.getNom() != null && !request.getNom().isBlank()) {
                existingProprietaire.setNom(request.getNom());
            }
            if (request.getPrenom() != null) {
                existingProprietaire.setPrenom(request.getPrenom());
            }
            if (request.getAdresse() != null) {
                existingProprietaire.setAdresse(request.getAdresse());
            }
            if (request.getTelephone() != null && !request.getTelephone().isBlank()) {
                existingProprietaire.setTelephone(request.getTelephone());
            }

            // 3. Mettre à jour les informations de l'entité Utilisateur
            Utilisateur utilisateur = existingProprietaire.getUtilisateur();
            ProfileUserRequest userRequest = request.getUtilisateur();

            if (userRequest != null) {
                if (userRequest.getEmail() != null && !userRequest.getEmail().isBlank()) {
                    String newEmail = userRequest.getEmail();
                    if (!newEmail.equals(utilisateur.getEmail()) && utilisateurRepository.existsByEmail(newEmail)) {
                        throw new RuntimeException("Un utilisateur avec l'email '" + newEmail + "' existe déjà.");
                    }
                    utilisateur.setEmail(newEmail);
                }

                if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
                    utilisateur.setPassword(passwordEncoder.encode(userRequest.getPassword()));
                }
            }

            // 4. Gérer la mise à jour de la photo de profil
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                Map<String, Object> deletePayload = new HashMap<>();
                deletePayload.put("proprietaireId", existingProprietaire.getId());
                outboxEventService.createAndSaveEvent("PROPRIETAIRE_PHOTO_DELETE_REQUESTED", deletePayload);

                // On crée l'événement pour uploader la nouvelle photo
                Map<String, Object> uploadPayload = new HashMap<>();
                uploadPayload.put("proprietaireId", existingProprietaire.getId());
                uploadPayload.put("fileContent", request.getFile().getBytes());
                outboxEventService.createAndSaveEvent("PROPRIETAIRE_PHOTO_UPLOAD_REQUESTED", uploadPayload);
            }

            // 5. Sauvegarder l'entité mise à jour
            Proprietaire updatedProprietaire = repository.save(existingProprietaire);

            // 6. Générer un nouveau token et construire la réponse AuthResponse
            String token = jwtUtils.generateToken(updatedProprietaire.getUtilisateur().getEmail());
            UserProfile userProfile = Utils.mapToUserProfile(updatedProprietaire);
            AuthResponse authResponse = Utils.mapToAuthResponse(userProfile, "Bearer", token);
            return authResponse;

        } catch (IOException e) {
            throw new FileUploadException("Erreur lors de la mise à jour du profil propriétaire", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ProprietaireResponse updateAdmin(Long proprietaireId, ProprietaireAdminRequest request) {
        try {
            // 1. Trouver le propriétaire via l'ID de l'utilisateur authentifié
            // 1. Trouver le propriétaire via son propre ID
            Proprietaire existingProprietaire = repository.findById(proprietaireId)
                    .orElseThrow(() -> new NotFoundException(
                            "Aucun propriétaire trouvé avec l'ID: " + proprietaireId));

            // 2. Mettre à jour les informations du profil Proprietaire (uniquement les
            // champs fournis)
            if (request.getNom() != null && !request.getNom().isBlank()) {
                existingProprietaire.setNom(request.getNom());
            }
            if (request.getPrenom() != null) {
                existingProprietaire.setPrenom(request.getPrenom());
            }
            if (request.getAdresse() != null) {
                existingProprietaire.setAdresse(request.getAdresse());
            }
            if (request.getTelephone() != null && !request.getTelephone().isBlank()) {
                existingProprietaire.setTelephone(request.getTelephone());
            }

            // 3. Mettre à jour les informations de l'entité Utilisateur
            Utilisateur utilisateur = existingProprietaire.getUtilisateur();
            ProfileAdminRequest userRequest = request.getUtilisateur();

            if (userRequest != null) {
                if (userRequest.getEmail() != null && !userRequest.getEmail().isBlank()) {
                    String newEmail = userRequest.getEmail();
                    if (!newEmail.equals(utilisateur.getEmail()) && utilisateurRepository.existsByEmail(newEmail)) {
                        throw new RuntimeException("Un utilisateur avec l'email '" + newEmail + "' existe déjà.");
                    }
                    utilisateur.setEmail(newEmail);
                }

                if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
                    utilisateur.setPassword(passwordEncoder.encode(userRequest.getPassword()));
                }

                // AJOUTEZ CETTE PARTIE
                if (userRequest.getRole() != null && !userRequest.getRole().isBlank()) {
                    utilisateur.setRole(userRequest.getRole());
                }
            }

            // 4. Gérer la mise à jour de la photo de profil
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                Map<String, Object> deletePayload = new HashMap<>();
                deletePayload.put("proprietaireId", existingProprietaire.getId());
                outboxEventService.createAndSaveEvent("PROPRIETAIRE_PHOTO_DELETE_REQUESTED", deletePayload);

                // On crée l'événement pour uploader la nouvelle photo
                Map<String, Object> uploadPayload = new HashMap<>();
                uploadPayload.put("proprietaireId", existingProprietaire.getId());
                uploadPayload.put("fileContent", request.getFile().getBytes());
                outboxEventService.createAndSaveEvent("PROPRIETAIRE_PHOTO_UPLOAD_REQUESTED", uploadPayload);
            }

            // 5. Sauvegarder l'entité mise à jour
            Proprietaire updatedProprietaire = repository.save(existingProprietaire);

            // 6. Retourner la réponse DTO
            return Utils.convertToResponse(updatedProprietaire);
        } catch (IOException e) {
            throw new FileUploadException("Erreur lors de la mise à jour du profil propriétaire", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public void deleteProprietaire(Long proprietaireId) {
        // On ne supprime plus directement, on crée un événement Outbox
        Proprietaire proprietaire = repository.findById(proprietaireId)
                .orElseThrow(() -> new NotFoundException("Aucun propriétaire trouvé avec id : " + proprietaireId));

        outboxEventService.createAndSaveEvent("PROPRIETAIRE_FOLDER_DELETED", proprietaire);
        // Supprimer le propriétaire de la base de données locale
        repository.delete(proprietaire);
    }

}
