package com.marketplace.ejb;

import com.marketplace.entities.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class UserStatelessBean {

    @PersistenceContext(unitName = "UserWalletPU")
    private EntityManager em;

    // Register a new customer
    public User registerCustomer(String username, String password, double initialBalance) {
        if (findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("CUSTOMER");
        user.setWalletBalance(initialBalance);
        em.persist(user);
        return user;
    }

    // Register a new service provider
    public User registerProvider(String username, String password, String professionType) {
        if (findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("PROVIDER");
        user.setProfessionType(professionType);
        user.setWalletBalance(0.0);
        em.persist(user);
        return user;
    }

    // Login
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid username or password");
        }
        return user;
    }

    // Find user by username
    public User findByUsername(String username) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    // Find user by ID
    public User findById(Long id) {
        return em.find(User.class, id);
    }

    // Get all users (admin)
    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }
}