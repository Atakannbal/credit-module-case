package com.creditapi.repository;

import com.creditapi.model.Customer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/*
Spring Data JPA scans for interfaces that extend JpaRepository (or other Spring Data repository interfaces) and automatically registers them as Spring-managed beans.
This eliminates the need to explicitly annotate them with @Repository.
*/ 
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
