package com.creditapi.service;

import com.creditapi.dto.LoanInstallmentDTO;
import com.creditapi.dto.LoanCreateRequestDTO;
import com.creditapi.dto.LoanCreateResponseDTO;
import com.creditapi.dto.LoanResponseDTO;
import com.creditapi.dto.PayInstallmentRequestDTO;
import com.creditapi.dto.PayInstallmentResponseDTO;

import java.util.List;
import java.util.UUID;

/*
 * This interface defines the contract for loan-related operations.
 * It includes methods for creating loans, listing loans by customer ID, listing installments by loan ID, and paying installments.
 * Each method is designed to handle specific loan management tasks, ensuring a clear separation of concerns.
 * The implementation of this interface will handle the business logic and data access for these operations.
 * * The methods return DTOs (Data Transfer Objects) to encapsulate the data being transferred between the service layer and the controller layer.
 * * This approach promotes a clean architecture by decoupling the service layer from the data model, allowing for easier maintenance and testing.
 */
public interface LoanService {
    LoanCreateResponseDTO createLoan(LoanCreateRequestDTO LoanCreateRequestDTO);

    List<LoanResponseDTO> listLoansByCustomerId(UUID customerId, Integer numberOfInstallments, Boolean isPaid);

    List<LoanInstallmentDTO> listInstallmentsByLoanId(UUID loanId);

    PayInstallmentResponseDTO payInstallments(UUID loanId, PayInstallmentRequestDTO requestDTO);
}
