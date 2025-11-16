package com.immo.repository;

import com.immo.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    // retourner des resultas paginer
    Page<OutboxEvent> findByProcessedFalse(Pageable pageable);
}





