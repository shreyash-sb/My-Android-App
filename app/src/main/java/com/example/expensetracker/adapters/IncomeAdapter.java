package com.example.expensetracker.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.databinding.ItemIncomeBinding;
import com.example.expensetracker.models.Income;
import com.example.expensetracker.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    public interface Listener {
        void onIncomeClicked(@NonNull Income income);
    }

    private final List<Income> incomes = new ArrayList<>();
    private final Listener listener;

    public IncomeAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void setIncomes(@NonNull List<Income> updatedIncomes) {
        incomes.clear();
        incomes.addAll(updatedIncomes);
        notifyDataSetChanged();
    }

    @NonNull
    public Income getIncomeAt(int position) {
        return incomes.get(position);
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIncomeBinding binding = ItemIncomeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new IncomeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        holder.bind(incomes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return incomes.size();
    }

    static class IncomeViewHolder extends RecyclerView.ViewHolder {

        private final ItemIncomeBinding binding;

        IncomeViewHolder(ItemIncomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Income income, Listener listener) {
            binding.tvIncomeBadge.setText(getReasonInitial(income.getReason()));
            binding.tvIncomeReason.setText(income.getReason());
            binding.tvIncomeDate.setText(FormatUtils.formatDate(income.getDate()));
            binding.tvIncomeAmount.setText("+" + FormatUtils.formatCurrency(income.getAmount()));
            binding.cardIncome.setOnClickListener(view -> listener.onIncomeClicked(income));
        }

        @NonNull
        private String getReasonInitial(String reason) {
            if (TextUtils.isEmpty(reason)) {
                return "I";
            }
            return reason.substring(0, 1).toUpperCase(Locale.getDefault());
        }
    }
}
