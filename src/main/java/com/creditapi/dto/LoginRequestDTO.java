package com.creditapi.dto;

import jakarta.validation.constraints.Size;

public class LoginRequestDTO {
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
