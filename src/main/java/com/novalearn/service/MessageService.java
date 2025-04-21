package com.novalearn.service;

import com.novalearn.entity.Message;
import com.novalearn.entity.Reclamation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class MessageService {
    private static MessageService instance;
    private final EntityManager entityManager;

    private MessageService() {
        this.entityManager = DatabaseService.getInstance().getEntityManager();
    }

    public static MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }

    public Message saveMessage(Message message) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(message);
            entityManager.getTransaction().commit();
            return message;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Message> getMessagesByReclamation(Reclamation reclamation) {
        TypedQuery<Message> query = entityManager.createQuery(
            "SELECT m FROM Message m WHERE m.reclamation = :reclamation ORDER BY m.sentAt ASC",
            Message.class
        );
        query.setParameter("reclamation", reclamation);
        return query.getResultList();
    }

    public List<Message> searchMessages(String keyword, LocalDateTime startDate, LocalDateTime endDate, 
                                     String sender, Boolean isAgent, Reclamation reclamation) {
        StringBuilder jpql = new StringBuilder("SELECT m FROM Message m WHERE 1=1");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append(" AND LOWER(m.content) LIKE LOWER(:keyword)");
        }
        if (startDate != null) {
            jpql.append(" AND m.sentAt >= :startDate");
        }
        if (endDate != null) {
            jpql.append(" AND m.sentAt <= :endDate");
        }
        if (sender != null && !sender.trim().isEmpty()) {
            jpql.append(" AND m.sender = :sender");
        }
        if (isAgent != null) {
            jpql.append(" AND m.isAgent = :isAgent");
        }
        if (reclamation != null) {
            jpql.append(" AND m.reclamation = :reclamation");
        }
        
        jpql.append(" ORDER BY m.sentAt DESC");

        TypedQuery<Message> query = entityManager.createQuery(jpql.toString(), Message.class);

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        if (sender != null && !sender.trim().isEmpty()) {
            query.setParameter("sender", sender);
        }
        if (isAgent != null) {
            query.setParameter("isAgent", isAgent);
        }
        if (reclamation != null) {
            query.setParameter("reclamation", reclamation);
        }

        return query.getResultList();
    }
} 