package com.marketplace.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    /*
     * Roles:
     * CUSTOMER
     * PROVIDER
     * ADMIN
     */
    @Column(nullable = false)
    private String role;

    /*
     * Only used for service providers.
     * Example: Plumber, Carpenter, Electrician.
     */
    private String professionType;

    /*
     * Only mainly used for customers.
     */
    private Double walletBalance;

    public User() {
    }

    public User(String username, String password, String role, String professionType, Double walletBalance) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.professionType = professionType;
        this.walletBalance = walletBalance;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public String getProfessionType() {
        return professionType;
    }

    public void setProfessionType(String professionType) {
        this.professionType = professionType;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }
}