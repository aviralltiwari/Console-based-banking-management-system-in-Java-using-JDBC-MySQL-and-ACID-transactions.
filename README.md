# Bank Account & Transaction Management System (Java + MySQL)

A robust console-based banking application built with core Java and MySQL using JDBC. The project demonstrates backend architecture principles, Data Access Object (DAO) pattern design, and ACID-compliant transaction handling with pessimistic row locking.

## Features
- **Account Management**: Create accounts, query profiles, and check real-time balances.
- **ACID Fund Transfers**: Multi-account balance transfers wrapped in transactional boundaries with automatic rollback on failure.
- **Pessimistic Locking**: Prevents race conditions and dirty reads during simultaneous withdrawals or transfers using SQL `FOR UPDATE`.
- **Passbook / Audit Log**: Tracks full transaction history (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`) per account.
- **Defensive Input Handling**: Robust validation for monetary amounts and account lookups.

## Tech Stack
- **Language**: Java 17+
- **Database**: MySQL 8+
- **Driver**: MySQL Connector/J (`com.mysql:mysql-connector-j`)
- **Build Tool**: Apache Maven

## Database Setup
Run the following script in MySQL Workbench or MySQL CLI:

```sql
CREATE DATABASE IF NOT EXISTS bank_db;
USE bank_db;

CREATE TABLE IF NOT EXISTS accounts (
    account_number BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_number BIGINT NOT NULL,
    type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    balance_after DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);
