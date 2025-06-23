package com.creditapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class LoanInstallment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonIgnore
    private Loan loan;
    
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private boolean isPaid;

    public LoanInstallment() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Loan getLoan() { return loan; }
    public void setLoan(Loan loan) { this.loan = loan; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
}
