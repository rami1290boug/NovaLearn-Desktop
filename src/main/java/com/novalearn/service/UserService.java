package com.novalearn.service;

import com.novalearn.dao.UserDao;
import com.novalearn.entity.User;
import java.util.List;

public class UserService {
    private final UserDao dao;
    public UserService(UserDao dao) { this.dao = dao; }

    public List<User> getAllUsers() { return dao.findAll(); }
    public User getUser(Long id) { return dao.findById(id); }
    public void saveUser(User u) { dao.save(u); }
    public void deleteUser(User u) { dao.delete(u); }
}
