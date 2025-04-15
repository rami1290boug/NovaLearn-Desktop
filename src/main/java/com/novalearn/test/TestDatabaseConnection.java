package com.novalearn.test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        // Create an EntityManagerFactory based on the persistence unit name in persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("NovalearnPU");
        EntityManager em = emf.createEntityManager();

        try {
            // Begin a transaction (not strictly required for a SELECT, but often good practice)
            em.getTransaction().begin();

            // Execute a simple query to test the connection, for example, count Users
            Long count = em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
            System.out.println("Connected! Number of User records: " + count);

            // Commit (for a test read, commit or rollback doesn't affect much)
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
            emf.close();
        }
    }
}
