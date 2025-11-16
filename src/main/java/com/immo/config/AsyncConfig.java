package com.immo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    // Configuration de thread pour execution des tache outboxEvent

    @Bean(name = "outboxEventExecutor")
    public Executor outboxEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Nombre de threads à garder actifs
        executor.setMaxPoolSize(10); // Nombre maximum de threads
        executor.setQueueCapacity(25); // Taille de la file d'attente avant de créer de nouveaux threads
        executor.setThreadNamePrefix("OutboxEvent-");
        executor.initialize();
        return executor;
    }
}


