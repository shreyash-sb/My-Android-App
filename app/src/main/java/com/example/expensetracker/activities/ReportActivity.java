package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityReportBinding;
import com.example.expensetracker.firebase.ExpenseRepository;
import com.example.expensetracker.firebase.IncomeRepository;
import com.example.expensetracker.firebase.ListDataCallback;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.utils.FormatUtils;
import com.example.expensetracker.utils.ReportPdfGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportActivity extends BaseActivity {

    private ActivityReportBinding binding;
    private final ExpenseRepository expenseRepository = new ExpenseRepository();
    private final IncomeRepository incomeRepository = new IncomeRepository();
    private final List<Expense> allExpenses = new ArrayList<>();
    private final List<Income> allIncomes = new ArrayList<>();

    private long fromDate;
    private long toDate;
    private boolean expensesLoaded;
    private boolean incomesLoaded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarReport, true);

        setDefaultRange();
        setupDateInputs();
        renderPreview();

        binding.btnGenerateReport.setOnClickListener(view -> generateReport());
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    private void setDefaultRange() {
        Calendar calendar = Calendar.getInstance();
        toDate = FormatUtils.createDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        fromDate = FormatUtils.createDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void setupDateInputs() {
        binding.etFromDate.setOnClickListener(view -> showDatePicker(true));
        binding.tilFromDate.setEndIconOnClickListener(view -> showDatePicker(true));
        binding.etToDate.setOnClickListener(view -> showDatePicker(false));
        binding.tilToDate.setEndIconOnClickListener(view -> showDatePicker(false));
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar calendar = FormatUtils.calendarFromMillis(isFromDate ? fromDate : toDate);
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {
                    long selectedDate = FormatUtils.createDate(year, month, dayOfMonth);
                    if (isFromDate) {
                        fromDate = selectedDate;
                        if (fromDate > toDate) {
                            toDate = fromDate;
                        }
                    } else {
                        toDate = selectedDate;
                        if (toDate < fromDate) {
                            fromDate = toDate;
                        }
                    }
                    renderPreview();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadData() {
        setLoading(true, false);
        expensesLoaded = false;
        incomesLoaded = false;
        loadExpenses();
        loadIncomes();
    }

    private void loadExpenses() {
        expenseRepository.fetchExpensesOnce(new ListDataCallback<Expense>() {
            @Override
            public void onSuccess(List<Expense> data) {
                allExpenses.clear();
                if (data != null) {
                    allExpenses.addAll(data);
                }
                expensesLoaded = true;
                completeDataLoad();
            }

            @Override
            public void onFailure(Exception exception) {
                expensesLoaded = true;
                completeDataLoad();
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void loadIncomes() {
        incomeRepository.fetchIncomesOnce(new ListDataCallback<Income>() {
            @Override
            public void onSuccess(List<Income> data) {
                allIncomes.clear();
                if (data != null) {
                    allIncomes.addAll(data);
                }
                incomesLoaded = true;
                completeDataLoad();
            }

            @Override
            public void onFailure(Exception exception) {
                incomesLoaded = true;
                completeDataLoad();
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void completeDataLoad() {
        if (!expensesLoaded || !incomesLoaded) {
            return;
        }
        setLoading(false, false);
        renderPreview();
    }

    private void renderPreview() {
        binding.etFromDate.setText(FormatUtils.formatDate(fromDate));
        binding.etToDate.setText(FormatUtils.formatDate(toDate));

        List<Expense> filteredExpenses = getExpensesInRange();
        List<Income> filteredIncomes = getIncomesInRange();
        double expenseTotal = 0;
        double incomeTotal = 0;
        for (Expense expense : filteredExpenses) {
            expenseTotal += expense.getAmount();
        }
        for (Income income : filteredIncomes) {
            incomeTotal += income.getAmount();
        }

        binding.tvReportPeriod.setText(getString(
                R.string.report_period_format,
                FormatUtils.formatDate(fromDate),
                FormatUtils.formatDate(toDate)
        ));
        binding.tvReportExpenseCount.setText(getString(
                R.string.report_preview_expense_count_format,
                filteredExpenses.size()
        ));
        binding.tvReportExpenseTotal.setText(getString(
                R.string.report_preview_expense_total_format,
                FormatUtils.formatCurrency(expenseTotal)
        ));
        binding.tvReportIncomeCount.setText(getString(
                R.string.report_preview_income_count_format,
                filteredIncomes.size()
        ));
        binding.tvReportIncomeTotal.setText(getString(
                R.string.report_preview_income_total_format,
                FormatUtils.formatCurrency(incomeTotal)
        ));
        binding.tvReportNetTotal.setText(getString(
                R.string.report_preview_balance_format,
                FormatUtils.formatCurrency(incomeTotal - expenseTotal)
        ));
    }

    private void generateReport() {
        if (fromDate > toDate) {
            showMessage(binding.getRoot(), getString(R.string.report_range_invalid));
            return;
        }

        try {
            setLoading(true, true);
            File pdfFile = ReportPdfGenerator.generateFinanceReport(
                    this,
                    getExpensesInRange(),
                    getIncomesInRange(),
                    fromDate,
                    toDate
            );
            setLoading(false, false);
            shareReport(pdfFile);
            showMessage(binding.getRoot(), getString(R.string.report_ready));
        } catch (Exception exception) {
            setLoading(false, false);
            showMessage(binding.getRoot(), getString(R.string.report_failed));
        }
    }

    private void shareReport(File pdfFile) {
        Uri reportUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                pdfFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, reportUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_title));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.report_title)));
    }

    private List<Expense> getExpensesInRange() {
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : allExpenses) {
            if (expense == null) {
                continue;
            }
            long expenseDate = expense.getDate();
            if (expenseDate >= fromDate && expenseDate <= toDate) {
                filteredExpenses.add(expense);
            }
        }
        return filteredExpenses;
    }

    private List<Income> getIncomesInRange() {
        List<Income> filteredIncomes = new ArrayList<>();
        for (Income income : allIncomes) {
            if (income == null) {
                continue;
            }
            long incomeDate = income.getDate();
            if (incomeDate >= fromDate && incomeDate <= toDate) {
                filteredIncomes.add(income);
            }
        }
        return filteredIncomes;
    }

    private void setLoading(boolean isLoading, boolean isGenerating) {
        binding.progressReport.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnGenerateReport.setEnabled(!isLoading);
        binding.etFromDate.setEnabled(!isLoading);
        binding.etToDate.setEnabled(!isLoading);
        binding.tilFromDate.setEnabled(!isLoading);
        binding.tilToDate.setEnabled(!isLoading);
        binding.btnGenerateReport.setText(isGenerating ? R.string.report_generating : R.string.generate_report);
    }
}
