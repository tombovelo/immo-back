package com.immo.controller;

import com.immo.dto.AlbumResponse;
import com.immo.error.NotFoundException;
import com.immo.service.AlbumService;
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

    @GetMapping("/proprietaire/{proprietaireId}")
    public ResponseEntity<List<AlbumResponse>> getByProprietaire(@PathVariable Long proprietaireId) {
        List<AlbumResponse> albums = service.findAlbumsByProprietaireId(proprietaireId);
        return ResponseEntity.ok(albums);
    }

     // @PostMapping
    // public ResponseEntity<AlbumResponse> create(@Valid @RequestBody AlbumRequest request) {
    //     AlbumResponse albumCreated = service.createAlbum(request);
    //     return new ResponseEntity<>(albumCreated, HttpStatus.CREATED);
    // }

    

    // @PutMapping("/{id}")
    // public ResponseEntity<AlbumResponse> update(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
    //     AlbumResponse albumCreated = service.updateAlbum(id, request);
    //     return ResponseEntity.ok(albumCreated);
    // }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> delete(@PathVariable Long id) {
    //     return service.findById(id)
    //         .map(existing -> {
    //             cloudinaryService.deleteAlbum(existing);
    //             service.deleteById(id);
    //             return new ResponseEntity<Void>(HttpStatus.OK); // ✅ Ajout du <Void>
    //         })
    //         .orElseThrow(() -> new NotFoundException("Aucune album trouvée avec id : " + id));
    // }
}

