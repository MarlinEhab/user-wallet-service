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

    // Initialize session for a user
    public void initSession(Long userId) {
        this.currentUserId = userId;
        User user = em.find(User.class, userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        this.sessionStartBalance = user.getWalletBalance();
    }

    // Get current balance
    public Double getBalance() {
        User user = em.find(User.class, currentUserId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user.getWalletBalance();
    }

    // Add funds to wallet
    public Double addFunds(double amount) {
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

    // Deduct funds from wallet
    public Double deductFunds(double amount) {
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

    // Rollback to session start balance
    public void rollback() {
        User user = em.find(User.class, currentUserId);
        if (user != null && sessionStartBalance != null) {
            user.setWalletBalance(sessionStartBalance);
            em.merge(user);
        }
    }

    // Check if balance is sufficient
    public boolean hasSufficientBalance(double amount) {
        User user = em.find(User.class, currentUserId);
        if (user == null) return false;
        return user.getWalletBalance() >= amount;
    }

    // End session
    @Remove
    public void endSession() {
        currentUserId = null;
        sessionStartBalance = null;
    }
}