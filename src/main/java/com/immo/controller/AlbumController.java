package com.immo.controller;

import com.immo.dto.AlbumRequest;
import com.immo.dto.AlbumResponse;
import com.immo.error.NotFoundException;
import com.immo.service.AlbumService;
import com.immo.service.CloudinaryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService service;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getAll() { 
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getById(@PathVariable Long id) {
        return service.findResponseById(id)
            .map(albumResponse -> ResponseEntity.ok(albumResponse)) // si trouvé
            .orElseThrow(() -> new NotFoundException("Aucune album trouvé avec id : " + id));
    }

    // @PostMapping
    // public ResponseEntity<AlbumResponse> create(@RequestBody AlbumRequest album) {
    //     return maisonService.findById(album.getMaison().getId())
    //         .map(existing -> {
    //             Map<String, Object> result = cloudinaryService.createAlbum(existing, album);
    //             album.setPath(String.valueOf(result.get("path")));
    //             AlbumResponse albumCreated = service.save(album);
    //             return new ResponseEntity<>(albumCreated, HttpStatus.CREATED);
    //         })
    //         .orElseThrow(() -> new NotFoundException("Aucune maison trouvé avec id : " + album.getMaison().getId()));
    // }

    @PostMapping
    public ResponseEntity<AlbumResponse> create(@Valid @RequestBody AlbumRequest request) {
        AlbumResponse albumCreated = service.createAlbum(request);
        return new ResponseEntity<>(albumCreated, HttpStatus.CREATED);
    }

    // @PutMapping("/{id}")
    // public ResponseEntity<AlbumResponse> update(@PathVariable Long id, @RequestBody Album album) {
    //     return service.findById(id)
    //         .map(existing -> {
    //             String ancienPath = existing.getPath(); // Sauvegarder ancien nom
    //             Proprietaire proprietaire = existing.getMaison().getProprietaire();
    //             album.getMaison().setProprietaire(proprietaire);
    //             String newPath = cloudinaryService.renameAlbum(ancienPath, album);  // Passer ancien nom
    //             existing.setMaison(album.getMaison());
    //             existing.setDescription(album.getDescription());
    //             existing.setNomAlbum(album.getNomAlbum());
    //             existing.setPath(newPath);
    //             AlbumResponse savedAlbum = service.save(existing);
    //             return ResponseEntity.ok(savedAlbum);
    //         })
    //         .orElseThrow(() -> new NotFoundException("Aucune album trouvé avec id : " + id));
    // }

    @PutMapping("/{id}")
    public ResponseEntity<AlbumResponse> update(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        AlbumResponse albumCreated = service.updateAlbum(id, request);
        return ResponseEntity.ok(albumCreated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.findById(id)
            .map(existing -> {
                cloudinaryService.deleteAlbum(existing);
                service.deleteById(id);
                return new ResponseEntity<Void>(HttpStatus.OK); // ✅ Ajout du <Void>
            })
            .orElseThrow(() -> new NotFoundException("Aucune album trouvée avec id : " + id));
    }

    @GetMapping("/proprietaire/{proprietaireId}")
    public ResponseEntity<List<AlbumResponse>> getByProprietaire(@PathVariable Long proprietaireId) {
        List<AlbumResponse> albums = service.findAlbumsByProprietaireId(proprietaireId);
        return ResponseEntity.ok(albums);
    }
}

