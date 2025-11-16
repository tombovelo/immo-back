package com.immo.controller;

import com.immo.dto.AlbumRequest;
import com.immo.dto.AlbumResponse;
import com.immo.dto.MaisonRequest;
import com.immo.dto.MaisonResponse;
import com.immo.dto.PhotoResponse;
import com.immo.dto.PhotoUploadRequest;
import com.immo.dto.ProprietaireAdminRequest;
import com.immo.dto.ProprietaireResponse;
import com.immo.model.TypeTransaction;
import com.immo.service.AlbumService;
import com.immo.service.MaisonService;
import com.immo.service.PhotoService;
import com.immo.service.ProprietaireService;
import com.immo.service.TypeTransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final ProprietaireService proprietaireService;
    private final TypeTransactionService typeTransactionService;
    private final MaisonService maisonService;
    private final AlbumService albumService;
    private final PhotoService photoService;

    // === Gestion des propriétaires ===

    @GetMapping("/proprietaires")
    public ResponseEntity<List<ProprietaireResponse>> getAllProprietaires() {
        return ResponseEntity.ok(proprietaireService.findAll());
    }

    @DeleteMapping("/proprietaires/{id}")
    public ResponseEntity<Void> deleteProprietaire(@PathVariable Long id) {
        proprietaireService.deleteProprietaire(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/proprietaires/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProprietaireResponse> updateProprietaire(
            @PathVariable("id") Long proprietaireId, // Renommé pour plus de clarté
            @ModelAttribute ProprietaireAdminRequest request) {
        // L'admin peut mettre à jour le profil de n'importe quel propriétaire via son ID
        ProprietaireResponse response = proprietaireService.updateAdmin(proprietaireId, request);
        return ResponseEntity.ok(response);
    }

    // ========== Gestion des Transaction =============

    @PostMapping
    public ResponseEntity<TypeTransaction> create(@RequestBody TypeTransaction typeTransaction) {
        TypeTransaction typeTransactionCreated = typeTransactionService.save(typeTransaction);
        return new ResponseEntity<>(typeTransactionCreated, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeTransaction> update(@PathVariable Long id, @RequestBody TypeTransaction typeTransaction) {
        TypeTransaction updated = typeTransactionService.update(id, typeTransaction);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        typeTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =========== Gestion des maison ===============

    @PostMapping(value = "/proprietaires/{proprietaireId}/maisons", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<MaisonResponse> createMaison(
            @PathVariable Long proprietaireId,
            @Valid @ModelAttribute MaisonRequest request) {
        MaisonResponse maisonCreated = maisonService.createMaisonForAdmin(proprietaireId, request);
        return new ResponseEntity<>(maisonCreated, HttpStatus.CREATED);
    }

    @PutMapping(value = "/maisons/{maisonId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }) // ok
    public ResponseEntity<MaisonResponse> updateMaison(@PathVariable Long maisonId, @ModelAttribute MaisonRequest request) {
        MaisonResponse response = maisonService.updateMaison(maisonId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/maisons/{maisonId}") // ok
    public ResponseEntity<Void> deleteMaison(@PathVariable Long maisonId) {
        maisonService.delete(maisonId);
        return ResponseEntity.noContent().build();
    }

    // ====== Gestion des album =================

    @PostMapping("/albums") // ok
    public ResponseEntity<AlbumResponse> createAlbum(@Valid @RequestBody AlbumRequest request) {
        AlbumResponse albumCreated = albumService.createAlbum(request);
        return new ResponseEntity<>(albumCreated, HttpStatus.CREATED);
    }

    @PutMapping("/albums/{albumId}")
    public ResponseEntity<AlbumResponse> updateAlbum(
            @PathVariable Long albumId,
            @Valid @RequestBody AlbumRequest request) {
        // Note: La méthode updateAlbum du service permet de changer la maison associée.
        // Pour plus de sécurité, on pourrait ajouter un second check :
        // @PreAuthorize("@albumService.isOwner(...) &&
        // @maisonService.isOwner(#request.maisonId, ...)")
        AlbumResponse albumUpdated = albumService.updateAlbum(albumId, request);
        return ResponseEntity.ok(albumUpdated);
    }

    @DeleteMapping("/albums/{albumId}") // ok
    public ResponseEntity<?> deleteAlbum(@PathVariable Long albumId) {
        albumService.deleteAlbum(albumId);
        return ResponseEntity.noContent().build();
    }

    // =========== Gestion des photo des personne connecter ==================

    @PutMapping("/photos/{photoId}")
    public ResponseEntity<PhotoResponse> updatePhoto(
            @PathVariable("photoId") Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "ordre", required = false) Integer ordre,
            @RequestParam(value = "albumId", required = false) Long albumId) {
        PhotoResponse photoUpdated = photoService.updatePhoto(id, file, description, ordre, albumId);
        return ResponseEntity.ok(photoUpdated);
    }

    @PostMapping(value = "/photos/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<PhotoResponse> uploadPhoto(@Valid @ModelAttribute PhotoUploadRequest request) {
        PhotoResponse photoUploaded = photoService.uploadPhoto(request);
        return new ResponseEntity<>(photoUploaded, HttpStatus.CREATED);
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }
}
