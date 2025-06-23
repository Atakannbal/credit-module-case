package com.creditapi.repository;

import com.creditapi.model.LoanInstallment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, UUID> {
    List<LoanInstallment> findByLoanId(UUID loanId);
}
