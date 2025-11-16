package com.immo.service;

import com.immo.dto.AlbumRequest;
import com.immo.dto.AlbumResponse;
import com.immo.error.NotFoundException;
import com.immo.model.Album;
import com.immo.model.Maison;
import com.immo.repository.AlbumRepository;
import com.immo.repository.MaisonRepository;
import com.immo.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {

    private final AlbumRepository repository;
    private final MaisonRepository maisonRepository;
    private final CloudinaryService cloudinaryService;
    private final OutboxEventService outboxEventService;

    public List<AlbumResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(Utils::convertToResponse) // référence de méthode
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public Optional<Album> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<AlbumResponse> findResponseById(Long id) {
        return repository.findById(id)
                .map(Utils::convertToResponse);
    }

    public AlbumResponse save(Album album) {
        return Utils.convertToResponse(repository.save(album));
    }

    public List<AlbumResponse> findAlbumsByProprietaireId(Long proprietaireId) {
        List<Album> albums = repository.findByProprietaireId(proprietaireId);
        return albums.stream()
                .map(Utils::convertToResponse)
                .collect(Collectors.toList());
    }

    //////////////////////// NOUVEAU METHODE ///////////////////////////////////

    @Transactional(rollbackFor = Exception.class)
    public AlbumResponse createAlbum(AlbumRequest request) {
        // On récupère la maison associée à cet album
        Maison maison = maisonRepository.findById(request.getMaisonId())
                .orElseThrow(() -> new NotFoundException("Maison non trouvée avec id: " + request.getMaisonId()));

        // Création d’un nouvel album
        Album album = new Album();
        album.setNomAlbum(request.getNomAlbum());
        album.setDescription(request.getDescription());
        album.setMaison(maison);

        // 1. Sauvegarder l'album une seule fois pour obtenir l'ID
        Album savedAlbum = repository.save(album);

        // 2. Créer un événement Outbox avec l'ID de l'album.
        outboxEventService.createAndSaveEvent("ALBUM_CREATED", Map.of("albumId", savedAlbum.getId()));
        return Utils.convertToResponse(savedAlbum);
    }

    @Transactional(rollbackFor = Exception.class)
    public AlbumResponse updateAlbum(Long id, AlbumRequest request) {

        // 1️⃣ Vérifier si l'album existe
        Album existingAlbum = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aucun album trouvé avec id : " + id));

        // 2️⃣ Récupérer la maison associée
        Maison maison = maisonRepository.findById(request.getMaisonId())
                .orElseThrow(() -> new NotFoundException("Maison non trouvée avec id: " + request.getMaisonId()));

        boolean isModified = false;
        String oldPath = existingAlbum.getPath();

        // --- Mise à jour des métadonnées ---
        if (request.getDescription() != null && !request.getDescription().equals(existingAlbum.getDescription())) {
            existingAlbum.setDescription(request.getDescription());
            isModified = true;
        }

        if (request.getNomAlbum() != null && !request.getNomAlbum().equalsIgnoreCase(existingAlbum.getNomAlbum())) {
            existingAlbum.setNomAlbum(request.getNomAlbum());
            isModified = true;
        }

        if (!maison.getId().equals(existingAlbum.getMaison().getId())) {
            existingAlbum.setMaison(maison);
            isModified = true;
        }

        // --- Gérer le renommage asynchrone si le chemin a changé ---
        String newPath = String.format("%s/%s/%s",
                cloudinaryService.getBasePath(),
                existingAlbum.getMaison().getProprietaire().getTelephone(),
                existingAlbum.getNomAlbum());

        if (!newPath.equals(oldPath)) {
            existingAlbum.setPath(newPath);
            isModified = true; // Assure la sauvegarde même si seul le chemin a changé
            Map<String, String> renamePayload = Map.of("oldPath", oldPath, "newPath", newPath);
            outboxEventService.createAndSaveEvent("ALBUM_RENAMED", renamePayload);
        }

        if (isModified) {
            Album updated = repository.save(existingAlbum);
            return Utils.convertToResponse(updated);
        }

        return Utils.convertToResponse(existingAlbum);
    }

    public boolean isOwner(Long albumId, Long userId) {
        // 1. Trouve l'album par son ID.
        return repository.findById(albumId)
                // 2. Si l'album est trouvé, on navigue jusqu'à l'utilisateur propriétaire.
                .map(album -> album.getMaison().getProprietaire().getUtilisateur().getId().equals(userId))
                // 3. Si l'album n'est pas trouvé, on retourne false par sécurité.
                .orElse(false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAlbum(Long albumId) {
        Album album = repository.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Aucun album trouvé avec id : " + albumId));
        outboxEventService.createAndSaveEvent("ALBUM_DELETED", album);
        repository.delete(album);
    }

    public List<AlbumResponse> getAlbumsForCurrentUser(Long userId) {
        // Appelle la nouvelle méthode du repository qui effectue une seule requête optimisée
        List<Album> albums = repository.findByUtilisateurId(userId);
        return albums.stream()
                .map(Utils::convertToResponse)
                .collect(Collectors.toList());
    }
}
