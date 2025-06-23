package com.creditapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
/*
 * This filter intercepts incoming HTTP requests to check for a JWT token in the Authorization header.
 * If a valid token is found, it extracts the user details and sets the authentication in the SecurityContext.
 * The token is expected to be in the format "Bearer <token>".
 * If the token is invalid or not present, it clears the SecurityContext.
 * This allows the application to secure endpoints by ensuring that only authenticated users can access them.
 */

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                String customerId = claims.get("customerId", String.class);
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
                JwtUserDetails userDetails = new JwtUserDetails(username, role, customerId);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }

    /*
     * Override this method to specify which requests should not be filtered.
     * In this case, we skip filtering for login, H2 console, API docs,
     * Swagger UI, and OpenAPI YAML endpoints.
     * This allows unauthenticated access to these endpoints.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/login") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/openapi.yaml");
    }
}
