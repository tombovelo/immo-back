package com.immo.controller;

import com.immo.error.NotFoundException;
import com.immo.model.TypeTransaction;
import com.immo.service.TypeTransactionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/types")
public class TypeTransactionController {

    private final TypeTransactionService service;
    public TypeTransactionController(TypeTransactionService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<TypeTransaction>> getAll() { 
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeTransaction> getById(@PathVariable Long id) {
        return service.findById(id)
               .map(typeTransaction -> ResponseEntity.ok(typeTransaction)) // si trouvé
                .orElseThrow(() -> new NotFoundException("Aucun typeTransaction trouvé avec id : " + id));
    }

   
}
