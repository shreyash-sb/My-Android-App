package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.adapters.ExpenseAdapter;
import com.example.expensetracker.databinding.ActivityExpenseListBinding;
import com.example.expensetracker.firebase.ExpenseRepository;
import com.example.expensetracker.firebase.ListDataCallback;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.AppConstants;
import com.example.expensetracker.utils.CategoryUtils;
import com.example.expensetracker.utils.DropdownUtils;
import com.example.expensetracker.utils.FormatUtils;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;

public class ExpenseListActivity extends BaseActivity implements ExpenseAdapter.Listener {

    private ActivityExpenseListBinding binding;
    private final ExpenseRepository expenseRepository = new ExpenseRepository();
    private final List<Expense> allExpenses = new ArrayList<>();
    private final ExpenseAdapter expenseAdapter = new ExpenseAdapter(this);
    private ListenerRegistration expenseRegistration;

    private String selectedCategory;
    private String selectedMonth;
    private Long selectedExactDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarExpenseList, true);
        setupRecyclerView();
        setupFilters();
        binding.fabAddExpense.setOnClickListener(view ->
                openScreen(new Intent(this, AddExpenseActivity.class), false, false));
    }

    @Override
    protected void onStart() {
        super.onStart();
        observeExpenses();
    }

    @Override
    protected void onStop() {
        if (expenseRegistration != null) {
            expenseRegistration.remove();
            expenseRegistration = null;
        }
        super.onStop();
    }

    private void setupRecyclerView() {
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExpenses.setAdapter(expenseAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Expense expense = expenseAdapter.getExpenseAt(position);
                expenseRepository.deleteExpense(expense, new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        showMessage(binding.getRoot(), getString(R.string.expense_deleted));
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        expenseAdapter.notifyItemChanged(position);
                        showMessage(binding.getRoot(), getString(R.string.delete_failed));
                    }
                });
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rvExpenses);
    }

    private void setupFilters() {
        selectedCategory = getString(R.string.all_categories);
        selectedMonth = getString(R.string.all_months);

        List<String> categoryOptions = new ArrayList<>();
        categoryOptions.add(getString(R.string.all_categories));
        categoryOptions.addAll(CategoryUtils.getCategories(this));
        ArrayAdapter<String> categoryAdapter = DropdownUtils.createAdapter(this, categoryOptions);
        binding.actvCategoryFilter.setAdapter(categoryAdapter);
        binding.actvCategoryFilter.setText(selectedCategory, false);
        DropdownUtils.setupDropdown(binding.actvCategoryFilter, binding.tilCategoryFilter);
        binding.actvCategoryFilter.setOnItemClickListener((adapterView, view, index, id) -> {
            selectedCategory = categoryOptions.get(index);
            applyFilters();
        });

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

        binding.etDateFilter.setOnClickListener(view -> showDatePicker());
        binding.tilDateFilter.setEndIconOnClickListener(view -> showDatePicker());
        binding.btnClearFilters.setOnClickListener(view -> clearFilters());
    }

    private void clearFilters() {
        selectedCategory = getString(R.string.all_categories);
        selectedMonth = getString(R.string.all_months);
        selectedExactDate = null;
        binding.actvCategoryFilter.setText(selectedCategory, false);
        binding.actvMonthFilter.setText(selectedMonth, false);
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

    private void observeExpenses() {
        if (expenseRegistration != null) {
            expenseRegistration.remove();
        }
        expenseRegistration = expenseRepository.listenToExpenses(new ListDataCallback<Expense>() {
            @Override
            public void onSuccess(List<Expense> data) {
                allExpenses.clear();
                if (data != null) {
                    allExpenses.addAll(data);
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
        for (Expense expense : allExpenses) {
            monthSet.add(FormatUtils.formatMonthLabel(expense.getDate()));
        }
        List<String> monthOptions = new ArrayList<>(monthSet);
        binding.actvMonthFilter.setAdapter(DropdownUtils.createAdapter(this, monthOptions));
        if (!monthOptions.contains(selectedMonth)) {
            selectedMonth = getString(R.string.all_months);
            binding.actvMonthFilter.setText(selectedMonth, false);
        }
    }

    private void applyFilters() {
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : allExpenses) {
            if (!TextUtils.equals(selectedCategory, getString(R.string.all_categories))
                    && !TextUtils.equals(selectedCategory, expense.getCategory())) {
                continue;
            }

            if (!TextUtils.equals(selectedMonth, getString(R.string.all_months))
                    && !TextUtils.equals(selectedMonth, FormatUtils.formatMonthLabel(expense.getDate()))) {
                continue;
            }

            if (selectedExactDate != null && !FormatUtils.isSameDay(selectedExactDate, expense.getDate())) {
                continue;
            }
            filteredExpenses.add(expense);
        }

        expenseAdapter.setExpenses(filteredExpenses);
        binding.tvResultCount.setText(getString(R.string.result_count_format, filteredExpenses.size()));
        binding.layoutEmptyState.setVisibility(filteredExpenses.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    public void onExpenseClicked(Expense expense) {
        Intent intent = new Intent(this, EditExpenseActivity.class);
        intent.putExtra(AppConstants.EXTRA_EXPENSE, expense);
        openScreen(intent, false, false);
    }
}
