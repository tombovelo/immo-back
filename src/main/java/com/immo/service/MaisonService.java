package com.immo.service;

import com.immo.dto.MaisonRequest;
import com.immo.dto.MaisonRequestUser;
import com.immo.dto.MaisonResponse;
import com.immo.error.NotFoundException;
import com.immo.model.Maison;
import com.immo.model.Proprietaire;
import com.immo.model.TypeTransaction;
import com.immo.repository.MaisonRepository;
import com.immo.repository.ProprietaireRepository;
import com.immo.repository.TypeTransactionRepository;
import com.immo.utils.Utils;

import lombok.RequiredArgsConstructor;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.immo.error.FileUploadException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaisonService {

    private final MaisonRepository repository;
    private final ProprietaireRepository proprietaireRepository;
    private final TypeTransactionRepository typeTransactionRepository;
    private final OutboxEventService outboxEventService;

    public List<MaisonResponse> findAll() {
        return repository.findAllWithProprietaire()
                .stream()
                .map(Utils::convertToResponse) // référence de méthode
                .collect(Collectors.toList());
    }

    public Optional<MaisonResponse> findResponseById(Long id) {
        return repository.findByIdWithProprietaire(id)
                .map(Utils::convertToResponse);
    }

    public Optional<Maison> findById(Long id) {
        return repository.findByIdWithProprietaire(id);
    }

    // Gardez les méthodes originales si besoin
    public List<Maison> findAllWithoutProprietaire() {
        return repository.findAll();
    }

    public Optional<Maison> findByIdWithoutProprietaire(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<MaisonResponse> searchMaisons(
            String adresse,
            String ville,
            Double minPrix,
            Double maxPrix,
            Long typeTransactionId,
            Integer minPieces,
            Integer maxPieces,
            Long proprietaireId,
            Boolean visible,
            Double latitude,
            Double longitude,
            Double distanceKm) {

        List<Maison> maisons = repository.searchMaisons(
                adresse, ville, minPrix, maxPrix, 
                typeTransactionId,minPieces, maxPieces, proprietaireId, 
                visible, latitude, longitude, distanceKm
            );

        return maisons.stream()
                .map(Utils::convertToResponse)
                .collect(Collectors.toList());
    }

    /////////////////// NOUVEAU METHODE ////////////////////////////////////

    @Transactional(rollbackFor = Exception.class)
    public MaisonResponse createMaison(MaisonRequestUser request, Long userId) {
        try {
            // 1. Trouver le Proprietaire correspondant à l'utilisateur connecté (la source de vérité)
            Proprietaire proprietaire = proprietaireRepository.findByUtilisateur_Id(userId)
                    .orElseThrow(
                            () -> new NotFoundException("Aucun propriétaire associé à l'utilisateur avec l'ID: " + userId));

            // 2. Trouver le TypeTransaction (vient de la requête)
            TypeTransaction typeTransaction = typeTransactionRepository.findById(request.getTypeTransactionId())
                    .orElseThrow(() -> new NotFoundException(
                            "Type de transaction non trouvé avec id: " + request.getTypeTransactionId()));

            // 3. Créer et mapper la nouvelle maison
            Maison maison = new Maison();
            maison.setAdresse(request.getAdresse());
            maison.setVille(request.getVille());
            maison.setCodePostal(request.getCodePostal());
            maison.setNombrePieces(request.getNombrePieces());
            maison.setPrix(request.getPrix());
            maison.setDescription(request.getDescription());
            // maison.setVisible(request.getVisible() != null ? request.getVisible() :
            // true);
            Point point = Utils.convertToPoint(request.getLongitude(), request.getLatitude());
            maison.setCoordinate(point);

            // 4. Associer le bon propriétaire (trouvé via le token) et le type
            maison.setProprietaire(proprietaire);
            maison.setTypeTransaction(typeTransaction);

            // 5. Sauvegarder la maison
            Maison savedMaison = repository.save(maison);

            // 6. Gérer l'upload de la photo via un événement outbox
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("maisonId", savedMaison.getId());
                payload.put("fileContent", request.getFile().getBytes());
                outboxEventService.createAndSaveEvent("MAISON_PHOTO_UPLOAD_REQUESTED", payload);
            }
            
            return Utils.convertToResponse(savedMaison);
        } catch (IOException e) {
            throw new FileUploadException("Erreur lors de la lecture du fichier pour la création de la maison.", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public MaisonResponse createMaisonForAdmin(Long proprietaireId, MaisonRequest request) {
        try {
            // 1. Trouver le Proprietaire via l'ID fourni dans la requête (logique pour
            // l'admin)
            Proprietaire proprietaire = proprietaireRepository.findById(proprietaireId)
                    .orElseThrow(() -> new NotFoundException(
                            "Aucun propriétaire trouvé avec l'ID: " + proprietaireId));

            // 2. Trouver le TypeTransaction (vient de la requête)
            TypeTransaction typeTransaction = typeTransactionRepository.findById(request.getTypeTransactionId())
                    .orElseThrow(() -> new NotFoundException(
                            "Type de transaction non trouvé avec id: " + request.getTypeTransactionId()));

            // 3. Créer et mapper la nouvelle maison
            Maison maison = new Maison();
            maison.setAdresse(request.getAdresse());
            maison.setVille(request.getVille());
            maison.setCodePostal(request.getCodePostal());
            maison.setNombrePieces(request.getNombrePieces());
            maison.setPrix(request.getPrix());
            maison.setDescription(request.getDescription());
            Point point = Utils.convertToPoint(request.getLongitude(), request.getLatitude());
            maison.setCoordinate(point);
            maison.setProprietaire(proprietaire);
            maison.setTypeTransaction(typeTransaction);
            Maison savedMaison = repository.save(maison);
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("maisonId", savedMaison.getId());
                payload.put("fileContent", request.getFile().getBytes());
                outboxEventService.createAndSaveEvent("MAISON_PHOTO_UPLOAD_REQUESTED", payload);
            }
            return Utils.convertToResponse(savedMaison);
        } catch (IOException e) {
            throw new FileUploadException("Erreur lors de l'enregistrement de la maison", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public MaisonResponse updateMaison(Long id, MaisonRequest request) {
        try {
            // 1. Vérifier si la maison existe.
            Maison existing = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Aucune maison trouvée avec id : " + id));

            // 2. Mettre à jour les champs de la maison (uniquement si fournis)
            if (request.getAdresse() != null) existing.setAdresse(request.getAdresse());
            if (request.getVille() != null && !request.getVille().isBlank()) existing.setVille(request.getVille());
            if (request.getCodePostal() != null) existing.setCodePostal(request.getCodePostal());
            if (request.getNombrePieces() != null) existing.setNombrePieces(request.getNombrePieces());
            if (request.getPrix() != null) existing.setPrix(request.getPrix());
            if (request.getDescription() != null) existing.setDescription(request.getDescription());
            if (request.getVisible() != null) existing.setVisible(request.getVisible());
            if (request.getLatitude() != null && request.getLongitude() != null) {
                Point point = Utils.convertToPoint(request.getLongitude(), request.getLatitude());
                existing.setCoordinate(point);
            }

            // 3. Mettre à jour le type de transaction si fourni
            if (request.getTypeTransactionId() != null) {
                TypeTransaction typeTransaction = typeTransactionRepository.findById(request.getTypeTransactionId())
                        .orElseThrow(() -> new NotFoundException(
                                "Type de transaction non trouvé avec id : " + request.getTypeTransactionId()));
                existing.setTypeTransaction(typeTransaction);
            }

            // 4. Gérer la mise à jour de la photo de la maison
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                // Si une ancienne photo existe, on crée un événement pour la supprimer
                if (existing.getCloudinaryPublicId() != null && !existing.getCloudinaryPublicId().isEmpty()) {
                    Map<String, Object> deletePayload = new HashMap<>();
                    deletePayload.put("publicId", existing.getCloudinaryPublicId());
                    outboxEventService.createAndSaveEvent("PHOTO_DELETED", deletePayload);
                }

                // On crée l'événement pour uploader la nouvelle photo
                Map<String, Object> uploadPayload = new HashMap<>();
                uploadPayload.put("maisonId", existing.getId());
                uploadPayload.put("fileContent", request.getFile().getBytes());
                outboxEventService.createAndSaveEvent("MAISON_PHOTO_UPLOAD_REQUESTED", uploadPayload);
            }

            // 5. Sauvegarde et retour de la réponse
            Maison saved = repository.save(existing);
            return Utils.convertToResponse(saved);
        } catch (IOException e) {
            throw new FileUploadException("Erreur lors de la mise à jour de la maison", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public MaisonResponse updateMaisonForCurrentUser(Long id, MaisonRequestUser request) {
        try {
            // 1. Vérifier si la maison existe.
            Maison existing = repository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Aucune maison trouvée avec id : " + id));

            // 2. Mettre à jour les champs de la maison (uniquement si fournis)
            if (request.getAdresse() != null) existing.setAdresse(request.getAdresse());
            if (request.getVille() != null && !request.getVille().isBlank()) existing.setVille(request.getVille());
            if (request.getCodePostal() != null) existing.setCodePostal(request.getCodePostal());
            if (request.getNombrePieces() != null) existing.setNombrePieces(request.getNombrePieces());
            if (request.getPrix() != null) existing.setPrix(request.getPrix());
            if (request.getDescription() != null) existing.setDescription(request.getDescription());
            if (request.getLatitude() != null && request.getLongitude() != null) {
                Point point = Utils.convertToPoint(request.getLongitude(), request.getLatitude());
                existing.setCoordinate(point);
            }

            // 3. Mettre à jour le type de transaction si fourni
            if (request.getTypeTransactionId() != null) {
                TypeTransaction typeTransaction = typeTransactionRepository.findById(request.getTypeTransactionId())
                        .orElseThrow(() -> new NotFoundException(
                                "Type de transaction non trouvé avec id : " + request.getTypeTransactionId()));
                existing.setTypeTransaction(typeTransaction);
            }

            // 4. Gérer la mise à jour de la photo de la maison
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                // Si une ancienne photo existe, on crée un événement pour la supprimer
                if (existing.getCloudinaryPublicId() != null && !existing.getCloudinaryPublicId().isEmpty()) {
                    Map<String, Object> deletePayload = new HashMap<>();
                    deletePayload.put("publicId", existing.getCloudinaryPublicId());
                    outboxEventService.createAndSaveEvent("PHOTO_DELETED", deletePayload);
                }

                // On crée l'événement pour uploader la nouvelle photo
                Map<String, Object> uploadPayload = new HashMap<>();
                uploadPayload.put("maisonId", existing.getId());
                uploadPayload.put("fileContent", request.getFile().getBytes());
                outboxEventService.createAndSaveEvent("MAISON_PHOTO_UPLOAD_REQUESTED", uploadPayload);
            }

            // 5. Sauvegarde et retour de la réponse
            Maison saved = repository.save(existing);
            return Utils.convertToResponse(saved);
        } catch (IOException e) {
            throw new FileUploadException("Erreur lors de la mise à jour de la maison", e);
        }
    }

    public boolean isOwner(Long maisonId, Long userId) {
        // 1. Trouve la maison par son ID.
        return repository.findById(maisonId)
                // 2. Si la maison est trouvée, on exécute la logique de vérification.
                .map(maison -> maison.getProprietaire().getUtilisateur().getId().equals(userId))
                // 3. Si la maison n'est pas trouvée, on retourne false par sécurité.
                .orElse(false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. Trouver la maison à supprimer.
        Maison maison = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aucune maison trouvée avec l'ID: " + id));
        Map<String, Object> payload = new HashMap<>();
        payload.put("dossier", maison.getDossier());
        payload.put("maisonId", maison.getId()); // Pour le log
        outboxEventService.createAndSaveEvent("MAISON_FOLDER_DELETED", payload);
        repository.delete(maison);
    }

    public List<MaisonResponse> getMaisonForCurrentUser(Long userId) {
        // 1. Trouver le Proprietaire correspondant à l'utilisateur connecté
        Proprietaire proprietaire = proprietaireRepository.findByUtilisateur_Id(userId)
                .orElseThrow(() -> new NotFoundException("Aucun propriétaire associé à l'utilisateur avec l'ID: " + userId));

        // 2. Utiliser la nouvelle méthode optimisée du repository qui récupère
        // directement les maisons du propriétaire.
        // Le JOIN FETCH garantit que les informations du propriétaire sont chargées en une seule requête.
        List<Maison> maisons = repository.findAllByProprietaireId(proprietaire.getId());

        // 3. Mapper les entités Maison en DTO MaisonResponse
        return maisons.stream()
                .map(Utils::convertToResponse)
                .collect(Collectors.toList());
    }
}
