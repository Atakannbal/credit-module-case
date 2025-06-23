package com.creditapi.config;

import com.creditapi.model.AppUser;
import com.creditapi.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
 * This configuration class initializes the application with default user data.
 * This is useful for development and testing purposes, allowing quick setup of user accounts.
 * The admin user has the role "ADMIN" and a specific UUID, while the customer users
 * have the role "CUSTOMER" and unique UUIDs.
 * The CommandLineRunner bean executes this logic at application startup.
 */

@Configuration
public class UserDataLoader {
    @Bean
    CommandLineRunner initUsers(AppUserRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new AppUser("admin", new BCryptPasswordEncoder().encode("admin"), "ADMIN", "99999999-9999-9999-9999-999999999999"));
                repo.save(new AppUser("user1", new BCryptPasswordEncoder().encode("user1"), "CUSTOMER", "00000000-0000-0000-0000-000000000001"));
                repo.save(new AppUser("user2", new BCryptPasswordEncoder().encode("user2"), "CUSTOMER", "00000000-0000-0000-0000-000000000002"));
                repo.save(new AppUser("user3", new BCryptPasswordEncoder().encode("user3"), "CUSTOMER", "00000000-0000-0000-0000-000000000003"));
            }
        };
    }
}
