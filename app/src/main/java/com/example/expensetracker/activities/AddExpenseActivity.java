package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityAddExpenseBinding;
import com.example.expensetracker.firebase.ExpenseRepository;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.AppConstants;
import com.example.expensetracker.utils.CategoryUtils;
import com.example.expensetracker.utils.DropdownUtils;
import com.example.expensetracker.utils.FormatUtils;
import com.example.expensetracker.utils.ValidationUtils;

import java.util.Arrays;
import java.util.Calendar;

public class AddExpenseActivity extends BaseActivity {

    private ActivityAddExpenseBinding binding;
    private final ExpenseRepository expenseRepository = new ExpenseRepository();
    private long selectedDate = System.currentTimeMillis();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarAddExpense, true);
        setupCategoryDropdown();
        setupSourceDropdown();
        setupDateInput();
        binding.btnSaveExpense.setOnClickListener(view -> saveExpense());
        binding.etExpenseDate.setText(FormatUtils.formatDate(selectedDate));
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = DropdownUtils.createAdapter(this, CategoryUtils.getCategories(this));
        binding.actvCategory.setAdapter(adapter);
        DropdownUtils.setupDropdown(binding.actvCategory, binding.tilCategory);
    }

    private void setupSourceDropdown() {
        ArrayAdapter<String> adapter = DropdownUtils.createAdapter(
                this,
                Arrays.asList(getResources().getStringArray(R.array.expense_sources))
        );
        binding.actvSource.setAdapter(adapter);
        DropdownUtils.setupDropdown(binding.actvSource, binding.tilSource);
        binding.actvSource.setText(getString(R.string.source_monthly_budget), false);
    }

    private void setupDateInput() {
        View.OnClickListener clickListener = view -> showDatePicker();
        binding.etExpenseDate.setOnClickListener(clickListener);
        binding.tilDate.setEndIconOnClickListener(clickListener);
    }

    private void showDatePicker() {
        Calendar calendar = FormatUtils.calendarFromMillis(selectedDate);
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {
                    selectedDate = FormatUtils.createDate(year, month, dayOfMonth);
                    binding.etExpenseDate.setText(FormatUtils.formatDate(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void saveExpense() {
        clearErrors();
        String amountValue = safeText(binding.etAmount.getText());
        String category = safeText(binding.actvCategory.getText());
        String sourceLabel = safeText(binding.actvSource.getText());
        String note = safeText(binding.etNote.getText());

        if (!ValidationUtils.isAmountValid(amountValue)) {
            binding.tilAmount.setError(getString(R.string.invalid_amount));
            return;
        }
        if (category.isEmpty()) {
            binding.tilCategory.setError(getString(R.string.category_required));
            return;
        }
        if (sourceLabel.isEmpty()) {
            binding.tilSource.setError(getString(R.string.source_required));
            return;
        }
        if (binding.etExpenseDate.getText() == null || binding.etExpenseDate.getText().toString().trim().isEmpty()) {
            binding.tilDate.setError(getString(R.string.date_required));
            return;
        }

        Expense expense = new Expense();
        expense.setAmount(Double.parseDouble(amountValue));
        expense.setCategory(category);
        expense.setPaymentSource(mapExpenseSource(sourceLabel));
        expense.setNote(note);
        expense.setDate(selectedDate);

        binding.btnSaveExpense.setEnabled(false);
        expenseRepository.addExpense(expense, new OperationCallback() {
            @Override
            public void onSuccess() {
                binding.btnSaveExpense.setEnabled(true);
                setResult(RESULT_OK);
                closeScreen();
            }

            @Override
            public void onFailure(Exception exception) {
                binding.btnSaveExpense.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.save_failed));
            }
        });
    }

    private void clearErrors() {
        binding.tilAmount.setError(null);
        binding.tilCategory.setError(null);
        binding.tilSource.setError(null);
        binding.tilDate.setError(null);
    }

    private String safeText(@Nullable CharSequence charSequence) {
        return charSequence == null ? "" : charSequence.toString().trim();
    }

    private String mapExpenseSource(String sourceLabel) {
        if (getString(R.string.source_income_balance).equals(sourceLabel)) {
            return AppConstants.EXPENSE_SOURCE_INCOME_BALANCE;
        }
        return AppConstants.EXPENSE_SOURCE_MONTHLY_BUDGET;
    }
}
