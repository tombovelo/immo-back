package com.immo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.immo.model.OutboxEvent;
import com.immo.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Crée et sauvegarde un événement Outbox de manière générique.
     * Cette méthode est conçue pour être appelée à l'intérieur d'une transaction existante.
     *
     * @param eventType Le type de l'événement (ex: "PROPRIETAIRE_CREATED").
     * @param payloadObject L'objet de données à sérialiser en JSON dans le payload.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void createAndSaveEvent(String eventType, Object payloadObject) {
        try {
            // Sérialiser l'objet Proprietaire complet en JSON pour le stocker dans le payload
            String payload = objectMapper.writeValueAsString(payloadObject);
             // Créer l'événement Outbox qui représente la tâche à effectuer
            OutboxEvent event = new OutboxEvent(eventType, payload);
            // sauvgarde de l'evenement dans la base de donner
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            // Si la sérialisation échoue, la transaction sera annulée.
            throw new RuntimeException("Échec de la sérialisation de l'événement Outbox de type " + eventType, e);
        }
    }
}


