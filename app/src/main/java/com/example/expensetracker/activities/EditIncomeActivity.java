package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityEditIncomeBinding;
import com.example.expensetracker.firebase.IncomeRepository;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.utils.AppConstants;
import com.example.expensetracker.utils.FormatUtils;
import com.example.expensetracker.utils.ValidationUtils;

import java.util.Calendar;

public class EditIncomeActivity extends BaseActivity {

    private ActivityEditIncomeBinding binding;
    private final IncomeRepository incomeRepository = new IncomeRepository();
    private Income income;
    private long selectedDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditIncomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarEditIncome, true);

        Object extra = getIncomeExtra();
        if (!(extra instanceof Income)) {
            closeScreen();
            return;
        }

        income = (Income) extra;
        selectedDate = income.getDate();

        setupDateInput();
        bindIncome();
        binding.btnUpdateIncome.setOnClickListener(view -> updateIncome());
    }

    private void setupDateInput() {
        View.OnClickListener clickListener = view -> showDatePicker();
        binding.etIncomeDate.setOnClickListener(clickListener);
        binding.tilDate.setEndIconOnClickListener(clickListener);
    }

    private void bindIncome() {
        binding.etAmount.setText(String.valueOf(income.getAmount()));
        binding.etReason.setText(income.getReason());
        binding.etIncomeDate.setText(FormatUtils.formatDate(income.getDate()));
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

    private void updateIncome() {
        clearErrors();
        String amountValue = safeText(binding.etAmount.getText());
        String reason = safeText(binding.etReason.getText());

        if (!ValidationUtils.isAmountValid(amountValue)) {
            binding.tilAmount.setError(getString(R.string.invalid_amount));
            return;
        }
        if (reason.isEmpty()) {
            binding.tilReason.setError(getString(R.string.reason_required));
            return;
        }

        income.setAmount(Double.parseDouble(amountValue));
        income.setReason(reason);
        income.setDate(selectedDate);

        binding.btnUpdateIncome.setEnabled(false);
        incomeRepository.updateIncome(income, new OperationCallback() {
            @Override
            public void onSuccess() {
                binding.btnUpdateIncome.setEnabled(true);
                setResult(RESULT_OK);
                closeScreen();
            }

            @Override
            public void onFailure(Exception exception) {
                binding.btnUpdateIncome.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.update_failed));
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

    @Nullable
    private Object getIncomeExtra() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return null;
        }
        return BundleCompat.getSerializable(extras, AppConstants.EXTRA_INCOME, Income.class);
    }
}
