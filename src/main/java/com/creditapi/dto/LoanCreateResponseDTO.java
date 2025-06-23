package com.creditapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class LoanCreateResponseDTO {
    private UUID id;
    private UUID customerId;
    private BigDecimal loanAmount;
    private Integer numberOfInstallments;
    private Double interestRate;
    private LocalDate createDate;
    private BigDecimal paymentAmount;
    private LocalDate firstPaymentDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public Integer getNumberOfInstallments() { return numberOfInstallments; }
    public void setNumberOfInstallments(Integer numberOfInstallments) { this.numberOfInstallments = numberOfInstallments; }
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
    public LocalDate getCreateDate() { return createDate; }
    public void setCreateDate(LocalDate createDate) { this.createDate = createDate; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public LocalDate getFirstPaymentDate() { return firstPaymentDate; }
    public void setFirstPaymentDate(LocalDate firstPaymentDate) { this.firstPaymentDate = firstPaymentDate; }
}
