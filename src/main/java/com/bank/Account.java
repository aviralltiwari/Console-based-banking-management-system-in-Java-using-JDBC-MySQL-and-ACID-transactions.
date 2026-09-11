package com.bank.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Account {
    private long accountNumber;
    private String fullName;
    private String email;
    private BigDecimal balance;
    private Timestamp createdAt;

    public Account() {}

    public Account(long accountNumber, String fullName, String email, BigDecimal balance, Timestamp createdAt) {
        this.accountNumber = accountNumber;
        this.fullName = fullName;
        this.email = email;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public long getAccountNumber() { return accountNumber; }
    public void setAccountNumber(long accountNumber) { this.accountNumber = accountNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Account #%d | Name: %s | Email: %s | Balance: $%.2f",
                accountNumber, fullName, email, balance);
    }
}
