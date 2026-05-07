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

    public User registerCustomer(String username, String password, double initialBalance) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        if (initialBalance < 0) {
            throw new RuntimeException("Initial balance cannot be negative");
        }

        if (findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("CUSTOMER");
        user.setProfessionType(null);
        user.setWalletBalance(initialBalance);

        em.persist(user);
        return user;
    }

    public User registerProvider(String username, String password, String professionType) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        if (professionType == null || professionType.trim().isEmpty()) {
            throw new RuntimeException("Profession type is required");
        }

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

    public User registerAdmin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        if (findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("ADMIN");
        user.setProfessionType(null);
        user.setWalletBalance(0.0);

        em.persist(user);
        return user;
    }

    public User login(String username, String password) {
        User user = findByUsername(username);

        if (user == null) {
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }

    public List<User> getAllUsers() {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u ORDER BY u.id",
                User.class
        );

        return query.getResultList();
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    public User findByUsername(String username) {
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username",
                    User.class
            );

            query.setParameter("username", username);
            return query.getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }
}