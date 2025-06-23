package com.creditapi.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PayInstallmentResponseDTO {
    private int numberOfInstallmentsPaid;
    private BigDecimal totalAmountSpent;
    private boolean loanFullyPaid;
    private List<InstallmentPaymentDetail> paidInstallments;

    public static class InstallmentPaymentDetail {
        private UUID installmentId;
        private BigDecimal paidAmount;
        private String paymentDate;
        private boolean isReward;
        private boolean isPenalty;
        // getters and setters
        public UUID getInstallmentId() { return installmentId; }
        public void setInstallmentId(UUID installmentId) { this.installmentId = installmentId; }
        public BigDecimal getPaidAmount() { return paidAmount; }
        public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
        public String getPaymentDate() { return paymentDate; }
        public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
        public boolean isReward() { return isReward; }
        public void setReward(boolean reward) { isReward = reward; }
        public boolean isPenalty() { return isPenalty; }
        public void setPenalty(boolean penalty) { isPenalty = penalty; }
    }

    // getters and setters
    public int getNumberOfInstallmentsPaid() { return numberOfInstallmentsPaid; }
    public void setNumberOfInstallmentsPaid(int numberOfInstallmentsPaid) { this.numberOfInstallmentsPaid = numberOfInstallmentsPaid; }
    public BigDecimal getTotalAmountSpent() { return totalAmountSpent; }
    public void setTotalAmountSpent(BigDecimal totalAmountSpent) { this.totalAmountSpent = totalAmountSpent; }
    public boolean isLoanFullyPaid() { return loanFullyPaid; }
    public void setLoanFullyPaid(boolean loanFullyPaid) { this.loanFullyPaid = loanFullyPaid; }
    public List<InstallmentPaymentDetail> getPaidInstallments() { return paidInstallments; }
    public void setPaidInstallments(List<InstallmentPaymentDetail> paidInstallments) { this.paidInstallments = paidInstallments; }
}
