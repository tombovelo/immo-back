package com.immo.controller;

import com.immo.dto.ProprietaireRequest;
import com.immo.dto.ProprietaireResponse;
import com.immo.error.NotFoundException;
import com.immo.service.CloudinaryService;
import com.immo.service.ProprietaireService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/proprietaires")
public class ProprietaireController {

    private final ProprietaireService service;
    private final CloudinaryService cloudinaryService;
    
    public ProprietaireController(ProprietaireService service, CloudinaryService cloudinaryService) { 
        this.service = service; 
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public ResponseEntity<List<ProprietaireResponse>> getAll() { 
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProprietaireResponse> getById(@PathVariable Long id) {
        return service.findResponseById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new NotFoundException("Aucun propriétaire trouvé avec id : " + id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProprietaireResponse> update(@PathVariable Long id, @Valid @RequestBody ProprietaireRequest request) {
        ProprietaireResponse updated = service.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.findById(id)
            .map(existing -> {
                cloudinaryService.deleteProprietaire(existing);
                service.deleteById(id);
                return new ResponseEntity<Void>(HttpStatus.OK);
            })
            .orElseThrow(() -> new NotFoundException("Aucun propriétaire trouvé avec id : " + id));

    }
}

