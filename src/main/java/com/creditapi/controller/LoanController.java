package com.creditapi.controller;

import com.creditapi.dto.LoanCreateResponseDTO;
import com.creditapi.dto.LoanInstallmentDTO;
import com.creditapi.dto.LoanCreateRequestDTO;
import com.creditapi.dto.LoanResponseDTO;
import com.creditapi.dto.PayInstallmentRequestDTO;
import com.creditapi.dto.PayInstallmentResponseDTO;
import com.creditapi.mapper.LoanMapper;
import com.creditapi.service.LoanService;
import com.creditapi.security.JwtUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService, LoanMapper loanMapper) {
        this.loanService = loanService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LoanCreateResponseDTO> createLoan(
            @AuthenticationPrincipal JwtUserDetails user,
            @Valid @RequestBody LoanCreateRequestDTO request) {
        LoanCreateResponseDTO response = loanService.createLoan(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == principal.customerId)")
    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> listLoans(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestParam UUID customerId,
            @RequestParam(required = false) Integer numberOfInstallments,
            @RequestParam(required = false) Boolean isPaid) {
        List<LoanResponseDTO> loans = loanService.listLoansByCustomerId(customerId, numberOfInstallments, isPaid);
        return ResponseEntity.ok(loans);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<LoanInstallmentDTO>> listInstallmentsForLoan(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID loanId) {
        List<LoanInstallmentDTO> installments = loanService.listInstallmentsByLoanId(loanId);
        return ResponseEntity.ok(installments);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @PostMapping("/{loanId}/pay")
    public ResponseEntity<PayInstallmentResponseDTO> payInstallments(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID loanId,
            @Valid @RequestBody PayInstallmentRequestDTO request) {
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);
        return ResponseEntity.ok(response);
    }
}
