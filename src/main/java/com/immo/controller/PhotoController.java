package com.immo.controller;

import com.immo.dto.PhotoResponse;
import com.immo.error.NotFoundException;
import com.immo.service.PhotoService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @GetMapping
    public ResponseEntity<List<PhotoResponse>> getAll() {
        return new ResponseEntity<>(photoService.findAll(), HttpStatus.OK); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getById(@PathVariable Long id) {
        return photoService.findResponseById(id)
            .map(photoResponse -> ResponseEntity.ok(photoResponse)) // si trouvé
            .orElseThrow(() -> new NotFoundException("Aucune photo trouvé avec id : " + id));
    }

    // @PostMapping("/upload")
    // public ResponseEntity<PhotoResponse> uploadPhoto(@Valid @ModelAttribute PhotoUploadRequest request) {

    //     // Vérifier que l'album existe
    //     Album album = albumService.findById(request.getAlbumId())
    //         .orElseThrow(() -> new NotFoundException("Album non trouvé pour l'id : " + request.getAlbumId()));
            
    //     Map<String, String> fileName = Utils.parseFilename(request.getFile().getOriginalFilename());

    //     Map<String, Object> uploadResult = cloudinaryService.uploadPhoto(
    //         request.getFile(),
    //         album,
    //         fileName.get("nameWithoutExt")
    //     );

    //     Photo photo = new Photo();
    //     photo.setNomFichier(fileName.get("fullName"));
    //     photo.setCloudinaryUrl(uploadResult.get("url").toString());
    //     photo.setCloudinaryPublicId(uploadResult.get("public_id").toString());
    //     photo.setDescription(request.getDescription());
    //     photo.setOrdre(request.getOrdre() != null ? request.getOrdre() : 0);
    //     photo.setAlbum(album);

    //     return ResponseEntity.ok(photoService.save(photo));
    // }


    // @PutMapping("/{id}")
    // public ResponseEntity<PhotoResponse> updatePhoto(
    //         @PathVariable Long id,
    //         @RequestParam(value = "file", required = false) MultipartFile file,
    //         @RequestParam(value = "description", required = false) String description,
    //         @RequestParam(value = "ordre", required = false) Integer ordre,
    //         @RequestParam(value = "albumId", required = false) Long albumId) {

    //     // Trouver la photo existante
    //     Photo existingPhoto = photoService.findById(id)
    //         .orElseThrow(() -> new NotFoundException("Aucune photo trouvée avec id : " + id));
        
    //     // Mettre à jour seulement les champs fournis
    //     if (description != null) {
    //         existingPhoto.setDescription(description);
    //     }
        
    //     if (ordre != null) {
    //         existingPhoto.setOrdre(ordre);
    //     }
        
    //     if (albumId != null) {
    //         Album album = albumService.findById(albumId)
    //             .orElseThrow(() -> new NotFoundException("Album non trouvé avec l'ID: " + albumId));
    //         existingPhoto.setAlbum(album);
    //     }

    //     // Gérer le nouveau fichier si fourni
    //     if (file != null && !file.isEmpty()) {
    //         Map<String, String> fileName = Utils.parseFilename(file.getOriginalFilename());
    //         // Supprimer l'ancienne photo de Cloudinary
    //         cloudinaryService.deletePhoto(existingPhoto.getCloudinaryPublicId());

    //         // recuprer album et le nom du proprietaire
    //         Album album = existingPhoto.getAlbum();
    
    //         // Uploader la nouvelle photo
    //         Map<String, Object> resultPhoto = cloudinaryService.uploadPhoto(file, album, fileName.get("nameWithoutExt"));
    //         // Mettre à jour les informations du fichier
    //         existingPhoto.setNomFichier(fileName.get("fullName")); // Garder le nom original
    //         existingPhoto.setCloudinaryPublicId(resultPhoto.get("public_id").toString());
    //         existingPhoto.setCloudinaryUrl(resultPhoto.get("url").toString());
    //     }

    //     PhotoResponse updatedPhoto = photoService.save(existingPhoto);
    //     return ResponseEntity.ok(updatedPhoto);
    // }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> delete(@PathVariable Long id) {
    //     return photoService.findById(id)
    //         .map(photo -> {
    //             photoService.deleteById(id);
    //             cloudinaryService.deletePhoto(photo.getCloudinaryPublicId());
    //             return new ResponseEntity<Void>(HttpStatus.OK);
    //         })
    //         .orElseThrow(() -> new NotFoundException("Aucune photo trouvée avec id : " + id));
    // }


}

