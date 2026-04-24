package com.example.expensetracker.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.databinding.ItemExpenseBinding;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.AppConstants;
import com.example.expensetracker.utils.CategoryUtils;
import com.example.expensetracker.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface Listener {
        void onExpenseClicked(@NonNull Expense expense);
    }

    private final List<Expense> expenses = new ArrayList<>();
    private final Listener listener;

    public ExpenseAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void setExpenses(@NonNull List<Expense> updatedExpenses) {
        expenses.clear();
        expenses.addAll(updatedExpenses);
        notifyDataSetChanged();
    }

    @NonNull
    public Expense getExpenseAt(int position) {
        return expenses.get(position);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(expenses.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private final ItemExpenseBinding binding;

        ExpenseViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Expense expense, Listener listener) {
            Context context = binding.getRoot().getContext();
            binding.tvCategoryBadge.setText(CategoryUtils.getCategoryInitial(expense.getCategory()));
            binding.tvCategoryName.setText(expense.getCategory());
            binding.tvExpenseAmount.setText(FormatUtils.formatCurrency(expense.getAmount()));
            binding.tvExpenseDate.setText(context.getString(
                    com.example.expensetracker.R.string.expense_item_meta,
                    FormatUtils.formatDate(expense.getDate()),
                    getDisplaySource(context, expense)
            ));
            binding.tvExpenseNote.setText(expense.getNote());
            binding.tvExpenseNote.setVisibility(TextUtils.isEmpty(expense.getNote()) ? View.GONE : View.VISIBLE);
            binding.tvCategoryBadge.setBackground(createBadgeBackground(context, expense.getCategory()));
            binding.cardExpense.setOnClickListener(view -> listener.onExpenseClicked(expense));
        }

        private String getDisplaySource(Context context, Expense expense) {
            if (AppConstants.EXPENSE_SOURCE_INCOME_BALANCE.equals(expense.getPaymentSource())) {
                return context.getString(com.example.expensetracker.R.string.source_income_balance);
            }
            return context.getString(com.example.expensetracker.R.string.source_monthly_budget);
        }

        private GradientDrawable createBadgeBackground(Context context, String category) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(ContextCompat.getColor(context, CategoryUtils.getCategoryColor(category)));
            return drawable;
        }
    }
}
