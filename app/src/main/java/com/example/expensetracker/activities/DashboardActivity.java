package com.example.expensetracker.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityDashboardBinding;
import com.example.expensetracker.databinding.ItemDashboardRecentExpenseBinding;
import com.example.expensetracker.firebase.AuthRepository;
import com.example.expensetracker.firebase.BudgetRepository;
import com.example.expensetracker.firebase.DataCallback;
import com.example.expensetracker.firebase.ExpenseRepository;
import com.example.expensetracker.firebase.IncomeRepository;
import com.example.expensetracker.firebase.ListDataCallback;
import com.example.expensetracker.firebase.UserRepository;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.DashboardSummary;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.models.UserProfile;
import com.example.expensetracker.utils.AppPreferences;
import com.example.expensetracker.utils.AppConstants;
import com.example.expensetracker.utils.CategoryUtils;
import com.example.expensetracker.utils.ExpenseAnalytics;
import com.example.expensetracker.utils.FormatUtils;
import com.example.expensetracker.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends BaseActivity {

    private ActivityDashboardBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ExpenseRepository expenseRepository = new ExpenseRepository();
    private final IncomeRepository incomeRepository = new IncomeRepository();
    private final BudgetRepository budgetRepository = new BudgetRepository();

    private final List<Expense> expenses = new ArrayList<>();
    private final List<Income> incomes = new ArrayList<>();
    private ListenerRegistration expenseRegistration;
    private ListenerRegistration incomeRegistration;
    private ListenerRegistration budgetRegistration;
    private Budget currentBudget;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!authRepository.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        binding.tvMonthStamp.setText(getString(
                R.string.dashboard_month_snapshot_format,
                FormatUtils.formatMonthLabel(System.currentTimeMillis())
        ));
        binding.tvDashboardSubtitle.setText(getString(R.string.dashboard_subtitle));

        binding.btnOpenDrawer.setOnClickListener(view -> openDrawer());
        binding.btnDrawerProfile.setOnClickListener(view ->
                openFromDrawer(new Intent(this, ProfileActivity.class)));
        binding.btnDrawerAddExpense.setOnClickListener(view ->
                openFromDrawer(new Intent(this, AddExpenseActivity.class)));
        binding.btnDrawerIncome.setOnClickListener(view ->
                openFromDrawer(new Intent(this, AddIncomeActivity.class)));
        binding.btnDrawerExpenseList.setOnClickListener(view ->
                openFromDrawer(new Intent(this, ExpenseListActivity.class)));
        binding.btnDrawerIncomeList.setOnClickListener(view ->
                openFromDrawer(new Intent(this, IncomeListActivity.class)));
        binding.btnDrawerBudget.setOnClickListener(view ->
                openFromDrawer(new Intent(this, BudgetActivity.class)));
        binding.btnDrawerGoals.setOnClickListener(view ->
                openFromDrawer(new Intent(this, SavingsGoalActivity.class)));
        binding.btnDrawerAnalytics.setOnClickListener(view ->
                openFromDrawer(new Intent(this, AnalyticsActivity.class)));
        binding.btnDrawerReport.setOnClickListener(view ->
                openFromDrawer(new Intent(this, ReportActivity.class)));
        binding.btnDrawerSettings.setOnClickListener(view ->
                openFromDrawer(new Intent(this, SettingsActivity.class)));
        binding.btnDrawerAbout.setOnClickListener(view ->
                openFromDrawer(new Intent(this, AboutActivity.class)));
        binding.btnDrawerLogout.setOnClickListener(view -> {
            authRepository.logout();
            redirectToLogin();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                setEnabled(false);
                DashboardActivity.this.getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });

        fetchProfile();
        renderDashboard();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!authRepository.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        observeExpenses();
        observeIncomes();
        observeBudget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authRepository.isLoggedIn()) {
            fetchProfile();
        }
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
        if (budgetRegistration != null) {
            budgetRegistration.remove();
            budgetRegistration = null;
        }
        super.onStop();
    }

    private void fetchProfile() {
        userRepository.fetchCurrentUserProfile(new DataCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                bindProfile(data);
            }

            @Override
            public void onFailure(Exception exception) {
                bindProfile(null);
            }
        });
    }

    private void bindProfile(@Nullable UserProfile profile) {
        FirebaseUser firebaseUser = authRepository.getCurrentUser();
        String email = profile != null && !TextUtils.isEmpty(profile.getEmail())
                ? profile.getEmail()
                : (firebaseUser != null ? firebaseUser.getEmail() : "");
        String displayName = profile != null && !TextUtils.isEmpty(profile.getName())
                ? profile.getName()
                : getFallbackUserName();
        long memberSince = profile != null && profile.getCreatedAt() > 0
                ? profile.getCreatedAt()
                : System.currentTimeMillis();

        binding.tvGreeting.setText(getString(R.string.greeting_format, displayName));
        binding.tvDrawerName.setText(displayName);
        binding.tvDrawerEmail.setText(email);
        binding.tvDrawerInitial.setText(getProfileInitial(displayName, email));
        binding.tvDrawerSince.setText(getString(
                R.string.drawer_profile_since_format,
                FormatUtils.formatMonthLabel(memberSince)
        ));
    }

    private void observeExpenses() {
        if (expenseRegistration != null) {
            expenseRegistration.remove();
        }
        expenseRegistration = expenseRepository.listenToExpenses(new ListDataCallback<Expense>() {
            @Override
            public void onSuccess(List<Expense> data) {
                expenses.clear();
                if (data != null) {
                    expenses.addAll(data);
                }
                renderDashboard();
            }

            @Override
            public void onFailure(Exception exception) {
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
            public void onSuccess(List<Income> data) {
                incomes.clear();
                if (data != null) {
                    incomes.addAll(data);
                }
                renderDashboard();
            }

            @Override
            public void onFailure(Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void observeBudget() {
        if (budgetRegistration != null) {
            budgetRegistration.remove();
        }
        String monthKey = FormatUtils.monthKeyFromMillis(System.currentTimeMillis());
        budgetRegistration = budgetRepository.listenBudgetForMonth(monthKey, new DataCallback<Budget>() {
            @Override
            public void onSuccess(Budget data) {
                currentBudget = data;
                renderDashboard();
            }

            @Override
            public void onFailure(Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void renderDashboard() {
        try {
            DashboardSummary summary = ExpenseAnalytics.buildSummary(expenses, incomes, currentBudget);
            binding.tvTotalExpensesValue.setText(FormatUtils.formatCurrency(summary.getTotalExpenses()));
            binding.tvCurrentMonthValue.setText(FormatUtils.formatCurrency(summary.getCurrentMonthSpending()));
            binding.tvTransactionCountValue.setText(String.valueOf(summary.getExpenseTransactionCount()));
            binding.tvTotalIncomeValue.setText(FormatUtils.formatCurrency(summary.getTotalIncome()));
            binding.tvCurrentMonthIncomeValue.setText(FormatUtils.formatCurrency(summary.getCurrentMonthIncome()));
            binding.tvIncomeTransactionCountValue.setText(String.valueOf(summary.getIncomeTransactionCount()));
            binding.tvHeroIncomeValue.setText(FormatUtils.formatCurrency(summary.getCurrentMonthIncome()));
            binding.tvHeroExpenseValue.setText(FormatUtils.formatCurrency(summary.getCurrentMonthSpending()));

            renderPulse(summary);
            renderBudgetPanel(summary);
            renderBudgetAlert(summary);
            renderSmartCheckIn();
            renderRecentActivity();
        } catch (Exception exception) {
            showMessage(binding.getRoot(), getString(R.string.load_failed));
        }
    }

    private void renderPulse(@NonNull DashboardSummary summary) {
        binding.tvMonthlyNetValue.setText(FormatUtils.formatCurrency(summary.getMonthlyNet()));
        binding.tvSavingsRateValue.setText(getString(R.string.percentage_format, summary.getSavingsRate()));

        int monthlyNetColor = ContextCompat.getColor(
                this,
                summary.getMonthlyNet() >= 0 ? R.color.color_success : R.color.color_warning
        );
        int savingsRateColor = ContextCompat.getColor(
                this,
                summary.getSavingsRate() >= 0 ? R.color.color_primary : R.color.color_warning
        );

        binding.tvMonthlyNetValue.setTextColor(monthlyNetColor);
        binding.tvSavingsRateValue.setTextColor(savingsRateColor);
    }

    private void renderBudgetPanel(DashboardSummary summary) {
        binding.tvIncomeHeadline.setText(FormatUtils.formatCurrency(summary.getRemainingIncomeBalance()));

        if (currentBudget == null || currentBudget.getAmount() <= 0) {
            binding.tvBudgetHeadline.setText(getString(R.string.dashboard_budget_not_set_short));
            binding.tvBudgetCaption.setVisibility(View.VISIBLE);
            binding.tvBudgetCaption.setText(getString(R.string.dashboard_budget_missing_short));
            binding.progressBudgetOverview.setVisibility(View.GONE);
            return;
        }

        double remaining = summary.getRemainingBudget();
        double budgetAmount = currentBudget.getAmount();
        int progress = (int) Math.min(100, Math.round((summary.getCurrentMonthBudgetSpend() / budgetAmount) * 100f));
        binding.progressBudgetOverview.setVisibility(View.VISIBLE);
        binding.progressBudgetOverview.setProgressCompat(Math.max(progress, 0), true);
        binding.tvBudgetCaption.setVisibility(View.VISIBLE);
        binding.tvBudgetCaption.setText(getString(
                R.string.dashboard_income_balance_status_format,
                FormatUtils.formatCurrency(summary.getIncomeBalanceSpentTotal())
        ));

        if (remaining < 0) {
            binding.tvBudgetHeadline.setText(getString(
                R.string.dashboard_budget_over_format,
                FormatUtils.formatCurrency(Math.abs(remaining))
            ));
        } else {
            binding.tvBudgetHeadline.setText(getString(
                    R.string.dashboard_budget_left_format,
                    FormatUtils.formatCurrency(remaining)
            ));
        }
    }

    private void renderBudgetAlert(@NonNull DashboardSummary summary) {
        if (!AppPreferences.isBudgetAlertEnabled(this)
                || currentBudget == null
                || currentBudget.getAmount() <= 0) {
            binding.cardBudgetAlert.setVisibility(View.GONE);
            AppPreferences.clearLastBudgetAlertSignature(this);
            return;
        }

        double ratio = summary.getCurrentMonthBudgetSpend() / currentBudget.getAmount();
        int usedPercent = (int) Math.round(ratio * 100d);
        int threshold = AppPreferences.getBudgetAlertThreshold(this);
        boolean exceeded = summary.getRemainingBudget() < 0;
        boolean nearLimit = usedPercent >= threshold;

        if (!exceeded && !nearLimit) {
            binding.cardBudgetAlert.setVisibility(View.GONE);
            AppPreferences.clearLastBudgetAlertSignature(this);
            return;
        }

        String message = exceeded
                ? getString(R.string.dashboard_budget_alert_exceeded)
                : getString(R.string.dashboard_budget_alert_threshold_format, usedPercent);
        binding.cardBudgetAlert.setVisibility(View.VISIBLE);
        binding.tvBudgetAlertMessage.setText(message);

        String monthKey = FormatUtils.monthKeyFromMillis(System.currentTimeMillis());
        String signature = monthKey + ":" + (exceeded ? "exceeded" : "threshold");
        if (!signature.equals(AppPreferences.getLastBudgetAlertSignature(this))) {
            NotificationHelper.showBudgetAlert(
                    this,
                    getString(R.string.dashboard_budget_alert_title),
                    message,
                    signature.hashCode()
            );
            AppPreferences.setLastBudgetAlertSignature(this, signature);
        }
    }

    private void renderSmartCheckIn() {
        List<Expense> currentMonthExpenses = getCurrentMonthExpenses();
        List<Income> currentMonthIncomes = getCurrentMonthIncomes();

        Expense highestExpense = getHighestExpense(currentMonthExpenses);
        Income highestIncome = getHighestIncome(currentMonthIncomes);

        binding.tvTopCategory.setText(highestExpense == null
                ? getString(R.string.dashboard_highest_expense_empty)
                : getString(
                        R.string.dashboard_highest_expense_format,
                        FormatUtils.formatCurrency(highestExpense.getAmount()),
                        highestExpense.getCategory()
                ));

        binding.tvAverageSpend.setText(highestIncome == null
                ? getString(R.string.dashboard_highest_income_empty)
                : getString(
                        R.string.dashboard_highest_income_format,
                        FormatUtils.formatCurrency(highestIncome.getAmount()),
                        highestIncome.getReason()
                ));

        binding.tvDashboardEmptyState.setVisibility(
                highestExpense == null && highestIncome == null ? View.VISIBLE : View.GONE
        );
    }

    private void renderRecentActivity() {
        binding.layoutRecentExpenses.removeAllViews();

        List<RecentEntry> recentEntries = new ArrayList<>();
        for (Expense expense : expenses) {
            if (expense != null) {
                recentEntries.add(RecentEntry.fromExpense(expense));
            }
        }
        for (Income income : incomes) {
            if (income != null) {
                recentEntries.add(RecentEntry.fromIncome(income));
            }
        }

        if (recentEntries.isEmpty()) {
            binding.tvRecentExpensesEmpty.setVisibility(View.VISIBLE);
            return;
        }

        recentEntries.sort((first, second) -> Long.compare(second.date, first.date));
        binding.tvRecentExpensesEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        int itemCount = Math.min(recentEntries.size(), 4);
        for (int index = 0; index < itemCount; index++) {
            RecentEntry recentEntry = recentEntries.get(index);
            ItemDashboardRecentExpenseBinding itemBinding =
                    ItemDashboardRecentExpenseBinding.inflate(inflater, binding.layoutRecentExpenses, false);

            if (recentEntry.isIncome()) {
                bindIncomeActivity(itemBinding, recentEntry.income);
            } else {
                bindExpenseActivity(itemBinding, recentEntry.expense);
            }

            binding.layoutRecentExpenses.addView(itemBinding.getRoot());
        }
    }

    private void bindExpenseActivity(@NonNull ItemDashboardRecentExpenseBinding itemBinding, @NonNull Expense expense) {
        itemBinding.tvRecentBadge.setText(CategoryUtils.getCategoryInitial(expense.getCategory()));
        itemBinding.tvRecentCategoryName.setText(expense.getCategory());
        itemBinding.tvRecentExpenseDate.setText(getString(
                R.string.expense_item_meta,
                FormatUtils.formatDate(expense.getDate()),
                getDisplaySource(expense.getPaymentSource())
        ));
        itemBinding.tvRecentType.setText(getString(R.string.debit_label));
        itemBinding.tvRecentType.setBackgroundResource(R.drawable.bg_soft_chip);
        itemBinding.tvRecentType.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));
        itemBinding.tvRecentExpenseAmount.setText("-" + FormatUtils.formatCurrency(expense.getAmount()));
        itemBinding.tvRecentExpenseAmount.setTextColor(ContextCompat.getColor(this, R.color.color_error));
        itemBinding.frameRecentBadge.setBackground(createBadgeBackground(
                ContextCompat.getColor(this, CategoryUtils.getCategoryColor(expense.getCategory()))
        ));
        itemBinding.cardRecentExpense.setOnClickListener(view -> openExpenseEditor(expense));
    }

    private void bindIncomeActivity(@NonNull ItemDashboardRecentExpenseBinding itemBinding, @NonNull Income income) {
        itemBinding.tvRecentBadge.setText(getIncomeInitial(income.getReason()));
        itemBinding.tvRecentCategoryName.setText(income.getReason());
        itemBinding.tvRecentExpenseDate.setText(FormatUtils.formatDate(income.getDate()));
        itemBinding.tvRecentType.setText(getString(R.string.credit_label));
        itemBinding.tvRecentType.setBackgroundResource(R.drawable.bg_badge_success);
        itemBinding.tvRecentType.setTextColor(ContextCompat.getColor(this, R.color.color_success));
        itemBinding.tvRecentExpenseAmount.setText("+" + FormatUtils.formatCurrency(income.getAmount()));
        itemBinding.tvRecentExpenseAmount.setTextColor(ContextCompat.getColor(this, R.color.color_success));
        itemBinding.frameRecentBadge.setBackground(createBadgeBackground(
                ContextCompat.getColor(this, R.color.color_success)
        ));
        itemBinding.cardRecentExpense.setOnClickListener(view -> openIncomeEditor(income));
    }

    private GradientDrawable createBadgeBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    private void openExpenseEditor(Expense expense) {
        Intent intent = new Intent(this, EditExpenseActivity.class);
        intent.putExtra(AppConstants.EXTRA_EXPENSE, expense);
        openScreen(intent, false, false);
    }

    private void openIncomeEditor(Income income) {
        Intent intent = new Intent(this, EditIncomeActivity.class);
        intent.putExtra(AppConstants.EXTRA_INCOME, income);
        openScreen(intent, false, false);
    }

    private void openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    private void openFromDrawer(Intent intent) {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        openScreen(intent, false, false);
    }

    private void redirectToLogin() {
        openScreen(new Intent(this, LoginActivity.class), true, true);
    }

    @NonNull
    private List<Expense> getCurrentMonthExpenses() {
        String monthKey = FormatUtils.monthKeyFromMillis(System.currentTimeMillis());
        List<Expense> monthExpenses = new ArrayList<>();
        for (Expense expense : expenses) {
            if (expense != null && FormatUtils.isSameMonth(expense.getDate(), monthKey)) {
                monthExpenses.add(expense);
            }
        }
        return monthExpenses;
    }

    @NonNull
    private List<Income> getCurrentMonthIncomes() {
        String monthKey = FormatUtils.monthKeyFromMillis(System.currentTimeMillis());
        List<Income> monthIncomes = new ArrayList<>();
        for (Income income : incomes) {
            if (income != null && FormatUtils.isSameMonth(income.getDate(), monthKey)) {
                monthIncomes.add(income);
            }
        }
        return monthIncomes;
    }

    @Nullable
    private Expense getHighestExpense(@NonNull List<Expense> monthExpenses) {
        Expense highestExpense = null;
        for (Expense expense : monthExpenses) {
            if (highestExpense == null || expense.getAmount() > highestExpense.getAmount()) {
                highestExpense = expense;
            }
        }
        return highestExpense;
    }

    @Nullable
    private Income getHighestIncome(@NonNull List<Income> monthIncomes) {
        Income highestIncome = null;
        for (Income income : monthIncomes) {
            if (highestIncome == null || income.getAmount() > highestIncome.getAmount()) {
                highestIncome = income;
            }
        }
        return highestIncome;
    }

    private String getIncomeInitial(@Nullable String reason) {
        if (TextUtils.isEmpty(reason)) {
            return "I";
        }
        return reason.substring(0, 1).toUpperCase(Locale.getDefault());
    }

    private String getDisplaySource(@Nullable String source) {
        if (AppConstants.EXPENSE_SOURCE_INCOME_BALANCE.equals(source)) {
            return getString(R.string.source_income_balance);
        }
        return getString(R.string.source_monthly_budget);
    }

    private String getFallbackUserName() {
        FirebaseUser firebaseUser = authRepository.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            return getString(R.string.drawer_account_fallback);
        }
        String email = firebaseUser.getEmail();
        int atIndex = email.indexOf("@");
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private String getProfileInitial(String displayName, String email) {
        if (!TextUtils.isEmpty(displayName)) {
            return displayName.substring(0, 1).toUpperCase(Locale.getDefault());
        }
        if (!TextUtils.isEmpty(email)) {
            return email.substring(0, 1).toUpperCase(Locale.getDefault());
        }
        return "?";
    }

    private static class RecentEntry {
        private final long date;
        @Nullable
        private final Expense expense;
        @Nullable
        private final Income income;

        private RecentEntry(long date, @Nullable Expense expense, @Nullable Income income) {
            this.date = date;
            this.expense = expense;
            this.income = income;
        }

        @NonNull
        static RecentEntry fromExpense(@NonNull Expense expense) {
            return new RecentEntry(expense.getDate(), expense, null);
        }

        @NonNull
        static RecentEntry fromIncome(@NonNull Income income) {
            return new RecentEntry(income.getDate(), null, income);
        }

        boolean isIncome() {
            return income != null;
        }
    }
}
