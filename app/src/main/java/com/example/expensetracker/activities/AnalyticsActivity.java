package com.example.expensetracker.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityAnalyticsBinding;
import com.example.expensetracker.firebase.ExpenseRepository;
import com.example.expensetracker.firebase.IncomeRepository;
import com.example.expensetracker.firebase.ListDataCallback;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.utils.ExpenseAnalytics;
import com.example.expensetracker.utils.FormatUtils;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsActivity extends BaseActivity {

    private ActivityAnalyticsBinding binding;
    private final ExpenseRepository expenseRepository = new ExpenseRepository();
    private final IncomeRepository incomeRepository = new IncomeRepository();
    private final List<Expense> allExpenses = new ArrayList<>();
    private final List<Income> allIncomes = new ArrayList<>();
    private ListenerRegistration expenseRegistration;
    private ListenerRegistration incomeRegistration;
    private int selectedPeriodDays = 30;
    private boolean incomeMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarAnalytics, true);
        setupEmptyCharts();
        setupPeriodFilter();
        setupModeFilter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        observeExpenses();
        observeIncome();
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
        super.onStop();
    }

    private void setupPeriodFilter() {
        binding.chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip7Days) {
                selectedPeriodDays = 7;
            } else if (checkedId == R.id.chip3Months) {
                selectedPeriodDays = 90;
            } else if (checkedId == R.id.chipYear) {
                selectedPeriodDays = 365;
            } else {
                selectedPeriodDays = 30;
            }
            renderCurrentMode();
        });
    }

    private void setupModeFilter() {
        binding.chipGroupMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            incomeMode = checkedIds.get(0) == R.id.chipModeIncome;
            renderCurrentMode();
        });
    }

    private void observeExpenses() {
        if (expenseRegistration != null) {
            expenseRegistration.remove();
        }
        expenseRegistration = expenseRepository.listenToExpenses(new ListDataCallback<Expense>() {
            @Override
            public void onSuccess(@NonNull List<Expense> data) {
                allExpenses.clear();
                allExpenses.addAll(data);
                renderCurrentMode();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void observeIncome() {
        if (incomeRegistration != null) {
            incomeRegistration.remove();
        }
        incomeRegistration = incomeRepository.listenToIncomes(new ListDataCallback<Income>() {
            @Override
            public void onSuccess(@NonNull List<Income> data) {
                allIncomes.clear();
                allIncomes.addAll(data);
                renderCurrentMode();
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void renderCurrentMode() {
        if (incomeMode) {
            renderIncomeAnalytics(filterIncomeByPeriod());
        } else {
            renderExpenseAnalytics(filterExpensesByPeriod());
        }
    }

    @NonNull
    private List<Expense> filterExpensesByPeriod() {
        long cutoff = System.currentTimeMillis() - (selectedPeriodDays * 24L * 60L * 60L * 1000L);
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : allExpenses) {
            if (expense.getDate() >= cutoff) {
                filteredExpenses.add(expense);
            }
        }
        return filteredExpenses;
    }

    @NonNull
    private List<Income> filterIncomeByPeriod() {
        long cutoff = System.currentTimeMillis() - (selectedPeriodDays * 24L * 60L * 60L * 1000L);
        List<Income> filteredIncomes = new ArrayList<>();
        for (Income income : allIncomes) {
            if (income.getDate() >= cutoff) {
                filteredIncomes.add(income);
            }
        }
        return filteredIncomes;
    }

    private void renderExpenseAnalytics(@NonNull List<Expense> expenses) {
        configureMetricLabels(false);
        binding.layoutAnalyticsEmpty.setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);
        binding.tvAnalyticsEmptyState.setText(R.string.analytics_empty);

        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        double averageExpense = expenses.isEmpty() ? 0 : total / expenses.size();
        LinkedHashMap<String, Float> monthlyTotals = ExpenseAnalytics.buildMonthlyTotals(expenses, selectedPeriodDays >= 365 ? 12 : 6);
        double monthlyAverage = calculateMonthlyAverage(monthlyTotals);

        binding.tvHighestCategoryValue.setText(ExpenseAnalytics.getHighestCategory(expenses));
        binding.tvAverageExpenseValue.setText(FormatUtils.formatCurrency(averageExpense));
        binding.tvMonthlyAverageValue.setText(FormatUtils.formatCurrency(monthlyAverage));

        if (expenses.isEmpty()) {
            binding.pieChart.clear();
            binding.barChart.clear();
            return;
        }

        renderPieChart(ExpenseAnalytics.buildCategoryTotals(expenses), getString(R.string.analytics_mode_expense));
        renderBarChart(monthlyTotals);
    }

    private void renderIncomeAnalytics(@NonNull List<Income> incomes) {
        configureMetricLabels(true);
        binding.layoutAnalyticsEmpty.setVisibility(incomes.isEmpty() ? View.VISIBLE : View.GONE);
        binding.tvAnalyticsEmptyState.setText(R.string.analytics_empty_income);

        double total = 0;
        for (Income income : incomes) {
            total += income.getAmount();
        }
        double averageIncome = incomes.isEmpty() ? 0 : total / incomes.size();
        LinkedHashMap<String, Float> monthlyTotals = ExpenseAnalytics.buildMonthlyIncomeTotals(incomes, selectedPeriodDays >= 365 ? 12 : 6);
        double monthlyAverage = calculateMonthlyAverage(monthlyTotals);

        binding.tvHighestCategoryValue.setText(ExpenseAnalytics.getHighestIncomeReason(incomes));
        binding.tvAverageExpenseValue.setText(FormatUtils.formatCurrency(averageIncome));
        binding.tvMonthlyAverageValue.setText(FormatUtils.formatCurrency(monthlyAverage));

        if (incomes.isEmpty()) {
            binding.pieChart.clear();
            binding.barChart.clear();
            return;
        }

        renderPieChart(ExpenseAnalytics.buildIncomeReasonTotals(incomes), getString(R.string.analytics_mode_income));
        renderBarChart(monthlyTotals);
    }

    private void configureMetricLabels(boolean incomeMode) {
        binding.tvPrimaryMetricLabel.setText(incomeMode ? R.string.highest_reason : R.string.highest_category);
        binding.tvSecondaryMetricLabel.setText(incomeMode ? R.string.average_income : R.string.average_expense);
        binding.tvPieChartTitle.setText(incomeMode ? R.string.income_breakdown : R.string.category_breakdown);
        binding.tvBarChartTitle.setText(incomeMode ? R.string.monthly_income_breakdown : R.string.monthly_breakdown);
    }

    private double calculateMonthlyAverage(@NonNull LinkedHashMap<String, Float> monthlyTotals) {
        float total = 0f;
        for (float amount : monthlyTotals.values()) {
            total += amount;
        }
        return monthlyTotals.isEmpty() ? 0 : total / monthlyTotals.size();
    }

    private void renderPieChart(@NonNull Map<String, Float> categoryTotals, @NonNull String centerText) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getChartColors());
        dataSet.setSliceSpace(4f);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.color_text_primary));
        dataSet.setValueTextSize(11f);

        PieData pieData = new PieData(dataSet);
        binding.pieChart.setUsePercentValues(false);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleRadius(54f);
        binding.pieChart.setEntryLabelColor(ContextCompat.getColor(this, R.color.color_text_primary));
        binding.pieChart.setCenterText(centerText);
        binding.pieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.color_text_primary));
        binding.pieChart.setData(pieData);

        Legend legend = binding.pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        legend.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));

        binding.pieChart.invalidate();
        binding.pieChart.animateY(700);
    }

    private void renderBarChart(@NonNull LinkedHashMap<String, Float> monthlyTotals) {
        List<String> labels = new ArrayList<>(monthlyTotals.keySet());
        List<BarEntry> entries = new ArrayList<>();
        for (int index = 0; index < labels.size(); index++) {
            entries.add(new BarEntry(index, monthlyTotals.get(labels.get(index))));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(this, incomeMode ? R.color.color_success : R.color.color_primary));
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.color_text_primary));
        dataSet.setValueTextSize(11f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        binding.barChart.setData(barData);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.setFitBars(true);

        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-18f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));

        YAxis leftAxis = binding.barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));
        binding.barChart.getAxisRight().setEnabled(false);

        binding.barChart.invalidate();
        binding.barChart.animateY(750);
    }

    private void setupEmptyCharts() {
        binding.tvHighestCategoryValue.setText("-");
        binding.tvAverageExpenseValue.setText(FormatUtils.formatCurrency(0));
        binding.tvMonthlyAverageValue.setText(FormatUtils.formatCurrency(0));
        configureMetricLabels(false);
    }

    private List<Integer> getChartColors() {
        return Arrays.asList(
                ContextCompat.getColor(this, R.color.color_chart_one),
                ContextCompat.getColor(this, R.color.color_chart_two),
                ContextCompat.getColor(this, R.color.color_chart_three),
                ContextCompat.getColor(this, R.color.color_chart_four),
                ContextCompat.getColor(this, R.color.color_chart_five)
        );
    }
}
