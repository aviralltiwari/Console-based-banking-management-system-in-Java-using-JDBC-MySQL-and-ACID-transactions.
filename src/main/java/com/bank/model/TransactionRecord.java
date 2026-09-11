package com.bank.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TransactionRecord {
    private long transactionId;
    private long accountNumber;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Timestamp createdAt;

    public TransactionRecord(long transactionId, long accountNumber, String type,
                             BigDecimal amount, BigDecimal balanceAfter, Timestamp createdAt) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public long getTransactionId() { return transactionId; }
    public long getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public Timestamp getCreatedAt() { return createdAt; }
}
