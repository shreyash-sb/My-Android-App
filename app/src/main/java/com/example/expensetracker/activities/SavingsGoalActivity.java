package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivitySavingsGoalBinding;
import com.example.expensetracker.firebase.DataCallback;
import com.example.expensetracker.firebase.ExpenseRepository;
import com.example.expensetracker.firebase.IncomeRepository;
import com.example.expensetracker.firebase.ListDataCallback;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.firebase.SavingsGoalRepository;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.models.SavingsGoal;
import com.example.expensetracker.utils.ExpenseAnalytics;
import com.example.expensetracker.utils.FormatUtils;
import com.example.expensetracker.utils.ValidationUtils;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SavingsGoalActivity extends BaseActivity {

    private ActivitySavingsGoalBinding binding;
    private final ExpenseRepository expenseRepository = new ExpenseRepository();
    private final IncomeRepository incomeRepository = new IncomeRepository();
    private final SavingsGoalRepository savingsGoalRepository = new SavingsGoalRepository();

    private final List<Expense> expenses = new ArrayList<>();
    private final List<Income> incomes = new ArrayList<>();
    private ListenerRegistration expenseRegistration;
    private ListenerRegistration incomeRegistration;
    private ListenerRegistration goalRegistration;
    private String selectedMonthKey;
    private SavingsGoal currentGoal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavingsGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarGoals, true);

        selectedMonthKey = FormatUtils.monthKeyFromMillis(System.currentTimeMillis());
        updateSelectedMonthLabel();

        binding.btnSelectGoalMonth.setOnClickListener(view -> showMonthPicker());
        binding.btnSaveGoal.setOnClickListener(view -> saveGoal());
        renderOverview();
    }

    @Override
    protected void onStart() {
        super.onStart();
        observeExpenses();
        observeIncomes();
        observeGoal();
    }

    @Override
    protected void onStop() {
        if (expenseRegistration != null) {
            expenseRegistration.remove();
            expenseRegistration = null;
        }
        if (incomeRegistration != null) {
            incomeRegistration.remove();
            incomeRegistration = null;
        }
        if (goalRegistration != null) {
            goalRegistration.remove();
            goalRegistration = null;
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
                    observeGoal();
                    renderOverview();
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
                renderOverview();
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
                renderOverview();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void observeGoal() {
        if (goalRegistration != null) {
            goalRegistration.remove();
        }
        goalRegistration = savingsGoalRepository.listenGoalForMonth(selectedMonthKey, new DataCallback<SavingsGoal>() {
            @Override
            public void onSuccess(SavingsGoal data) {
                currentGoal = data;
                if (data != null && !binding.etGoalTarget.isFocused()) {
                    binding.etGoalTarget.setText(trimAmount(data.getTargetAmount()));
                    binding.etGoalNote.setText(data.getNote());
                } else if (data == null && !binding.etGoalTarget.isFocused()) {
                    binding.etGoalTarget.setText(null);
                    binding.etGoalNote.setText(null);
                }
                renderOverview();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void saveGoal() {
        String amountValue = binding.etGoalTarget.getText() == null ? "" : binding.etGoalTarget.getText().toString().trim();
        String note = binding.etGoalNote.getText() == null ? "" : binding.etGoalNote.getText().toString().trim();
        binding.tilGoalTarget.setError(null);

        if (!ValidationUtils.isAmountValid(amountValue)) {
            binding.tilGoalTarget.setError(getString(R.string.invalid_amount));
            return;
        }

        SavingsGoal goal = new SavingsGoal();
        goal.setMonthKey(selectedMonthKey);
        goal.setMonthLabel(ExpenseAnalytics.monthLabelFromKey(selectedMonthKey));
        goal.setTargetAmount(Double.parseDouble(amountValue));
        goal.setNote(note);
        goal.setUpdatedAt(System.currentTimeMillis());

        binding.btnSaveGoal.setEnabled(false);
        savingsGoalRepository.saveGoal(goal, new OperationCallback() {
            @Override
            public void onSuccess() {
                binding.btnSaveGoal.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.goal_saved));
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                binding.btnSaveGoal.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.save_failed));
            }
        });
    }

    private void renderOverview() {
        double monthlyIncome = ExpenseAnalytics.getIncomeForMonth(incomes, selectedMonthKey);
        double monthlySpending = ExpenseAnalytics.getTotalExpenseForMonth(expenses, selectedMonthKey);
        double savedThisMonth = monthlyIncome - monthlySpending;
        double target = currentGoal != null ? currentGoal.getTargetAmount() : 0;
        double remaining = target - savedThisMonth;

        binding.tvGoalMonthLabel.setText(ExpenseAnalytics.monthLabelFromKey(selectedMonthKey));
        binding.tvSavedThisMonth.setText(FormatUtils.formatCurrency(savedThisMonth));
        binding.tvTargetAmount.setText(currentGoal == null ? "-" : FormatUtils.formatCurrency(target));
        binding.tvRemainingToGoal.setText(currentGoal == null ? "-" : FormatUtils.formatCurrency(remaining));
        binding.tvGoalMonthlyIncome.setText(FormatUtils.formatCurrency(monthlyIncome));
        binding.tvGoalMonthlySpending.setText(FormatUtils.formatCurrency(monthlySpending));
        binding.tvGoalNoteValue.setText(currentGoal != null && !TextUtils.isEmpty(currentGoal.getNote())
                ? currentGoal.getNote()
                : getString(R.string.goal_not_set));
        binding.tvGoalNoteValue.setTextColor(ContextCompat.getColor(
                this,
                currentGoal != null && !TextUtils.isEmpty(currentGoal.getNote())
                        ? R.color.color_text_primary
                        : R.color.color_text_secondary
        ));
    }

    private void updateSelectedMonthLabel() {
        binding.tvGoalSelectedMonth.setText(ExpenseAnalytics.monthLabelFromKey(selectedMonthKey));
    }

    private String trimAmount(double amount) {
        if (amount == (long) amount) {
            return String.valueOf((long) amount);
        }
        return String.valueOf(amount);
    }
}
