package com.immo.repository;
import com.immo.model.Album;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Récupérer tous les albums d’un propriétaire
    @Query("SELECT a FROM Album a " +
           "JOIN a.maison m " +
           "JOIN m.proprietaire p " +
           "WHERE p.id = :proprietaireId")
    List<Album> findByProprietaireId(Long proprietaireId);
}

