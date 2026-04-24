package com.example.expensetracker.utils;

import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.DashboardSummary;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.models.Income;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ExpenseAnalytics {

    private ExpenseAnalytics() {
    }

    public static DashboardSummary buildSummary(List<Expense> expenses, List<Income> incomes, Budget budget) {
        double totalExpenses = 0;
        double currentMonthSpending = 0;
        double currentMonthBudgetSpend = 0;
        double incomeBalanceSpentTotal = 0;
        double incomeBalanceSpentThisMonth = 0;
        int currentMonthExpenseTransactions = 0;
        double totalIncome = 0;
        double currentMonthIncome = 0;
        int currentMonthIncomeTransactions = 0;
        String currentMonthKey = FormatUtils.monthKeyFromMillis(System.currentTimeMillis());
        for (Expense expense : expenses) {
            if (expense == null) {
                continue;
            }
            totalExpenses += expense.getAmount();
            if (FormatUtils.isSameMonth(expense.getDate(), currentMonthKey)) {
                currentMonthSpending += expense.getAmount();
                currentMonthExpenseTransactions++;
                if (isMonthlyBudgetExpense(expense)) {
                    currentMonthBudgetSpend += expense.getAmount();
                } else {
                    incomeBalanceSpentThisMonth += expense.getAmount();
                }
            }
            if (isIncomeBalanceExpense(expense)) {
                incomeBalanceSpentTotal += expense.getAmount();
            }
        }

        for (Income income : incomes) {
            if (income == null) {
                continue;
            }
            totalIncome += income.getAmount();
            if (FormatUtils.isSameMonth(income.getDate(), currentMonthKey)) {
                currentMonthIncome += income.getAmount();
                currentMonthIncomeTransactions++;
            }
        }

        double remainingBudget = budget != null ? budget.getAmount() - currentMonthBudgetSpend : 0;
        double remainingIncomeBalance = totalIncome - incomeBalanceSpentTotal;
        double monthlyNet = currentMonthIncome - currentMonthSpending;
        double savingsRate = currentMonthIncome > 0 ? (monthlyNet / currentMonthIncome) * 100d : 0d;

        return new DashboardSummary(
                totalExpenses,
                currentMonthSpending,
                currentMonthBudgetSpend,
                remainingBudget,
                currentMonthExpenseTransactions,
                totalIncome,
                currentMonthIncome,
                currentMonthIncomeTransactions,
                incomeBalanceSpentTotal,
                incomeBalanceSpentThisMonth,
                remainingIncomeBalance,
                monthlyNet,
                savingsRate
        );
    }

    public static Map<String, Float> buildCategoryTotals(List<Expense> expenses) {
        Map<String, Float> totals = new LinkedHashMap<>();
        for (Expense expense : expenses) {
            if (expense == null) {
                continue;
            }
            String category = expense.getCategory();
            if (category == null || category.trim().isEmpty()) {
                category = "Other";
            }
            float current = totals.containsKey(category) ? totals.get(category) : 0f;
            totals.put(category, current + (float) expense.getAmount());
        }
        return totals;
    }

    public static Map<String, Float> buildIncomeReasonTotals(List<Income> incomes) {
        Map<String, Float> totals = new LinkedHashMap<>();
        for (Income income : incomes) {
            if (income == null) {
                continue;
            }
            String reason = income.getReason();
            if (reason == null || reason.trim().isEmpty()) {
                reason = "Other";
            }
            float current = totals.containsKey(reason) ? totals.get(reason) : 0f;
            totals.put(reason, current + (float) income.getAmount());
        }
        return totals;
    }

    public static LinkedHashMap<String, Float> buildMonthlyTotals(List<Expense> expenses, int monthsBack) {
        LinkedHashMap<String, Float> totals = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        for (int index = monthsBack - 1; index >= 0; index--) {
            Calendar monthCalendar = (Calendar) calendar.clone();
            monthCalendar.add(Calendar.MONTH, -index);
            String monthLabel = FormatUtils.formatMonthLabel(monthCalendar.getTimeInMillis());
            totals.put(monthLabel, 0f);
        }

        for (Expense expense : expenses) {
            if (expense == null) {
                continue;
            }
            String monthLabel = FormatUtils.formatMonthLabel(expense.getDate());
            if (totals.containsKey(monthLabel)) {
                totals.put(monthLabel, totals.get(monthLabel) + (float) expense.getAmount());
            }
        }
        return totals;
    }

    public static LinkedHashMap<String, Float> buildMonthlyIncomeTotals(List<Income> incomes, int monthsBack) {
        LinkedHashMap<String, Float> totals = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        for (int index = monthsBack - 1; index >= 0; index--) {
            Calendar monthCalendar = (Calendar) calendar.clone();
            monthCalendar.add(Calendar.MONTH, -index);
            String monthLabel = FormatUtils.formatMonthLabel(monthCalendar.getTimeInMillis());
            totals.put(monthLabel, 0f);
        }

        for (Income income : incomes) {
            if (income == null) {
                continue;
            }
            String monthLabel = FormatUtils.formatMonthLabel(income.getDate());
            if (totals.containsKey(monthLabel)) {
                totals.put(monthLabel, totals.get(monthLabel) + (float) income.getAmount());
            }
        }
        return totals;
    }

    public static double getSpentForMonth(List<Expense> expenses, String monthKey) {
        double total = 0;
        for (Expense expense : expenses) {
            if (expense == null) {
                continue;
            }
            if (FormatUtils.isSameMonth(expense.getDate(), monthKey) && isMonthlyBudgetExpense(expense)) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    public static double getTotalExpenseForMonth(List<Expense> expenses, String monthKey) {
        double total = 0;
        for (Expense expense : expenses) {
            if (expense != null && FormatUtils.isSameMonth(expense.getDate(), monthKey)) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    public static double getIncomeBalanceSpentTotal(List<Expense> expenses) {
        double total = 0;
        for (Expense expense : expenses) {
            if (isIncomeBalanceExpense(expense)) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    public static double getIncomeBalanceSpentForMonth(List<Expense> expenses, String monthKey) {
        double total = 0;
        for (Expense expense : expenses) {
            if (expense == null) {
                continue;
            }
            if (FormatUtils.isSameMonth(expense.getDate(), monthKey) && isIncomeBalanceExpense(expense)) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    public static double getTotalIncome(List<Income> incomes) {
        double total = 0;
        for (Income income : incomes) {
            if (income != null) {
                total += income.getAmount();
            }
        }
        return total;
    }

    public static double getIncomeForMonth(List<Income> incomes, String monthKey) {
        double total = 0;
        for (Income income : incomes) {
            if (income != null && FormatUtils.isSameMonth(income.getDate(), monthKey)) {
                total += income.getAmount();
            }
        }
        return total;
    }

    public static double getIncomeBalanceRemaining(List<Expense> expenses, List<Income> incomes) {
        return getTotalIncome(incomes) - getIncomeBalanceSpentTotal(expenses);
    }

    public static boolean isMonthlyBudgetExpense(Expense expense) {
        if (expense == null || expense.getPaymentSource() == null || expense.getPaymentSource().trim().isEmpty()) {
            return true;
        }
        return AppConstants.EXPENSE_SOURCE_MONTHLY_BUDGET.equals(expense.getPaymentSource());
    }

    public static boolean isIncomeBalanceExpense(Expense expense) {
        return expense != null && AppConstants.EXPENSE_SOURCE_INCOME_BALANCE.equals(expense.getPaymentSource());
    }

    public static List<Long> extractDates(List<Expense> expenses) {
        List<Long> dates = new ArrayList<>();
        for (Expense expense : expenses) {
            if (expense != null) {
                dates.add(expense.getDate());
            }
        }
        Collections.sort(dates, Collections.reverseOrder());
        return dates;
    }

    public static String getHighestCategory(List<Expense> expenses) {
        Map<String, Float> totals = buildCategoryTotals(expenses);
        String category = "-";
        float max = 0f;
        for (Map.Entry<String, Float> entry : totals.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                category = entry.getKey();
            }
        }
        return category;
    }

    public static String getHighestIncomeReason(List<Income> incomes) {
        Map<String, Float> totals = buildIncomeReasonTotals(incomes);
        String reason = "-";
        float max = 0f;
        for (Map.Entry<String, Float> entry : totals.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                reason = entry.getKey();
            }
        }
        return reason;
    }

    public static String monthLabelFromKey(String monthKey) {
        if (monthKey == null || monthKey.trim().isEmpty()) {
            return "-";
        }
        try {
            String[] split = monthKey.split("-");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(split[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(split[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return FormatUtils.formatMonthLabel(calendar.getTimeInMillis());
        } catch (Exception exception) {
            return monthKey.toUpperCase(Locale.getDefault());
        }
    }
}
