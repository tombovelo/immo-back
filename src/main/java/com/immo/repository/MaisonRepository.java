package com.immo.repository;

import com.immo.model.Maison;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MaisonRepository extends JpaRepository<Maison, Long> {
    
    // Gardez les méthodes existantes pour différents cas d'usage
    @Query("SELECT m FROM Maison m JOIN FETCH m.proprietaire")
    List<Maison> findAllWithProprietaire();
    
    @Query("SELECT m FROM Maison m JOIN FETCH m.proprietaire WHERE m.id = :id")
    Optional<Maison> findByIdWithProprietaire(Long id);

    @Query("SELECT m FROM Maison m JOIN FETCH m.proprietaire p WHERE p.id = :proprietaireId")
    List<Maison> findAllByProprietaireId(@Param("proprietaireId") Long proprietaireId);

    @Query(value = """
    SELECT m.* FROM maison m
    INNER JOIN proprietaire p ON m.proprietaire_id = p.id
    INNER JOIN type_transaction t ON m.type_transaction_id = t.id
    WHERE 
        (:adresse IS NULL OR LOWER(m.adresse) LIKE LOWER(CONCAT('%', :adresse, '%')))
        AND (:ville IS NULL OR LOWER(m.ville) LIKE LOWER(CONCAT('%', :ville, '%')))
        AND (:minPrix IS NULL OR m.prix >= :minPrix)
        AND (:maxPrix IS NULL OR m.prix <= :maxPrix)
        AND (:typeTransactionId IS NULL OR t.id = :typeTransactionId)
        AND (:minPieces IS NULL OR m.nombre_pieces >= :minPieces)
        AND (:maxPieces IS NULL OR m.nombre_pieces <= :maxPieces)
        AND (:proprietaireId IS NULL OR p.id = :proprietaireId)
        AND (:visible IS NULL OR m.visible = :visible)
        AND (
            :latitude IS NULL OR :longitude IS NULL OR :distanceKm IS NULL
            OR ST_DWithin(
                m.coordonnees::geography,
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                :distanceKm * 1000
            )
        )
    """, nativeQuery = true)
    List<Maison> searchMaisons(
        @Param("adresse") String adresse,
        @Param("ville") String ville,
        @Param("minPrix") Double minPrix,
        @Param("maxPrix") Double maxPrix,
        @Param("typeTransactionId") Long typeTransactionId,
        @Param("minPieces") Integer minPieces,
        @Param("maxPieces") Integer maxPieces,
        @Param("proprietaireId") Long proprietaireId,
        @Param("visible") Boolean visible,
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("distanceKm") Double distanceKm
    );

}

