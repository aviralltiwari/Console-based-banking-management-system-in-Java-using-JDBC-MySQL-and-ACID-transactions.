package com.bank;

import com.bank.dao.AccountDAO;
import com.bank.dao.AccountDAOImpl;
import com.bank.model.Account;
import com.bank.model.TransactionRecord;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final AccountDAO accountDAO = new AccountDAOImpl();

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("   WELCOME TO JAVA BANKING CLI SYSTEM      ");
        System.out.println("===========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> handleCreateAccount();
                case 2 -> handleViewAccount();
                case 3 -> handleDeposit();
                case 4 -> handleWithdraw();
                case 5 -> handleTransfer();
                case 6 -> handleViewHistory();
                case 7 -> {
                    System.out.println("\nThank you for banking with us. Goodbye!");
                    running = false;
                }
                default -> System.out.println("\n[!] Invalid choice. Please select 1 through 7.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n------------- MAIN MENU -------------");
        System.out.println("1. Open New Account");
        System.out.println("2. View Account & Balance");
        System.out.println("3. Deposit Funds");
        System.out.println("4. Withdraw Funds");
        System.out.println("5. Transfer Funds");
        System.out.println("6. View Transaction Statement");
        System.out.println("7. Exit");
        System.out.println("-------------------------------------");
    }

    private static void handleCreateAccount() {
        System.out.println("\n--- Open a New Account ---");
        System.out.print("Enter full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter email address: ");
        String email = scanner.nextLine().trim();

        BigDecimal initialDeposit = readBigDecimalInput("Enter initial deposit amount: ");
        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("[!] Initial deposit cannot be negative.");
            return;
        }

        try {
            long newAccNum = accountDAO.createAccount(name, email, initialDeposit);
            System.out.println("\n[✔] Account created successfully!");
            System.out.println("    Assigned Account Number: " + newAccNum);
            System.out.println("    Holder: " + name);
            System.out.println("    Starting Balance: $" + initialDeposit);
        } catch (SQLException e) {
            System.out.println("[X] Error creating account: " + e.getMessage());
        }
    }

    private static void handleViewAccount() {
        long accNum = readLongInput("\nEnter Account Number: ");
        try {
            Account account = accountDAO.getAccountByNumber(accNum);
            if (account != null) {
                System.out.println("\n--- Account Details ---");
                System.out.println("Account Number : " + account.getAccountNumber());
                System.out.println("Account Holder : " + account.getFullName());
                System.out.println("Email Address  : " + account.getEmail());
                System.out.printf("Current Balance: $%.2f%n", account.getBalance());
                System.out.println("Opened On      : " + account.getCreatedAt());
            } else {
                System.out.println("[!] No account found with number: " + accNum);
            }
        } catch (SQLException e) {
            System.out.println("[X] Database error: " + e.getMessage());
        }
    }

    private static void handleDeposit() {
        long accNum = readLongInput("\nEnter Account Number: ");
        BigDecimal amount = readBigDecimalInput("Enter Deposit Amount: ");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("[!] Deposit amount must be greater than zero.");
            return;
        }

        try {
            boolean success = accountDAO.deposit(accNum, amount);
            if (success) {
                System.out.printf("[✔] Successfully deposited $%.2f into Account #%d%n", amount, accNum);
            } else {
                System.out.println("[!] Deposit failed. Account does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("[X] Database error: " + e.getMessage());
        }
    }

    private static void handleWithdraw() {
        long accNum = readLongInput("\nEnter Account Number: ");
        BigDecimal amount = readBigDecimalInput("Enter Withdrawal Amount: ");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("[!] Withdrawal amount must be greater than zero.");
            return;
        }

        try {
            boolean success = accountDAO.withdraw(accNum, amount);
            if (success) {
                System.out.printf("[✔] Successfully withdrew $%.2f from Account #%d%n", amount, accNum);
            } else {
                System.out.println("[!] Withdrawal failed.");
            }
        } catch (SQLException e) {
            System.out.println("[X] Database error: " + e.getMessage());
        }
    }

    private static void handleTransfer() {
        long fromAcc = readLongInput("\nEnter Sender Account Number: ");
        long toAcc = readLongInput("Enter Receiver Account Number: ");
        BigDecimal amount = readBigDecimalInput("Enter Amount to Transfer: ");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("[!] Transfer amount must be greater than zero.");
            return;
        }

        try {
            boolean success = accountDAO.transferFunds(fromAcc, toAcc, amount);
            if (success) {
                System.out.printf("[✔] Transfer complete: $%.2f transferred from #%d to #%d%n",
                        amount, fromAcc, toAcc);
            } else {
                System.out.println("[!] Transfer failed.");
            }
        } catch (SQLException e) {
            System.out.println("[X] Database error: " + e.getMessage());
        }
    }

    private static void handleViewHistory() {
        long accNum = readLongInput("\nEnter Account Number: ");
        try {
            List<TransactionRecord> list = accountDAO.getTransactionHistory(accNum);
            if (list.isEmpty()) {
                System.out.println("[i] No transactions found for Account #" + accNum);
                return;
            }

            System.out.println("\n----------------- TRANSACTION HISTORY (Account #" + accNum + ") -----------------");
            System.out.printf("%-6s | %-14s | %-12s | %-14s | %s%n", "TX ID", "TYPE", "AMOUNT", "BALANCE AFTER", "DATE/TIME");
            System.out.println("-----------------------------------------------------------------------------");
            for (TransactionRecord tx : list) {
                System.out.printf("%-6d | %-14s | $%-11.2f | $%-13.2f | %s%n",
                        tx.getTransactionId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getBalanceAfter(),
                        tx.getCreatedAt());
            }
            System.out.println("-----------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("[X] Database error: " + e.getMessage());
        }
    }

    private static int readIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid number format. Please enter an integer.");
            }
        }
    }

    private static long readLongInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid format. Please enter a valid numerical account number.");
            }
        }
    }

    private static BigDecimal readBigDecimalInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                BigDecimal val = new BigDecimal(input);
                if (val.scale() > 2) {
                    System.out.println("[!] Amounts cannot have more than 2 decimal places.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid currency format. Example: 150.00");
            }
        }
    }
}
