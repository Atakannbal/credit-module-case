package com.creditapi.controller;

import com.creditapi.dto.LoginRequestDTO;
import com.creditapi.model.AppUser;
import com.creditapi.repository.AppUserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    private final AppUserRepository userRepo;    
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.secret}")
    private String SECRET;


    public AuthController(AppUserRepository userRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AppUser user = userRepo.findByUsername(loginRequest.getUsername());

        if (user == null || !encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .claim("customerId", user.getCustomerId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        Map<String, String> result = new HashMap<>();

        result.put("token", token);
        result.put("role", user.getRole());
        result.put("customerId", user.getCustomerId());
        
        return result;
    }
}
