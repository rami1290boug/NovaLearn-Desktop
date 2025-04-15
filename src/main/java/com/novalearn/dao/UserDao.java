package com.novalearn.dao;

import com.novalearn.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class UserDao {
    private final EntityManager em;
    public UserDao(EntityManager em) { this.em = em; }

    public List<User> findAll() {
        TypedQuery<User> q = em.createQuery("SELECT u FROM User u", User.class);
        return q.getResultList();
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    public void save(User u) {
        em.getTransaction().begin();
        if (u.getId() == null) em.persist(u);
        else em.merge(u);
        em.getTransaction().commit();
    }

    public void delete(User u) {
        em.getTransaction().begin();
        em.remove(em.contains(u) ? u : em.merge(u));
        em.getTransaction().commit();
    }
}
