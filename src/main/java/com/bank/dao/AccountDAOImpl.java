package com.bank.dao;

import com.bank.config.DatabaseConnection;
import com.bank.model.Account;
import com.bank.model.TransactionRecord;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public long createAccount(String fullName, String email, BigDecimal initialDeposit) throws SQLException {
        String insertAccountSql = "INSERT INTO accounts (full_name, email, balance) VALUES (?, ?, ?)";
        String insertTxSql = "INSERT INTO transactions (account_number, type, amount, balance_after) VALUES (?, 'DEPOSIT', ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psAcc = conn.prepareStatement(insertAccountSql, Statement.RETURN_GENERATED_KEYS)) {
                psAcc.setString(1, fullName);
                psAcc.setString(2, email);
                psAcc.setBigDecimal(3, initialDeposit);
                psAcc.executeUpdate();

                long generatedAccNum = -1;
                try (ResultSet rs = psAcc.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedAccNum = rs.getLong(1);
                    }
                }

                if (generatedAccNum == -1) {
                    conn.rollback();
                    throw new SQLException("Failed to retrieve account number.");
                }

                try (PreparedStatement psTx = conn.prepareStatement(insertTxSql)) {
                    psTx.setLong(1, generatedAccNum);
                    psTx.setBigDecimal(2, initialDeposit);
                    psTx.setBigDecimal(3, initialDeposit);
                    psTx.executeUpdate();
                }

                conn.commit();
                return generatedAccNum;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    @Override
    public Account getAccountByNumber(long accountNumber) throws SQLException {
        String sql = "SELECT account_number, full_name, email, balance, created_at FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getLong("account_number"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getBigDecimal("balance"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public boolean deposit(long accountNumber, BigDecimal amount) throws SQLException {
        String lockSql = "SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE";
        String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        String txSql = "INSERT INTO transactions (account_number, type, amount, balance_after) VALUES (?, 'DEPOSIT', ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement lockPs = conn.prepareStatement(lockSql);
                 PreparedStatement updatePs = conn.prepareStatement(updateSql);
                 PreparedStatement txPs = conn.prepareStatement(txSql)) {

                lockPs.setLong(1, accountNumber);
                BigDecimal currentBalance;
                try (ResultSet rs = lockPs.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    currentBalance = rs.getBigDecimal("balance");
                }

                BigDecimal newBalance = currentBalance.add(amount);

                updatePs.setBigDecimal(1, amount);
                updatePs.setLong(2, accountNumber);
                updatePs.executeUpdate();

                txPs.setLong(1, accountNumber);
                txPs.setBigDecimal(2, amount);
                txPs.setBigDecimal(3, newBalance);
                txPs.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    @Override
    public boolean withdraw(long accountNumber, BigDecimal amount) throws SQLException {
        String lockSql = "SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE";
        String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
        String txSql = "INSERT INTO transactions (account_number, type, amount, balance_after) VALUES (?, 'WITHDRAWAL', ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement lockPs = conn.prepareStatement(lockSql);
                 PreparedStatement updatePs = conn.prepareStatement(updateSql);
                 PreparedStatement txPs = conn.prepareStatement(txSql)) {

                lockPs.setLong(1, accountNumber);
                BigDecimal currentBalance;
                try (ResultSet rs = lockPs.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    currentBalance = rs.getBigDecimal("balance");
                }

                if (currentBalance.compareTo(amount) < 0) {
                    conn.rollback();
                    System.out.println("[Error] Insufficient balance.");
                    return false;
                }

                BigDecimal newBalance = currentBalance.subtract(amount);

                updatePs.setBigDecimal(1, amount);
                updatePs.setLong(2, accountNumber);
                updatePs.executeUpdate();

                txPs.setLong(1, accountNumber);
                txPs.setBigDecimal(2, amount);
                txPs.setBigDecimal(3, newBalance);
                txPs.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    @Override
    public boolean transferFunds(long senderAccount, long receiverAccount, BigDecimal amount) throws SQLException {
        if (senderAccount == receiverAccount) return false;

        long firstLock = Math.min(senderAccount, receiverAccount);
        long secondLock = Math.max(senderAccount, receiverAccount);

        String lockSql = "SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE";
        String debitSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
        String creditSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        String txSql = "INSERT INTO transactions (account_number, type, amount, balance_after) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement lockPs = conn.prepareStatement(lockSql);
                 PreparedStatement debitPs = conn.prepareStatement(debitSql);
                 PreparedStatement creditPs = conn.prepareStatement(creditSql);
                 PreparedStatement txPs = conn.prepareStatement(txSql)) {

                lockPs.setLong(1, firstLock);
                ResultSet rs1 = lockPs.executeQuery();
                if (!rs1.next()) { conn.rollback(); return false; }

                lockPs.setLong(1, secondLock);
                ResultSet rs2 = lockPs.executeQuery();
                if (!rs2.next()) { conn.rollback(); return false; }

                lockPs.setLong(1, senderAccount);
                BigDecimal senderBalance;
                try (ResultSet rs = lockPs.executeQuery()) {
                    rs.next();
                    senderBalance = rs.getBigDecimal("balance");
                }

                if (senderBalance.compareTo(amount) < 0) {
                    conn.rollback();
                    return false;
                }

                lockPs.setLong(1, receiverAccount);
                BigDecimal receiverBalance;
                try (ResultSet rs = lockPs.executeQuery()) {
                    rs.next();
                    receiverBalance = rs.getBigDecimal("balance");
                }

                debitPs.setBigDecimal(1, amount);
                debitPs.setLong(2, senderAccount);
                debitPs.executeUpdate();

                creditPs.setBigDecimal(1, amount);
                creditPs.setLong(2, receiverAccount);
                creditPs.executeUpdate();

                txPs.setLong(1, senderAccount);
                txPs.setString(2, "TRANSFER_OUT");
                txPs.setBigDecimal(3, amount);
                txPs.setBigDecimal(4, senderBalance.subtract(amount));
                txPs.executeUpdate();

                txPs.setLong(1, receiverAccount);
                txPs.setString(2, "TRANSFER_IN");
                txPs.setBigDecimal(3, amount);
                txPs.setBigDecimal(4, receiverBalance.add(amount));
                txPs.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    @Override
    public List<TransactionRecord> getTransactionHistory(long accountNumber) throws SQLException {
        String sql = "SELECT transaction_id, account_number, type, amount, balance_after, created_at FROM transactions WHERE account_number = ? ORDER BY created_at DESC LIMIT 50";
        List<TransactionRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TransactionRecord(
                            rs.getLong("transaction_id"),
                            rs.getLong("account_number"),
                            rs.getString("type"),
                            rs.getBigDecimal("amount"),
                            rs.getBigDecimal("balance_after"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return list;
    }
}
