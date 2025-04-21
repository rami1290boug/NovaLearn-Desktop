package com.novalearn.service;

import com.novalearn.entity.Reclamation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class ReclamationService {
    private static ReclamationService instance;
    private final EntityManager entityManager;

    private ReclamationService() {
        this.entityManager = DatabaseService.getInstance().getEntityManager();
    }

    public static ReclamationService getInstance() {
        if (instance == null) {
            instance = new ReclamationService();
        }
        return instance;
    }

    public List<Reclamation> getAllReclamations() {
        TypedQuery<Reclamation> query = entityManager.createQuery(
            "SELECT r FROM Reclamation r ORDER BY r.createdAt DESC",
            Reclamation.class
        );
        return query.getResultList();
    }

    public Reclamation saveReclamation(Reclamation reclamation) {
        try {
            entityManager.getTransaction().begin();
            if (reclamation.getId() == 0) {
                entityManager.persist(reclamation);
            } else {
                entityManager.merge(reclamation);
            }
            entityManager.getTransaction().commit();
            return reclamation;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deleteReclamation(Reclamation reclamation) {
        try {
            entityManager.getTransaction().begin();
            entityManager.remove(reclamation);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Reclamation> searchReclamations(String keyword, String status, String priority, LocalDateTime date) {
        StringBuilder jpql = new StringBuilder("SELECT r FROM Reclamation r WHERE 1=1");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append(" AND (LOWER(r.title) LIKE LOWER(:keyword) OR LOWER(r.description) LIKE LOWER(:keyword))");
        }
        if (status != null && !status.trim().isEmpty()) {
            jpql.append(" AND r.status = :status");
        }
        if (priority != null && !priority.trim().isEmpty()) {
            jpql.append(" AND r.priority = :priority");
        }
        if (date != null) {
            jpql.append(" AND DATE(r.createdAt) = DATE(:date)");
        }
        
        jpql.append(" ORDER BY r.createdAt DESC");

        TypedQuery<Reclamation> query = entityManager.createQuery(jpql.toString(), Reclamation.class);

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            query.setParameter("status", status);
        }
        if (priority != null && !priority.trim().isEmpty()) {
            query.setParameter("priority", priority);
        }
        if (date != null) {
            query.setParameter("date", date);
        }

        return query.getResultList();
    }
} 