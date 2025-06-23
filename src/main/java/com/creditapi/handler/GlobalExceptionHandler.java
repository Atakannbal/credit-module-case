package com.creditapi.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.creditapi.exception.CustomerNotFoundException;
import com.creditapi.exception.LoanNotFoundException;
import com.creditapi.exception.InsufficientCreditLimitException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponse buildErrorResponse(HttpStatus status, String message, String error, String path) {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return new ErrorResponse(timestamp, status.value(), error, message, path);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse body = buildErrorResponse(HttpStatus.BAD_REQUEST, message, "Validation Error", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleDeserializationException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Invalid request: " + ex.getMostSpecificCause().getMessage();
        ErrorResponse body = buildErrorResponse(HttpStatus.BAD_REQUEST, message, "Malformed JSON", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        if (ex.getMessage() != null && (ex.getMessage().contains("Customer not found") || ex.getMessage().contains("Loan not found"))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        ErrorResponse body = buildErrorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), "Response Status Exception", request.getRequestURI());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse body = buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied", "Access Denied", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse body = buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", "Bad Credentials", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse body = buildErrorResponse(HttpStatus.BAD_REQUEST, message, "Constraint Violation", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex, HttpServletRequest request) {
        logger.warn("Customer not found: {}", ex.getMessage());
        ErrorResponse body = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "Customer Not Found", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(LoanNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleLoanNotFoundException(LoanNotFoundException ex, HttpServletRequest request) {
        logger.warn("Loan not found: {}", ex.getMessage());
        ErrorResponse body = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "Loan Not Found", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InsufficientCreditLimitException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInsufficientCreditLimitException(InsufficientCreditLimitException ex, HttpServletRequest request) {
        logger.warn("Insufficient credit limit: {}", ex.getMessage());
        ErrorResponse body = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "Insufficient Credit Limit", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        ErrorResponse body = buildErrorResponse(HttpStatus.BAD_REQUEST, message, "Type Mismatch", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
