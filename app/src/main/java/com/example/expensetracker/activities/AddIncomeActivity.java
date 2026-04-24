package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityAddIncomeBinding;
import com.example.expensetracker.firebase.IncomeRepository;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.utils.FormatUtils;
import com.example.expensetracker.utils.ValidationUtils;

import java.util.Calendar;

public class AddIncomeActivity extends BaseActivity {

    private ActivityAddIncomeBinding binding;
    private final IncomeRepository incomeRepository = new IncomeRepository();
    private long selectedDate = System.currentTimeMillis();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddIncomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarAddIncome, true);
        setupDateInput();
        binding.btnSaveIncome.setOnClickListener(view -> saveIncome());
        binding.etIncomeDate.setText(FormatUtils.formatDate(selectedDate));
    }

    private void setupDateInput() {
        View.OnClickListener clickListener = view -> showDatePicker();
        binding.etIncomeDate.setOnClickListener(clickListener);
        binding.tilDate.setEndIconOnClickListener(clickListener);
    }

    private void showDatePicker() {
        Calendar calendar = FormatUtils.calendarFromMillis(selectedDate);
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {
                    selectedDate = FormatUtils.createDate(year, month, dayOfMonth);
                    binding.etIncomeDate.setText(FormatUtils.formatDate(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void saveIncome() {
        clearErrors();
        String amountValue = safeText(binding.etAmount.getText());
        String reason = safeText(binding.etReason.getText());

        if (!ValidationUtils.isAmountValid(amountValue)) {
            binding.tilAmount.setError(getString(R.string.invalid_amount));
            return;
        }
        if (TextUtils.isEmpty(reason)) {
            binding.tilReason.setError(getString(R.string.reason_required));
            return;
        }
        if (binding.etIncomeDate.getText() == null || binding.etIncomeDate.getText().toString().trim().isEmpty()) {
            binding.tilDate.setError(getString(R.string.date_required));
            return;
        }

        Income income = new Income();
        income.setAmount(Double.parseDouble(amountValue));
        income.setReason(reason);
        income.setDate(selectedDate);

        binding.btnSaveIncome.setEnabled(false);
        incomeRepository.addIncome(income, new OperationCallback() {
            @Override
            public void onSuccess() {
                binding.btnSaveIncome.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.income_saved));
                setResult(RESULT_OK);
                closeScreen();
            }

            @Override
            public void onFailure(Exception exception) {
                binding.btnSaveIncome.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.save_failed));
            }
        });
    }

    private void clearErrors() {
        binding.tilAmount.setError(null);
        binding.tilReason.setError(null);
        binding.tilDate.setError(null);
    }

    private String safeText(@Nullable CharSequence charSequence) {
        return charSequence == null ? "" : charSequence.toString().trim();
    }
}
