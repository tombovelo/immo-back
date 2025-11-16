package com.immo.service;

import com.immo.dto.PhotoResponse;
import com.immo.dto.PhotoUploadRequest;
import com.immo.error.FileUploadException;
import com.immo.error.NotFoundException;
import com.immo.model.Album;
import com.immo.model.Photo;
import com.immo.repository.PhotoRepository;
import com.immo.utils.Utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository repository;
    private final AlbumService albumService;
    private final OutboxEventService outboxEventService;

    public List<PhotoResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(Utils::convertToResponse) // référence de méthode
                .collect(Collectors.toList());
    }

    public Optional<Photo> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<PhotoResponse> findResponseById(Long id) {
        return repository.findById(id)
                .map(Utils::convertToResponse);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public PhotoResponse save(Photo photo) {
        return Utils.convertToResponse(repository.save(photo));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    //////////////////////// NOUVEAU METHODE ///////////////////

    public boolean isOwner(Long photoId, Long userId) {
        return repository.findById(photoId)
                .map(photo -> photo.getAlbum().getMaison().getProprietaire().getUtilisateur().getId().equals(userId))
                .orElse(false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long photoId) {
        Photo photo = repository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Aucune photo trouvée avec id : " + photoId));
        Map<String, String> deletePayload = Map.of("publicId", photo.getCloudinaryPublicId());
        outboxEventService.createAndSaveEvent("PHOTO_DELETED", deletePayload);
        repository.delete(photo);
    }

    @Transactional(rollbackFor = Exception.class)
    public PhotoResponse updatePhoto(Long photoId, MultipartFile file, String description, Integer ordre, Long albumId) {
        try {
            // 1. Trouver la photo existante
            Photo existingPhoto = repository.findById(photoId)
                    .orElseThrow(() -> new NotFoundException("Aucune photo trouvée avec id : " + photoId));

            // --- Partie 1 : Mise à jour des métadonnées (toujours effectuée) ---
            boolean isModified = false;

            // --- Partie 1 : Mise à jour des métadonnées ---
            if (description != null && !description.equals(existingPhoto.getDescription())) {
                existingPhoto.setDescription(description);
                isModified = true;
            }

            if (ordre != null && !ordre.equals(existingPhoto.getOrdre())) {
                existingPhoto.setOrdre(ordre);
                isModified = true;
            }

            if (albumId != null && !albumId.equals(existingPhoto.getAlbum().getId())) {
                Album newAlbum = albumService.findById(albumId)
                        .orElseThrow(() -> new NotFoundException("Album non trouvé avec l'ID: " + albumId));
                existingPhoto.setAlbum(newAlbum);
                isModified = true;
            }

            // --- Partie 2 : Remplacement du fichier (uniquement si un nouveau fichier est
            // fourni) ---
            if (file != null && !file.isEmpty()) {
                // 2a. Créer un événement pour supprimer l'ANCIEN fichier de Cloudinary, s'il
                // existe.
                if (existingPhoto.getCloudinaryPublicId() != null && !existingPhoto.getCloudinaryPublicId().isEmpty()) {
                    Map<String, String> deletePayload = Map.of("publicId", existingPhoto.getCloudinaryPublicId());
                    outboxEventService.createAndSaveEvent("PHOTO_FILE_TO_DELETE", deletePayload);
                }

                // 2b. Mettre à jour le nom du fichier et réinitialiser les champs Cloudinary
                Map<String, String> fileNameParts = Utils.parseFilename(file.getOriginalFilename());
                existingPhoto.setNomFichier(fileNameParts.get("fullName"));
                existingPhoto.setCloudinaryPublicId(null);
                existingPhoto.setCloudinaryUrl(null);
                isModified = true;
            }
            // --- Partie 3 : Sauvegarder si des modifications ont eu lieu ---
            if (isModified) {
                // Sauvegarder la photo dans la base de donne
                Photo savedPhoto = repository.save(existingPhoto);

                // Si un fichier faisait partie de la mise à jour, créer l'événement d'upload
                if (file != null && !file.isEmpty()) {
                    Map<String, Object> uploadPayload = new HashMap<>();
                    uploadPayload.put("photoId", savedPhoto.getId());
                    uploadPayload.put("fileContent", file.getBytes());
                    outboxEventService.createAndSaveEvent("PHOTO_UPLOAD_REQUESTED", uploadPayload);
                }
                return Utils.convertToResponse(savedPhoto);
            }
            // Si rien n'a changé, il suffit de retourner l'état actuel.
            return Utils.convertToResponse(existingPhoto);
        } catch (Exception e) {
            throw new FileUploadException("Erreur lors de la mise à jour de la photo");
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public PhotoResponse uploadPhoto(PhotoUploadRequest request) {
        try {
            // Vérifier que l'album existe
            Album album = albumService.findById(request.getAlbumId())
                    .orElseThrow(() -> new NotFoundException("Album non trouvé pour l'id : " + request.getAlbumId()));

            MultipartFile file = request.getFile();
            Map<String, String> fileNameParts = Utils.parseFilename(file.getOriginalFilename());

            Photo photo = new Photo();
            photo.setNomFichier(fileNameParts.get("fullName"));
            photo.setDescription(request.getDescription());
            photo.setOrdre(request.getOrdre() != null ? request.getOrdre() : 0);
            photo.setAlbum(album);

            Photo savedPhoto = repository.save(photo);

            // 3. Préparer le payload pour l'événement Outbox.
            // Il contient l'ID de la photo et le contenu du fichier en bytes.
            Map<String, Object> payload = new HashMap<>();
            payload.put("photoId", savedPhoto.getId());
            payload.put("fileContent", file.getBytes()); // Le contenu binaire du fichier

            // On crée une "promesse" d'upload
            outboxEventService.createAndSaveEvent("PHOTO_UPLOAD_REQUESTED", payload);
            return Utils.convertToResponse(savedPhoto);

        } catch (IOException e) {
            throw new FileUploadException("Erreur lors de l'upload de la photo");
        }
    }

    public List<PhotoResponse> getPhotosForCurrentUser(Long userId) {
        List<Photo> photos = repository.findByUtilisateurId(userId);
        return photos.stream()
                .map(Utils::convertToResponse)
                .collect(Collectors.toList());
    }
}
