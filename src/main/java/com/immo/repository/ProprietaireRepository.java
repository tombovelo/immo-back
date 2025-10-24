package com.immo.repository;

import com.immo.model.Proprietaire;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProprietaireRepository extends JpaRepository<Proprietaire, Long> {
    Optional<Proprietaire> findByUtilisateur_Email(String email);
}

