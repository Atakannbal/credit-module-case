package com.creditapi.exception;

public class InsufficientCreditLimitException extends RuntimeException {
    public InsufficientCreditLimitException(String message) {
        super(message);
    }
}
