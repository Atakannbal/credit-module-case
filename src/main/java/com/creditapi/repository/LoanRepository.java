package com.creditapi.repository;

import com.creditapi.model.Loan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/*
Spring Data JPA scans for interfaces that extend JpaRepository (or other Spring Data repository interfaces) and automatically registers them as Spring-managed beans.
This eliminates the need to explicitly annotate them with @Repository.
*/ 
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByCustomerId(UUID customerId);
}
