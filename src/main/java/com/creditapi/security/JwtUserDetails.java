package com.creditapi.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

/*
 * This class implements UserDetails to provide user information for JWT authentication.
 * It includes the username, role, and customerId.
 * The getAuthorities method returns the user's role as a GrantedAuthority.
 * This class is used by the JwtAuthFilter to set the authentication in the SecurityContext.
 */
public class JwtUserDetails implements UserDetails {
    private final String username;
    private final String role;
    private final String customerId;

    public JwtUserDetails(String username, String role, String customerId) {
        this.username = username;
        this.role = role;
        this.customerId = customerId;
    }

    public String getRole() { return role; }
    public String getCustomerId() { return customerId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_" + role);
    }
    @Override
    public String getPassword() { return null; }
    @Override
    public String getUsername() { return username; }
}
