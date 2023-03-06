package com.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UserWalletService {
    private final Connection conn;

    public UserWalletService(Connection conn) {
        this.conn = conn;
    }
    // 查询余额
    public BigDecimal getBalance(int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT balance FROM user_wallet WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                } else {
                    throw new SQLException("User wallet not found");
                }
            }
        }
    }

    public void makePurchase(int userId, BigDecimal amount, int referenceId) throws SQLException {
        BigDecimal balance = getBalance(userId);
        if (balance.compareTo(amount) < 0) {
            throw new SQLException("Insufficient balance");
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE user_wallet SET balance = balance - ? WHERE user_id = ?")) {
            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO user_wallet_transactions (user_id, transaction_type, amount, balance, reference_id) " +
                        "VALUES (?, 'PURCHASE', ?, ?, ?)")) {
            stmt.setInt(1, userId);
            stmt.setBigDecimal(2, amount);
            stmt.setBigDecimal(3, balance.subtract(amount));
            stmt.setInt(4, referenceId);
            stmt.executeUpdate();
        }
    }

    public void requestRefund(int userId, BigDecimal amount, int referenceId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT balance FROM user_wallet WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1 FOR UPDATE")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal balance = rs.getBigDecimal("balance");
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE user_wallet SET balance = balance + ? WHERE user_id = ?")) {
                        updateStmt.setBigDecimal(1, amount);
                        updateStmt.setInt(2, userId);
                        updateStmt.executeUpdate();
                    }

                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO user_wallet_transactions (user_id, transaction_type, amount, balance, reference_id) " +
                                    "VALUES (?, 'REFUND', ?, ?, ?)")) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setBigDecimal(2, amount);
                        insertStmt.setBigDecimal(3, balance.add(amount));
                        insertStmt.setInt(4, referenceId);
                        insertStmt.executeUpdate();
                    }
                } else {
                    throw new
                            SQLException("User wallet not found");
                }
            }
        }
    }

    public List<UserWalletTransaction> getTransactionHistory(int userId) throws SQLException {
        List<UserWalletTransaction> transactions = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM user_wallet_transactions WHERE user_id = ? ORDER BY created_at DESC")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UserWalletTransaction transaction = new UserWalletTransaction(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            UserWalletTransactionType.valueOf(rs.getString("transaction_type")),
                            rs.getBigDecimal("amount"),
                            rs.getBigDecimal("balance"),
                            rs.getInt("reference_id"),
                            rs.getTimestamp("created_at").toInstant()
                    );
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }
}

class UserWalletTransaction {
    private final int id;
    private final int userId;
    private final UserWalletTransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balance;
    private final int referenceId;
    private final Instant createdAt;
    public UserWalletTransaction(int id, int userId, UserWalletTransactionType type,
                                 BigDecimal amount, BigDecimal balance, int referenceId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balance = balance;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public UserWalletTransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

enum UserWalletTransactionType {
    DEPOSIT,
    PURCHASE,
    REFUND,
    WITHDRAW
}
