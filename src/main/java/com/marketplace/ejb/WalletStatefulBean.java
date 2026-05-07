package com.marketplace.ejb;

import com.marketplace.entities.User;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateful
public class WalletStatefulBean {

    @PersistenceContext(unitName = "UserWalletPU")
    private EntityManager em;

    private Long currentUserId;
    private Double sessionStartBalance;

    public void initSession(Long userId) {
        User user = em.find(User.class, userId);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!"CUSTOMER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Wallet operations are only allowed for customers");
        }

        this.currentUserId = userId;
        this.sessionStartBalance = user.getWalletBalance();
    }

    public Double getBalance() {
        validateSession();

        User user = em.find(User.class, currentUserId);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return user.getWalletBalance();
    }

    public Double addFunds(double amount) {
        validateSession();

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        User user = em.find(User.class, currentUserId);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setWalletBalance(user.getWalletBalance() + amount);
        em.merge(user);

        return user.getWalletBalance();
    }

    public Double deductFunds(double amount) {
        validateSession();

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        User user = em.find(User.class, currentUserId);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getWalletBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        user.setWalletBalance(user.getWalletBalance() - amount);
        em.merge(user);

        return user.getWalletBalance();
    }

    public Double refundFunds(double amount) {
        validateSession();

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        User user = em.find(User.class, currentUserId);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setWalletBalance(user.getWalletBalance() + amount);
        em.merge(user);

        return user.getWalletBalance();
    }

    public boolean hasSufficientBalance(double amount) {
        validateSession();

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        User user = em.find(User.class, currentUserId);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return user.getWalletBalance() >= amount;
    }

    public void rollback() {
        validateSession();

        User user = em.find(User.class, currentUserId);

        if (user != null && sessionStartBalance != null) {
            user.setWalletBalance(sessionStartBalance);
            em.merge(user);
        }
    }

    private void validateSession() {
        if (currentUserId == null) {
            throw new RuntimeException("Wallet session has not been initialized");
        }
    }

    @Remove
    public void endSession() {
        currentUserId = null;
        sessionStartBalance = null;
    }
}