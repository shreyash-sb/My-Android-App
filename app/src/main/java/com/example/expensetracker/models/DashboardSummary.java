package com.example.expensetracker.models;

public class DashboardSummary {

    private final double totalExpenses;
    private final double currentMonthSpending;
    private final double currentMonthBudgetSpend;
    private final double remainingBudget;
    private final int expenseTransactionCount;
    private final double totalIncome;
    private final double currentMonthIncome;
    private final int incomeTransactionCount;
    private final double incomeBalanceSpentTotal;
    private final double incomeBalanceSpentThisMonth;
    private final double remainingIncomeBalance;
    private final double monthlyNet;
    private final double savingsRate;

    public DashboardSummary(double totalExpenses,
                            double currentMonthSpending,
                            double currentMonthBudgetSpend,
                            double remainingBudget,
                            int expenseTransactionCount,
                            double totalIncome,
                            double currentMonthIncome,
                            int incomeTransactionCount,
                            double incomeBalanceSpentTotal,
                            double incomeBalanceSpentThisMonth,
                            double remainingIncomeBalance,
                            double monthlyNet,
                            double savingsRate) {
        this.totalExpenses = totalExpenses;
        this.currentMonthSpending = currentMonthSpending;
        this.currentMonthBudgetSpend = currentMonthBudgetSpend;
        this.remainingBudget = remainingBudget;
        this.expenseTransactionCount = expenseTransactionCount;
        this.totalIncome = totalIncome;
        this.currentMonthIncome = currentMonthIncome;
        this.incomeTransactionCount = incomeTransactionCount;
        this.incomeBalanceSpentTotal = incomeBalanceSpentTotal;
        this.incomeBalanceSpentThisMonth = incomeBalanceSpentThisMonth;
        this.remainingIncomeBalance = remainingIncomeBalance;
        this.monthlyNet = monthlyNet;
        this.savingsRate = savingsRate;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public double getCurrentMonthSpending() {
        return currentMonthSpending;
    }

    public double getCurrentMonthBudgetSpend() {
        return currentMonthBudgetSpend;
    }

    public double getRemainingBudget() {
        return remainingBudget;
    }

    public int getExpenseTransactionCount() {
        return expenseTransactionCount;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getCurrentMonthIncome() {
        return currentMonthIncome;
    }

    public int getIncomeTransactionCount() {
        return incomeTransactionCount;
    }

    public double getIncomeBalanceSpentTotal() {
        return incomeBalanceSpentTotal;
    }

    public double getIncomeBalanceSpentThisMonth() {
        return incomeBalanceSpentThisMonth;
    }

    public double getRemainingIncomeBalance() {
        return remainingIncomeBalance;
    }

    public double getMonthlyNet() {
        return monthlyNet;
    }

    public double getSavingsRate() {
        return savingsRate;
    }
}
