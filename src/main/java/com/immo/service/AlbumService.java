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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository repository;
    private final MaisonRepository maisonRepository;
    private final CloudinaryService cloudinaryService;

    public List<AlbumResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(Utils::convertToResponse) // référence de méthode
                .collect(Collectors.toList());
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

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<AlbumResponse> findAlbumsByProprietaireId(Long proprietaireId) {
        List<Album> albums = repository.findByProprietaireId(proprietaireId);
        return albums.stream()
                .map(Utils::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public AlbumResponse createAlbum(AlbumRequest request) {
        // On récupère la maison associée à cet album
        Maison maison = maisonRepository.findById(request.getMaisonId())
                .orElseThrow(() -> new NotFoundException("Maison non trouvée avec id: " + request.getMaisonId()));
        // creation de l'album dans cludinary
        Map<String, Object> result = cloudinaryService.createAlbum(maison, request);
        // Création d’un nouvel album
        Album album = new Album();
        album.setNomAlbum(request.getNomAlbum());
        album.setDescription(request.getDescription());
        album.setMaison(maison);
        album.setPath(String.valueOf(result.get("path")));
        // Sauvegarde et conversion en DTO
        Album savedAlbum = repository.save(album);
        return Utils.convertToResponse(savedAlbum);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public AlbumResponse updateAlbum(Long id, AlbumRequest request) {

        // 1️⃣ Vérifier si l'album existe
        Album existingAlbum = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aucun album trouvé avec id : " + id));

        // 2️⃣ Récupérer la maison associée
        Maison maison = maisonRepository.findById(request.getMaisonId())
                .orElseThrow(() -> new NotFoundException("Maison non trouvée avec id: " + request.getMaisonId()));

        // 3️⃣ Gérer le renommage du dossier Cloudinary si le nom change
        String ancienPath = existingAlbum.getPath();
        String newPath = ancienPath;

        if (!existingAlbum.getNomAlbum().equalsIgnoreCase(request.getNomAlbum())) {
            newPath = cloudinaryService.renameAlbum(ancienPath, maison, request);
        }

        // 4️⃣ Mettre à jour les champs
        existingAlbum.setNomAlbum(request.getNomAlbum());
        existingAlbum.setDescription(request.getDescription());
        existingAlbum.setMaison(maison);
        existingAlbum.setPath(newPath);

        // 5️⃣ Sauvegarder et renvoyer la réponse
        Album updated = repository.save(existingAlbum);
        return Utils.convertToResponse(updated);
    }
}
