package com.immo.service;

import com.immo.dto.MaisonRequest;
import com.immo.dto.MaisonResponse;
import com.immo.error.NotFoundException;
import com.immo.model.Maison;
import com.immo.model.Proprietaire;
import com.immo.model.TypeTransaction;
import com.immo.repository.MaisonRepository;
import com.immo.repository.ProprietaireRepository;
import com.immo.repository.TypeTransactionRepository;
import com.immo.utils.Utils;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaisonService {

    private final MaisonRepository repository;
    private final ProprietaireRepository proprietaireRepository;
    private final TypeTransactionRepository typeTransactionRepository; 
    
    public List<MaisonResponse> findAll() {
        return repository.findAllWithProprietaire()
            .stream()
            .map(Utils::convertToResponse) // référence de méthode
            .collect(Collectors.toList());
    }

    public Optional<MaisonResponse> findResponseById(Long id) { 
        return repository.findByIdWithProprietaire(id)
            .map(Utils::convertToResponse);
    }

    public Optional<Maison> findById(Long id) { 
        return repository.findByIdWithProprietaire(id);
    }
    
    // Gardez les méthodes originales si besoin
    public List<Maison> findAllWithoutProprietaire() { 
        return repository.findAll(); 
    }
    
    public Optional<Maison> findByIdWithoutProprietaire(Long id) { 
        return repository.findById(id); 
    }
    
    public void deleteById(Long id) { 
        repository.deleteById(id); 
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public MaisonResponse createMaison(MaisonRequest request) {
        Proprietaire proprietaire = proprietaireRepository.findById(request.getProprietaireId())
            .orElseThrow(() -> new NotFoundException("Propriétaire non trouvé avec id: " + request.getProprietaireId()));
        TypeTransaction typeTransaction = typeTransactionRepository.findById(request.getTypeTransactionId())
            .orElseThrow(() -> new NotFoundException("Type de transaction non trouvé avec id: " + request.getTypeTransactionId()));
        Maison maison = new Maison();
        maison.setAdresse(request.getAdresse());
        maison.setVille(request.getVille());
        maison.setCodePostal(request.getCodePostal());
        maison.setNombrePieces(request.getNombrePieces());
        maison.setPrix(request.getPrix());
        maison.setDescription(request.getDescription());
        maison.setVisible(request.getVisible() != null ? request.getVisible() : true);
        maison.setProprietaire(proprietaire);
        maison.setTypeTransaction(typeTransaction);
        Point point = Utils.convertToPoint(request.getLongitude(), request.getLatitude());
        maison.setCoordinate(point);
        return Utils.convertToResponse(repository.save(maison));
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30, rollbackFor = Exception.class)
    public MaisonResponse updateMaison(Long id, MaisonRequest request) {
        // Vérifier si la maison existe
        Maison existing = repository.findByIdWithProprietaire(id)
            .orElseThrow(() -> new NotFoundException("Aucune maison trouvée avec id : " + id));
        // Charger les entités liées
        Proprietaire proprietaire = proprietaireRepository.findById(request.getProprietaireId())
            .orElseThrow(() -> new NotFoundException("Propriétaire non trouvé avec id : " + request.getProprietaireId()));
        TypeTransaction typeTransaction = typeTransactionRepository.findById(request.getTypeTransactionId())
            .orElseThrow(() -> new NotFoundException("Type de transaction non trouvé avec id : " + request.getTypeTransactionId()));
        // Mettre à jour les champs
        existing.setAdresse(request.getAdresse());
        existing.setVille(request.getVille());
        existing.setCodePostal(request.getCodePostal());
        existing.setNombrePieces(request.getNombrePieces());
        existing.setPrix(request.getPrix());
        existing.setDescription(request.getDescription());
        existing.setVisible(request.getVisible() != null ? request.getVisible() : true);
        existing.setProprietaire(proprietaire);
        existing.setTypeTransaction(typeTransaction);
        Point point = Utils.convertToPoint(request.getLongitude(), request.getLatitude());
        existing.setCoordinate(point);
        // Sauvegarde et retour de la réponse
        Maison saved = repository.save(existing);
        return Utils.convertToResponse(saved);
    }

    public List<MaisonResponse> searchMaisons(
            String ville,
            Double minPrix,
            Double maxPrix,
            Long typeTransactionId,
            Integer minPieces,
            Integer maxPieces,
            Long proprietaireId,
            Boolean visible,
            Double latitude,
            Double longitude,
            Double distanceKm) {
        
        List<Maison> maisons = repository.searchMaisons(
            ville, minPrix, maxPrix, typeTransactionId,
            minPieces, maxPieces, proprietaireId, visible,
            latitude, longitude, distanceKm
        );
        
        return maisons.stream()
                .map(Utils::convertToResponse)
                .collect(Collectors.toList());
    }
    

}
