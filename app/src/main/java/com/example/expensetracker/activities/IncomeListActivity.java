package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.adapters.IncomeAdapter;
import com.example.expensetracker.databinding.ActivityIncomeListBinding;
import com.example.expensetracker.firebase.IncomeRepository;
import com.example.expensetracker.firebase.ListDataCallback;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.utils.AppConstants;
import com.example.expensetracker.utils.DropdownUtils;
import com.example.expensetracker.utils.FormatUtils;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class IncomeListActivity extends BaseActivity implements IncomeAdapter.Listener {

    private ActivityIncomeListBinding binding;
    private final IncomeRepository incomeRepository = new IncomeRepository();
    private final List<Income> allIncomes = new ArrayList<>();
    private final IncomeAdapter incomeAdapter = new IncomeAdapter(this);
    private ListenerRegistration incomeRegistration;

    private String selectedMonth;
    private String reasonQuery = "";
    private Long selectedExactDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomeListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarIncomeList, true);
        setupRecyclerView();
        setupFilters();
        binding.fabAddIncome.setOnClickListener(view ->
                openScreen(new Intent(this, AddIncomeActivity.class), false, false));
    }

    @Override
    protected void onStart() {
        super.onStart();
        observeIncome();
    }

    @Override
    protected void onStop() {
        if (incomeRegistration != null) {
            incomeRegistration.remove();
            incomeRegistration = null;
        }
        super.onStop();
    }

    private void setupRecyclerView() {
        binding.rvIncome.setLayoutManager(new LinearLayoutManager(this));
        binding.rvIncome.setAdapter(incomeAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Income income = incomeAdapter.getIncomeAt(position);
                incomeRepository.deleteIncome(income, new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        showMessage(binding.getRoot(), getString(R.string.income_deleted));
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        incomeAdapter.notifyItemChanged(position);
                        showMessage(binding.getRoot(), getString(R.string.delete_failed));
                    }
                });
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rvIncome);
    }

    private void setupFilters() {
        selectedMonth = getString(R.string.all_months);

        List<String> defaultMonths = new ArrayList<>();
        defaultMonths.add(getString(R.string.all_months));
        ArrayAdapter<String> monthAdapter = DropdownUtils.createAdapter(this, defaultMonths);
        binding.actvMonthFilter.setAdapter(monthAdapter);
        binding.actvMonthFilter.setText(selectedMonth, false);
        DropdownUtils.setupDropdown(binding.actvMonthFilter, binding.tilMonthFilter);
        binding.actvMonthFilter.setOnItemClickListener((adapterView, view, index, id) -> {
            selectedMonth = (String) adapterView.getItemAtPosition(index);
            applyFilters();
        });

        binding.etReasonFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reasonQuery = s == null ? "" : s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etDateFilter.setOnClickListener(view -> showDatePicker());
        binding.tilDateFilter.setEndIconOnClickListener(view -> showDatePicker());
        binding.btnClearFilters.setOnClickListener(view -> clearFilters());
    }

    private void clearFilters() {
        selectedMonth = getString(R.string.all_months);
        reasonQuery = "";
        selectedExactDate = null;
        binding.actvMonthFilter.setText(selectedMonth, false);
        binding.etReasonFilter.setText(null);
        binding.etDateFilter.setText(null);
        applyFilters();
    }

    private void showDatePicker() {
        Calendar calendar = selectedExactDate == null
                ? Calendar.getInstance()
                : FormatUtils.calendarFromMillis(selectedExactDate);
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {
                    selectedExactDate = FormatUtils.createDate(year, month, dayOfMonth);
                    binding.etDateFilter.setText(FormatUtils.formatDate(selectedExactDate));
                    applyFilters();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void observeIncome() {
        if (incomeRegistration != null) {
            incomeRegistration.remove();
        }
        incomeRegistration = incomeRepository.listenToIncomes(new ListDataCallback<Income>() {
            @Override
            public void onSuccess(List<Income> data) {
                allIncomes.clear();
                if (data != null) {
                    allIncomes.addAll(data);
                }
                updateMonthOptions();
                applyFilters();
            }

            @Override
            public void onFailure(Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void updateMonthOptions() {
        LinkedHashSet<String> monthSet = new LinkedHashSet<>();
        monthSet.add(getString(R.string.all_months));
        for (Income income : allIncomes) {
            monthSet.add(FormatUtils.formatMonthLabel(income.getDate()));
        }
        List<String> monthOptions = new ArrayList<>(monthSet);
        binding.actvMonthFilter.setAdapter(DropdownUtils.createAdapter(this, monthOptions));
        if (!monthOptions.contains(selectedMonth)) {
            selectedMonth = getString(R.string.all_months);
            binding.actvMonthFilter.setText(selectedMonth, false);
        }
    }

    private void applyFilters() {
        List<Income> filteredIncome = new ArrayList<>();
        for (Income income : allIncomes) {
            if (!TextUtils.equals(selectedMonth, getString(R.string.all_months))
                    && !TextUtils.equals(selectedMonth, FormatUtils.formatMonthLabel(income.getDate()))) {
                continue;
            }

            if (!TextUtils.isEmpty(reasonQuery)) {
                String reason = income.getReason() == null ? "" : income.getReason().toLowerCase(Locale.getDefault());
                if (!reason.contains(reasonQuery.toLowerCase(Locale.getDefault()))) {
                    continue;
                }
            }

            if (selectedExactDate != null && !FormatUtils.isSameDay(selectedExactDate, income.getDate())) {
                continue;
            }

            filteredIncome.add(income);
        }

        incomeAdapter.setIncomes(filteredIncome);
        binding.tvResultCount.setText(getString(R.string.income_result_count_format, filteredIncome.size()));
        binding.layoutEmptyState.setVisibility(filteredIncome.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    public void onIncomeClicked(Income income) {
        Intent intent = new Intent(this, EditIncomeActivity.class);
        intent.putExtra(AppConstants.EXTRA_INCOME, income);
        openScreen(intent, false, false);
    }
}
