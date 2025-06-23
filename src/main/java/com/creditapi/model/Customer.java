package com.creditapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class Customer {
    @Id
    private UUID id;
    private String name;
    private String surname;
    private BigDecimal creditLimit;
    private BigDecimal usedCreditLimit;

    public Customer() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public BigDecimal getUsedCreditLimit() { return usedCreditLimit; }
    public void setUsedCreditLimit(BigDecimal usedCreditLimit) { this.usedCreditLimit = usedCreditLimit; }
}
