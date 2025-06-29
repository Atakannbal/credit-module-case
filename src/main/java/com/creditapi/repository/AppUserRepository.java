package com.creditapi.repository;

import com.creditapi.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

/*
Spring Data JPA scans for interfaces that extend JpaRepository (or other Spring Data repository interfaces) and automatically registers them as Spring-managed beans.
This eliminates the need to explicitly annotate them with @Repository.
*/ 
public interface AppUserRepository extends JpaRepository<AppUser, String> {
    AppUser findByUsername(String username);
}
