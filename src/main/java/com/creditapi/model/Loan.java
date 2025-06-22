package com.creditapi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    private UUID customerId;

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentOption numberOfInstallments;

    @Column(nullable = false)
    private double interestRate;
    
    private LocalDateTime createDate;
    private boolean isPaid = false;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanInstallment> installments = new ArrayList<>();

    public Loan() {}

    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public InstallmentOption getNumberOfInstallments() { return numberOfInstallments; }
    public void setNumberOfInstallments(InstallmentOption numberOfInstallments) { this.numberOfInstallments = numberOfInstallments; }
    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    public List<LoanInstallment> getInstallments() { return installments; }
    public void setInstallments(List<LoanInstallment> installments) { this.installments = installments; }
}
