package com.immo.service; // Assurez-vous que le nom du package est correct

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.immo.error.NotFoundException;
import com.immo.model.Album;
import com.immo.model.Maison;
import com.immo.model.OutboxEvent;
import com.immo.model.Photo;
import com.immo.model.Proprietaire;
import com.immo.repository.MaisonRepository;
import com.immo.repository.OutboxEventRepository;
import com.immo.repository.PhotoRepository;
import com.immo.repository.ProprietaireRepository;
import com.immo.repository.AlbumRepository;
import com.immo.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessorService {

    private static final int BATCH_SIZE = 5; // Taille du lot à traiter

    private final OutboxEventRepository outboxEventRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;
    private final Executor outboxEventExecutor;
    private final PhotoRepository photoRepository;
    private final ProprietaireRepository proprietaireRepository;
    private final MaisonRepository maisonRepository;
    private final AlbumRepository albumRepository;


    @Scheduled(fixedRate = 30000) // Exécute toutes les 30 secondes
    public void processOutboxEvents() {
        // 1. Récupérer un lot d'événements (pagination)
        Page<OutboxEvent> eventPage = outboxEventRepository.findByProcessedFalse(PageRequest.of(0, BATCH_SIZE));
        List<OutboxEvent> events = eventPage.getContent();

        if (events.isEmpty()) {
            return; // Rien à faire
        }

        log.info("Traitement de {} événement(s) en attente...", events.size());

        // 2. Traiter les événements en parallèle
        List<CompletableFuture<OutboxEvent>> futures = events.stream()
                .map(event -> CompletableFuture.supplyAsync(() -> {
                    try {
                        processEvent(event);
                        return event; // Retourner l'événement en cas de succès
                    } catch (Exception e) {
                        log.error(
                                "Échec du traitement de l'événement Outbox {}. Il sera réessayé plus tard. Erreur: {}",
                                event.getId(), e.getMessage());
                        return null; // Retourner null en cas d'échec
                    }
                }, outboxEventExecutor))
                .collect(Collectors.toList());

        // Attendre que tous les traitements soient terminés
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 3. Mettre à jour en base de données en un seul lot
        List<OutboxEvent> processedEvents = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // Filtrer les événements qui ont échoué (null)
                .peek(event -> event.setProcessed(true))
                .collect(Collectors.toList());

        if (!processedEvents.isEmpty()) {
            outboxEventRepository.saveAll(processedEvents);
            log.info("{} événement(s) marqués comme traités.", processedEvents.size());
        }
    }

    // La méthode processEvent reste inchangée
    private void processEvent(OutboxEvent event) throws IOException {
        switch (event.getEventType()) {
            case "PROPRIETAIRE_FOLDER_DELETED":
                Proprietaire deletedProprietaire = objectMapper.readValue(event.getPayload(), Proprietaire.class);
                if (deletedProprietaire.getDossier() != null && !deletedProprietaire.getDossier().isEmpty()) {
                    String dossierProprietaire = Utils.getRootFolder(deletedProprietaire.getDossier());
                    cloudinaryService.deleteFolder(dossierProprietaire);
                    log.info("Événement PROPRIETAIRE_FOLDER_DELETED traité pour proprietaire ID: {}.",
                            deletedProprietaire.getId());
                }
                break;

            case "PROPRIETAIRE_PHOTO_UPLOAD_REQUESTED":
                Map<String, Object> proprietairePayload = objectMapper.readValue(event.getPayload(),
                        new TypeReference<>() {
                        });
                Long proprietaireId = ((Number) proprietairePayload.get("proprietaireId")).longValue();
                byte[] proprietaireFileContent = Base64.getDecoder()
                        .decode((String) proprietairePayload.get("fileContent"));

                Proprietaire proprietaireToUpdate = proprietaireRepository.findById(proprietaireId)
                        .orElseThrow(() -> new NotFoundException(
                                "Proprietaire non trouvé pour l'ID: " + proprietaireId + ". L'upload est annulé."));

                Map<String, Object> proprietaireUploadResult = cloudinaryService
                        .uploadPhoto(
                                proprietaireFileContent,
                                cloudinaryService.getBasePath() + "/" + proprietaireToUpdate.getNom() + "_"
                                        + proprietaireToUpdate.getId() + "/profile");

                proprietaireToUpdate.setUrlProfile(proprietaireUploadResult.get("secure_url").toString());
                proprietaireToUpdate.setCloudinaryPublicId(proprietaireUploadResult.get("public_id").toString());
                String dossierProprietaire = Utils
                        .getRootFolder(proprietaireUploadResult.get("asset_folder").toString());
                proprietaireToUpdate.setDossier(dossierProprietaire);
                proprietaireRepository.save(proprietaireToUpdate);
                log.info(
                        "Événement PROPRIETAIRE_PHOTO_UPLOAD_REQUESTED traité. Photo de profil pour Proprietaire ID {} uploadée sur Cloudinary.",
                        proprietaireId);
                break;

            case "PROPRIETAIRE_PHOTO_DELETE_REQUESTED":
                Map<String, Object> proprietaireIdPyload = objectMapper.readValue(event.getPayload(),
                        new TypeReference<>() {
                        });
                Long proprietairId = ((Number) proprietaireIdPyload.get("proprietaireId")).longValue();
                Proprietaire proprietaire = proprietaireRepository.findById(proprietairId)
                        .orElseThrow(() -> new NotFoundException(
                                "Proprietaire non trouvé pour l'ID: " + proprietairId + ". L'upload est annulé."));
                cloudinaryService.deletePhoto(proprietaire.getCloudinaryPublicId());
                log.info("Événement PROPRIETAIRE_PHOTO_DELETE_REQUESTED traité pour proprietaire ID: {}.",
                        proprietaire.getId());
                break;

            case "MAISON_FOLDER_DELETED":
                Map<String, Object> maisonPayloads = objectMapper.readValue(event.getPayload(), new TypeReference<>() {
                });
                String dossierMaisons = (String) maisonPayloads.get("dossier");
                Long maisonIdForLog = ((Number) maisonPayloads.get("maisonId")).longValue();

                if (dossierMaisons != null && !dossierMaisons.isEmpty()) {
                    cloudinaryService.deleteFolder(dossierMaisons);
                    log.info("Événement MAISON_FOLDER_DELETED traité. Dossier {} pour la maison ID {} supprimé de Cloudinary.",
                            dossierMaisons, maisonIdForLog);
                }
                break;

            case "MAISON_PHOTO_UPLOAD_REQUESTED":
                Map<String, Object> maisonPayload = objectMapper.readValue(event.getPayload(), new TypeReference<>() {
                });
                Long maisonId = ((Number) maisonPayload.get("maisonId")).longValue();
                byte[] maisonFileContent = Base64.getDecoder().decode((String) maisonPayload.get("fileContent"));
                Maison maisonToUpdate = maisonRepository.findById(maisonId)
                        .orElseThrow(() -> new NotFoundException(
                                "Maison non trouvée pour l'ID: " + maisonId + ". L'upload est annulé."));
                // Construire le dossier Cloudinary :
                // immobilier/proprietaire_{id}/maisons_{id}/image
                String cloudinaryFolder = String.format("%s/%s_%d/maisons_%d/image",
                        cloudinaryService.getBasePath(),
                        maisonToUpdate.getProprietaire().getNom(),
                        maisonToUpdate.getProprietaire().getId(),
                        maisonToUpdate.getId());

                Map<String, Object> maisonUploadResult = cloudinaryService.uploadPhoto(maisonFileContent,
                        cloudinaryFolder);

                maisonToUpdate.setCloudinaryUrl(maisonUploadResult.get("secure_url").toString());
                maisonToUpdate.setCloudinaryPublicId(maisonUploadResult.get("public_id").toString());
                String dossierMaison = Utils.getRootFolder(maisonUploadResult.get("asset_folder").toString());
                maisonToUpdate.setDossier(dossierMaison);

                maisonRepository.save(maisonToUpdate);
                log.info(
                        "Événement MAISON_PHOTO_UPLOAD_REQUESTED traité. Photo pour la maison ID {} uploadée sur Cloudinary.",
                        maisonId);
                break;

            case "ALBUM_CREATED":
                Map<String, Object> albumPayload = objectMapper.readValue(event.getPayload(), new TypeReference<>() {});
                Long albumId = ((Number) albumPayload.get("albumId")).longValue();

                Album createdAlbum = albumRepository.findById(albumId)
                    .orElseThrow(() -> new NotFoundException("Album non trouvé avec l'ID: " + albumId + " lors du traitement de l'événement."));
                
                // Construire le path Cloudinary
                String path = String.format(
                    "%s/%s_%d/maisons_%d/%s_%d", cloudinaryService.getBasePath(),
                    createdAlbum.getMaison().getProprietaire().getNom(),
                    createdAlbum.getMaison().getProprietaire().getId(),
                    createdAlbum.getMaison().getId(),
                    createdAlbum.getNomAlbum(), createdAlbum.getId());
                    
                // Mettre à jour l'album avec le path et sauvegarder
                createdAlbum.setPath(path);
                albumRepository.save(createdAlbum);
                // Créer le dossier sur Cloudinary
                cloudinaryService.createFolder(path);
                log.info("Événement ALBUM_CREATED traité. Dossier Cloudinary créé pour l'album ID: {}.", createdAlbum.getId());
                break;

            case "ALBUM_DELETED":
                Album deletedAlbum = objectMapper.readValue(event.getPayload(), Album.class);
                cloudinaryService.deleteAlbum(deletedAlbum.getPath());
                log.info("Événement ALBUM_DELETED traité pour l'album ID: {}", deletedAlbum.getId());
                break;

            case "PHOTO_UPLOAD_REQUESTED":
                // 1. Désérialiser le payload en une Map
                Map<String, Object> payload = objectMapper.readValue(event.getPayload(), new TypeReference<>() {
                });
                Long photoId = ((Number) payload.get("photoId")).longValue();
                // Jackson encode les byte[] en Base64 (String), il faut donc les décoder.
                byte[] fileContent = Base64.getDecoder().decode((String) payload.get("fileContent"));

                // 2. Récupérer l'entité Photo depuis la base de données
                Photo photoToUpload = photoRepository.findById(photoId)
                        .orElseThrow(() -> new RuntimeException(
                                "Photo non trouvée pour l'ID: " + photoId + ". L'upload est annulé."));

                // 3. Uploader le contenu binaire vers Cloudinary
                Map<String, Object> uploadResult = cloudinaryService.uploadPhoto(fileContent, photoToUpload.getAlbum());

                // 4. Mettre à jour l'entité avec les informations de Cloudinary
                photoToUpload.setCloudinaryUrl(uploadResult.get("url").toString());
                photoToUpload.setCloudinaryPublicId(uploadResult.get("public_id").toString());
                photoRepository.save(photoToUpload);
                log.info("Événement PHOTO_UPLOAD_REQUESTED traité. Photo ID {} uploadée sur Cloudinary.", photoId);
                break;

            case "PHOTO_DELETED":
                Map<String, String> deletePayload = objectMapper.readValue(event.getPayload(), new TypeReference<>() {
                });
                String publicIdToDelete = deletePayload.get("publicId");
                if (publicIdToDelete != null && !publicIdToDelete.isEmpty()) {
                    cloudinaryService.deletePhoto(publicIdToDelete);
                    log.info("Événement PHOTO_DELETED traité pour le public_id: {}", publicIdToDelete);
                }
                break;
        }
    }
}
