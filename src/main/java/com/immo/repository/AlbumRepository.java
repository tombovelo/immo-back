package com.immo.repository;
import com.immo.model.Album;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Récupérer tous les albums d’un propriétaire
    @Query("SELECT a FROM Album a " +
           "JOIN a.maison m " +
           "JOIN m.proprietaire p " +
           "WHERE p.id = :proprietaireId")
    List<Album> findByProprietaireId(Long proprietaireId);

     @Query("SELECT a FROM Album a " +
           "JOIN FETCH a.maison m " +
           "JOIN m.proprietaire p " +
           "JOIN p.utilisateur u " +
           "WHERE u.id = :userId")
    List<Album> findByUtilisateurId(@Param("userId") Long userId);
}

