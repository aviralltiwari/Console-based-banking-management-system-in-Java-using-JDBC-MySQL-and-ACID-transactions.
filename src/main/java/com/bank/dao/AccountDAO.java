package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.TransactionRecord;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface AccountDAO {
    long createAccount(String fullName, String email, BigDecimal initialDeposit) throws SQLException;
    Account getAccountByNumber(long accountNumber) throws SQLException;
    boolean deposit(long accountNumber, BigDecimal amount) throws SQLException;
    boolean withdraw(long accountNumber, BigDecimal amount) throws SQLException;
    boolean transferFunds(long senderAccount, long receiverAccount, BigDecimal amount) throws SQLException;
    List<TransactionRecord> getTransactionHistory(long accountNumber) throws SQLException;
}
