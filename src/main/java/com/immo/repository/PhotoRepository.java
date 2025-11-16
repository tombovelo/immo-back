package com.immo.repository;

import com.immo.model.Photo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    @Query("SELECT ph FROM Photo ph " +
           "JOIN FETCH ph.album a " +
           "JOIN a.maison m JOIN m.proprietaire p JOIN p.utilisateur u WHERE u.id = :userId")
    List<Photo> findByUtilisateurId(@Param("userId") Long userId);

}

