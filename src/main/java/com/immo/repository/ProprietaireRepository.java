package com.immo.repository;

import com.immo.model.Proprietaire;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProprietaireRepository extends JpaRepository<Proprietaire, Long> {
    
    Optional<Proprietaire> findByUtilisateur_Email(String email);

    /**
     * Trouve un propriétaire par l'ID de son compte utilisateur associé.
     * @param userId L'ID de l'entité User.
     * @return Un Optional contenant le Proprietaire s'il est trouvé.
    */
    Optional<Proprietaire> findByUtilisateur_Id(Long userId);
}

