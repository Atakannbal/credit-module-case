package com.creditapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class AppUser {
    @Id
    private String username;
    private String password;
    private String role;
    private String customerId;

    public AppUser() {}
    public AppUser(String username, String password, String role, String customerId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.customerId = customerId;
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
