package com.creditapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class LoanInstallmentDTO {
    private UUID id;
    private UUID loanId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private boolean isPaid;

    public LoanInstallmentDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID loanId) { this.loanId = loanId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    @JsonProperty("isPaid")
    public boolean isPaid() { return isPaid; }
    @JsonProperty("isPaid")
    public void setPaid(boolean paid) { isPaid = paid; }
}
