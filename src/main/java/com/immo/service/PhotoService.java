package com.immo.service;

import com.immo.dto.PhotoResponse;
import com.immo.model.Photo;
import com.immo.repository.PhotoRepository;
import com.immo.utils.Utils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    private final PhotoRepository repository;

    public PhotoService(PhotoRepository repository) { 
        this.repository = repository;
    }

    public List<PhotoResponse> findAll() {
        return repository.findAll()
            .stream()
            .map(Utils::convertToResponse) // référence de méthode
            .collect(Collectors.toList());
    }

    public Optional<Photo> findById(Long id) { 
        return repository.findById(id); 
    }

     public Optional<PhotoResponse> findResponseById(Long id) { 
        return repository.findById(id)
        .map(Utils::convertToResponse); 
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public PhotoResponse save(Photo photo) { 
        return Utils.convertToResponse(repository.save(photo)); 
    }

    public void deleteById(Long id) { 
        repository.deleteById(id); 
    }
} 


