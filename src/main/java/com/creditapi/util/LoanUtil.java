package com.creditapi.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanUtil {
    private LoanUtil() {}

    public static BigDecimal calculateTotalToBePaid(BigDecimal amount, double interestRate) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.multiply(BigDecimal.valueOf(1 + interestRate));
    }

    public static LocalDate calculateFirstPaymentDate(LocalDateTime createDate) {
        if (createDate == null) return null;
        return createDate.toLocalDate().plusMonths(1).withDayOfMonth(1);
    }
}
