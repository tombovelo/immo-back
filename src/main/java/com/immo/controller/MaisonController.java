package com.immo.controller;

import com.immo.dto.MaisonRequest;
import com.immo.dto.MaisonResponse;
import com.immo.error.NotFoundException;
import com.immo.service.MaisonService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/maisons")
public class MaisonController {

    private final MaisonService service;

    public MaisonController(MaisonService service) { 
        this.service = service; 
    }

    @GetMapping
    public ResponseEntity<List<MaisonResponse>> getAll() { 
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaisonResponse> getById(@PathVariable Long id) {
        return service.findResponseById(id)
            .map(ResponseEntity::ok) // si trouvé
            .orElseThrow(() -> new NotFoundException("Aucune maison trouvé avec id : " + id));
    }

    @PostMapping
    public ResponseEntity<MaisonResponse> create(@Valid @RequestBody MaisonRequest maison) {
        MaisonResponse maisonCreated = service.createMaison(maison);
        return new ResponseEntity<>(maisonCreated, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaisonResponse> update(@PathVariable Long id, @Valid @RequestBody MaisonRequest request) {
        MaisonResponse response = service.updateMaison(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.findById(id)
            .map(existing -> {
                service.deleteById(id);
                return new ResponseEntity<Void>(HttpStatus.OK); // ✅ Ajout du <Void>
            })
            .orElseThrow(() -> new NotFoundException("Aucune maison trouvée avec id : " + id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MaisonResponse>> searchMaisons(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Double minPrix,
            @RequestParam(required = false) Double maxPrix,
            @RequestParam(required = false) Long typeTransactionId,
            @RequestParam(required = false) Integer minPieces,
            @RequestParam(required = false) Integer maxPieces,
            @RequestParam(required = false) Long proprietaireId,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double distanceKm) {
        
        List<MaisonResponse> results = service.searchMaisons(
            ville, minPrix, maxPrix, typeTransactionId,
            minPieces, maxPieces, proprietaireId, visible,
            latitude, longitude, distanceKm
        );
        
        return ResponseEntity.ok(results);
    }
}

