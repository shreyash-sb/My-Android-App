package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityBudgetBinding;
import com.example.expensetracker.firebase.BudgetRepository;
import com.example.expensetracker.firebase.DataCallback;
import com.example.expensetracker.firebase.ExpenseRepository;
import com.example.expensetracker.firebase.IncomeRepository;
import com.example.expensetracker.firebase.ListDataCallback;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.utils.ExpenseAnalytics;
import com.example.expensetracker.utils.FormatUtils;
import com.example.expensetracker.utils.ValidationUtils;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetActivity extends BaseActivity {

    private ActivityBudgetBinding binding;
    private final ExpenseRepository expenseRepository = new ExpenseRepository();
    private final IncomeRepository incomeRepository = new IncomeRepository();
    private final BudgetRepository budgetRepository = new BudgetRepository();

    private final List<Expense> expenses = new ArrayList<>();
    private final List<Income> incomes = new ArrayList<>();
    private ListenerRegistration expenseRegistration;
    private ListenerRegistration incomeRegistration;
    private ListenerRegistration budgetRegistration;
    private String selectedMonthKey;
    private Budget currentBudget;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarBudget, true);

        selectedMonthKey = FormatUtils.monthKeyFromMillis(System.currentTimeMillis());
        updateSelectedMonthLabel();

        binding.btnSelectMonth.setOnClickListener(view -> showMonthPicker());
        binding.btnSaveBudget.setOnClickListener(view -> saveBudget());
        renderBudgetOverview();
    }

    @Override
    protected void onStart() {
        super.onStart();
        observeExpenses();
        observeIncomes();
        observeBudget();
    }

    @Override
    protected void onStop() {
        if (expenseRegistration != null) {
            expenseRegistration.remove();
            expenseRegistration = null;
        }
        if (budgetRegistration != null) {
            budgetRegistration.remove();
            budgetRegistration = null;
        }
        if (incomeRegistration != null) {
            incomeRegistration.remove();
            incomeRegistration = null;
        }
        super.onStop();
    }

    private void showMonthPicker() {
        Calendar calendar = Calendar.getInstance();
        String[] split = selectedMonthKey.split("-");
        if (split.length == 2) {
            calendar.set(Calendar.YEAR, Integer.parseInt(split[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(split[1]) - 1);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {
                    selectedMonthKey = FormatUtils.buildMonthKey(year, month);
                    updateSelectedMonthLabel();
                    observeBudget();
                    renderBudgetOverview();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void observeExpenses() {
        if (expenseRegistration != null) {
            expenseRegistration.remove();
        }
        expenseRegistration = expenseRepository.listenToExpenses(new ListDataCallback<Expense>() {
            @Override
            public void onSuccess(@NonNull List<Expense> data) {
                expenses.clear();
                expenses.addAll(data);
                renderBudgetOverview();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void observeBudget() {
        if (budgetRegistration != null) {
            budgetRegistration.remove();
        }
        budgetRegistration = budgetRepository.listenBudgetForMonth(selectedMonthKey, new DataCallback<Budget>() {
            @Override
            public void onSuccess(Budget data) {
                currentBudget = data;
                if (data != null && !binding.etBudgetAmount.isFocused()) {
                    binding.etBudgetAmount.setText(trimAmount(data.getAmount()));
                } else if (data == null && !binding.etBudgetAmount.isFocused()) {
                    binding.etBudgetAmount.setText(null);
                }
                renderBudgetOverview();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void observeIncomes() {
        if (incomeRegistration != null) {
            incomeRegistration.remove();
        }
        incomeRegistration = incomeRepository.listenToIncomes(new ListDataCallback<Income>() {
            @Override
            public void onSuccess(@NonNull List<Income> data) {
                incomes.clear();
                incomes.addAll(data);
                renderBudgetOverview();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void saveBudget() {
        String amountValue = binding.etBudgetAmount.getText() == null ? "" : binding.etBudgetAmount.getText().toString().trim();
        binding.tilBudgetAmount.setError(null);
        if (!ValidationUtils.isAmountValid(amountValue)) {
            binding.tilBudgetAmount.setError(getString(R.string.invalid_amount));
            return;
        }

        Budget budget = new Budget();
        budget.setMonthKey(selectedMonthKey);
        budget.setMonthLabel(ExpenseAnalytics.monthLabelFromKey(selectedMonthKey));
        budget.setAmount(Double.parseDouble(amountValue));
        budget.setUpdatedAt(System.currentTimeMillis());

        binding.btnSaveBudget.setEnabled(false);
        budgetRepository.saveBudget(budget, new OperationCallback() {
            @Override
            public void onSuccess() {
                binding.btnSaveBudget.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.budget_saved));
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                binding.btnSaveBudget.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.save_failed));
            }
        });
    }

    private void renderBudgetOverview() {
        double spent = ExpenseAnalytics.getSpentForMonth(expenses, selectedMonthKey);
        double totalIncome = ExpenseAnalytics.getTotalIncome(incomes);
        double incomeBalanceUsed = ExpenseAnalytics.getIncomeBalanceSpentTotal(expenses);
        double incomeBalanceRemaining = ExpenseAnalytics.getIncomeBalanceRemaining(expenses, incomes);
        double incomeAddedThisMonth = ExpenseAnalytics.getIncomeForMonth(incomes, selectedMonthKey);
        double incomeUsedThisMonth = ExpenseAnalytics.getIncomeBalanceSpentForMonth(expenses, selectedMonthKey);

        binding.tvBudgetMonthLabel.setText(getString(
                R.string.budget_month_header_format,
                ExpenseAnalytics.monthLabelFromKey(selectedMonthKey)
        ));
        binding.tvSpentAmount.setText(FormatUtils.formatCurrency(spent));
        binding.tvIncomeBalanceAmount.setText(FormatUtils.formatCurrency(incomeBalanceRemaining));
        binding.tvIncomeTotalValue.setText(FormatUtils.formatCurrency(totalIncome));
        binding.tvIncomeUsedValue.setText(FormatUtils.formatCurrency(incomeBalanceUsed));
        binding.tvIncomeBalanceStatus.setText(getString(
                R.string.income_balance_month_status_format,
                ExpenseAnalytics.monthLabelFromKey(selectedMonthKey),
                FormatUtils.formatCurrency(incomeAddedThisMonth),
                FormatUtils.formatCurrency(incomeUsedThisMonth)
        ));
        binding.tvIncomeBalanceEmptyHint.setVisibility(totalIncome <= 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.tvIncomeBalanceAmount.setTextColor(ContextCompat.getColor(
                this,
                incomeBalanceRemaining < 0 ? R.color.color_warning : R.color.color_text_primary
        ));

        if (currentBudget == null || currentBudget.getAmount() <= 0) {
            binding.tvRemainingAmount.setText("-");
            binding.tvTotalBudget.setText("-");
            binding.tvBudgetStatus.setText(getString(R.string.budget_not_set));
            binding.tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.white));
            binding.progressBudget.setProgressCompat(0, true);
            binding.progressBudget.setIndicatorColor(ContextCompat.getColor(this, R.color.white));
            return;
        }

        double budgetAmount = currentBudget.getAmount();
        double remaining = budgetAmount - spent;
        int progress = budgetAmount > 0 ? (int) Math.min(100, Math.round((spent / budgetAmount) * 100f)) : 0;

        binding.tvRemainingAmount.setText(FormatUtils.formatCurrency(remaining));
        binding.tvTotalBudget.setText(FormatUtils.formatCurrency(budgetAmount));
        binding.progressBudget.setProgressCompat(progress, true);

        if (remaining < 0) {
            binding.tvBudgetStatus.setText(getString(
                    R.string.budget_exceeded_format,
                    FormatUtils.formatCurrency(Math.abs(remaining))
            ));
            binding.progressBudget.setIndicatorColor(ContextCompat.getColor(this, R.color.color_warning));
        } else {
            binding.tvBudgetStatus.setText(getString(
                    R.string.budget_safe_format,
                    FormatUtils.formatCurrency(remaining)
            ));
            binding.progressBudget.setIndicatorColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void updateSelectedMonthLabel() {
        binding.tvSelectedMonth.setText(ExpenseAnalytics.monthLabelFromKey(selectedMonthKey));
    }

    private String trimAmount(double amount) {
        if (amount == (long) amount) {
            return String.valueOf((long) amount);
        }
        return String.valueOf(amount);
    }
}
