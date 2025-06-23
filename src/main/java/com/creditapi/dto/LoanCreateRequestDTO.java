package com.creditapi.dto;
import java.math.BigDecimal;
import java.util.UUID;
import com.creditapi.model.InstallmentOption;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.AssertTrue;

public class LoanCreateRequestDTO {
    @NotNull
    private UUID customerId;

    @NotNull
    @Positive
    private BigDecimal loanAmount;

    @NotNull
    @Positive
    private Integer numberOfInstallments;
    
    @NotNull
    @DecimalMin(value = "0.1", inclusive = true)
    @DecimalMax(value = "0.5", inclusive = true)
    private Double interestRate;

    public LoanCreateRequestDTO() {}

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
    public Integer getNumberOfInstallments() { return numberOfInstallments; }
    public void setNumberOfInstallments(Integer numberOfInstallments) { this.numberOfInstallments = numberOfInstallments; }

    @AssertTrue(message = "numberOfInstallments must be one of 6, 9, 12, 24")
    public boolean isValidInstallmentOption() {
        return numberOfInstallments != null && InstallmentOption.isValid(numberOfInstallments);
    }
}
