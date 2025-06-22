package com.creditapi.repository;

import com.creditapi.model.Loan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByCustomerId(UUID customerId);
}
