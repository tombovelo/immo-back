package com.immo.controller;
import com.immo.dto.ProprietaireResponse;
import com.immo.error.NotFoundException;
import com.immo.service.ProprietaireService;


import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/proprietaires")
@RequiredArgsConstructor
public class ProprietaireController {

    private final ProprietaireService service;

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


    // @PutMapping("/{id}")
    // public ResponseEntity<ProprietaireResponse> update(@PathVariable Long id, @Valid @RequestBody ProprietaireRequest request) {
    //     ProprietaireResponse updated = service.update(id, request);
    //     return ResponseEntity.ok(updated);
    // }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> delete(@PathVariable Long id) {
    //     return service.findById(id)
    //         .map(existing -> {
    //             cloudinaryService.deleteProprietaire(existing);
    //             service.deleteById(id);
    //             return new ResponseEntity<Void>(HttpStatus.OK);
    //         })
    //         .orElseThrow(() -> new NotFoundException("Aucun propriétaire trouvé avec id : " + id));

    // }
}

