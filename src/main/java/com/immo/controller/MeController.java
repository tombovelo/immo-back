package com.immo.controller;

import com.immo.dto.AlbumRequest;
import com.immo.dto.AlbumResponse;
import com.immo.dto.AuthResponse;
import com.immo.dto.MaisonRequest;
import com.immo.dto.MaisonRequestUser;
import com.immo.dto.MaisonResponse;
import com.immo.dto.PhotoResponse;
import com.immo.dto.PhotoUploadRequest;
import com.immo.dto.ProprietaireRequest;
import com.immo.dto.ProprietaireResponse;
import com.immo.service.UserDetailsImpl;
import com.immo.service.AlbumService;
import com.immo.service.MaisonService;
import com.immo.service.PhotoService;
import com.immo.service.ProprietaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final ProprietaireService proprietaireService;
    private final MaisonService maisonService;
    private final AlbumService albumService;
    private final PhotoService photoService;

    // === Gestion du profil du propriétaire connecté ===

    @GetMapping("/profile") // ok
    public ResponseEntity<ProprietaireResponse> getMyProfile(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        // Note: Vous devrez créer une méthode findByUserId dans votre
        // ProprietaireService
        return proprietaireService.getMyProfile(currentUser.getId())
                .map(proprietaire -> ResponseEntity.ok(proprietaire))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/profile", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<AuthResponse> updateMyProfile(@AuthenticationPrincipal UserDetailsImpl currentUser,
            @ModelAttribute ProprietaireRequest request) {
        // On utilise @ModelAttribute car la requête est en multipart/form-data (à causedu fichier)
        // L'ID de l'utilisateur est récupéré depuis le token d'authentification pour la sécurité
        AuthResponse response = proprietaireService.update(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    // === Gestion des maisons du propriétaire connecté ===

    @PostMapping("/maisons")
    public ResponseEntity<MaisonResponse> createMaison(@AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @ModelAttribute MaisonRequestUser request) {
        // Note: Votre service doit pouvoir créer une maison pour un utilisateur spécifique
        MaisonResponse maisonCreated = maisonService.createMaison(request, currentUser.getId());
        return new ResponseEntity<>(maisonCreated, HttpStatus.CREATED);
    }

    @PutMapping(value = "/maisons/{maisonId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }) // ok
    @PreAuthorize("@maisonService.isOwner(#maisonId, authentication.principal.id)")
    public ResponseEntity<MaisonResponse> updateMaison(@PathVariable Long maisonId, @ModelAttribute MaisonRequestUser request) {
        MaisonResponse response = maisonService.updateMaisonForCurrentUser(maisonId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/maisons/{maisonId}") // ok
    @PreAuthorize("@maisonService.isOwner(#maisonId, authentication.principal.id)")
    public ResponseEntity<Void> deleteMaison(@PathVariable Long maisonId) {
        maisonService.delete(maisonId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/maisons")
    public ResponseEntity<List<MaisonResponse>> getMyMaisons(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<MaisonResponse> maisons = maisonService.getMaisonForCurrentUser(currentUser.getId());
        return ResponseEntity.ok(maisons);
    }

    // ========= Gestion des albums des personne connecter ===============

    @PostMapping("/albums") // ok
    @PreAuthorize("@maisonService.isOwner(#request.maisonId, authentication.principal.id)")
    public ResponseEntity<AlbumResponse> createAlbum(@Valid @RequestBody AlbumRequest request) {
        AlbumResponse albumCreated = albumService.createAlbum(request);
        return new ResponseEntity<>(albumCreated, HttpStatus.CREATED);
    }

    @PutMapping("/albums/{albumId}")
    @PreAuthorize("@albumService.isOwner(#albumId, authentication.principal.id) && @maisonService.isOwner(#request.maisonId, authentication.principal.id)")
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
    @PreAuthorize("@albumService.isOwner(#albumId, authentication.principal.id)")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long albumId) {
        albumService.deleteAlbum(albumId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/albums")
    public ResponseEntity<List<AlbumResponse>> getMyAlbums(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<AlbumResponse> albums = albumService.getAlbumsForCurrentUser(currentUser.getId());
        return ResponseEntity.ok(albums);
    }

    // ========= Gestion des photo des personne connecter ===============

    @GetMapping("/photos")
    public ResponseEntity<List<PhotoResponse>> getMyPhotos(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<PhotoResponse> photos = photoService.getPhotosForCurrentUser(currentUser.getId());
        return ResponseEntity.ok(photos);
    }

    @PutMapping("/photos/{photoId}")
    @PreAuthorize("@photoService.isOwner(#photoId, authentication.principal.id)")
    public ResponseEntity<PhotoResponse> updatePhoto(
        @PathVariable Long photoId, // Correction ici
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "ordre", required = false) Integer ordre,
            @RequestParam(value = "albumId", required = false) Long albumId) {
        PhotoResponse photoUpdated = photoService.updatePhoto(photoId, file, description, ordre, albumId);
        return ResponseEntity.ok(photoUpdated);
    }

    @PostMapping(value = "/photos/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("@albumService.isOwner(#request.albumId, authentication.principal.id)")
    public ResponseEntity<PhotoResponse> uploadPhoto(@Valid @ModelAttribute PhotoUploadRequest request) {
        PhotoResponse photoUploaded = photoService.uploadPhoto(request);
        return new ResponseEntity<>(photoUploaded, HttpStatus.CREATED);
    }

    @DeleteMapping("/photos/{photoId}")
    @PreAuthorize("@photoService.isOwner(#photoId, authentication.principal.id)")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }
}
