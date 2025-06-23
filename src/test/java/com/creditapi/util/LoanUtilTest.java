package com.creditapi.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LoanUtilTest {
    @Test
    void calculatesTotalToBePaidCorrectly() {
        BigDecimal amount = new BigDecimal("1000");
        double interestRate = 0.2;
        BigDecimal expected = new BigDecimal("1200.0");
        BigDecimal actual = LoanUtil.calculateTotalToBePaid(amount, interestRate);
        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    void returnsZeroIfAmountIsNull() {
        double interestRate = 0.2;
        BigDecimal actual = LoanUtil.calculateTotalToBePaid(null, interestRate);
        assertEquals(BigDecimal.ZERO, actual);
    }

    @Test
    void calculatesWithZeroInterest() {
        BigDecimal amount = new BigDecimal("1000");
        double interestRate = 0.0;
        BigDecimal expected = new BigDecimal("1000.0");
        BigDecimal actual = LoanUtil.calculateTotalToBePaid(amount, interestRate);
        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    void calculatesFirstPaymentDateCorrectly() {
        LocalDateTime createDate = LocalDateTime.of(2025, 6, 23, 10, 0);
        LocalDate expected = LocalDate.of(2025, 7, 1);
        LocalDate actual = LoanUtil.calculateFirstPaymentDate(createDate);
        assertEquals(expected, actual);
    }

    @Test
    void returnsNullIfCreateDateIsNull() {
        assertNull(LoanUtil.calculateFirstPaymentDate(null));
    }
}
