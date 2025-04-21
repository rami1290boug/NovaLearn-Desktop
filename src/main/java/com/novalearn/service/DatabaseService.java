package com.novalearn.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatabaseService {
    private static DatabaseService instance;
    private final EntityManagerFactory entityManagerFactory;
    private final EntityManager entityManager;

    private DatabaseService() {
        entityManagerFactory = Persistence.createEntityManagerFactory("novaLearnPU");
        entityManager = entityManagerFactory.createEntityManager();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void close() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
} 