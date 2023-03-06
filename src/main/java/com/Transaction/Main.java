package com.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 假设当前用户的 ID 为 1
        int userId = 1;

        try {
            // 查询用户钱包余额
            BigDecimal balance = UserWalletService.getInstance().getBalance(userId);
            System.out.println("User wallet balance: " + balance);

            // 用户消费减额接口
            BigDecimal purchaseAmount = new BigDecimal("100.00");
            int orderId = 123;
            UserWalletService.getInstance().purchase(userId, purchaseAmount, orderId);
            System.out.println("User purchased with amount: " + purchaseAmount);

            // 用户退款接口
            BigDecimal refundAmount = new BigDecimal("20.00");
            int refundId = 456;
            UserWalletService.getInstance().refund(userId, refundAmount, refundId);
            System.out.println("User refunded with amount: " + refundAmount);

            // 查询用户钱包金额变动明细的接口
            List<UserWalletTransaction> transactions = UserWalletService.getInstance().getTransactionHistory(userId);
            System.out.println("User wallet transaction history:");
            for (UserWalletTransaction transaction : transactions) {
                System.out.println(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
