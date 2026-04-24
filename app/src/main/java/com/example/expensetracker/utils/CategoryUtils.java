package com.example.expensetracker.utils;

import android.content.Context;

import androidx.annotation.ColorRes;

import com.example.expensetracker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class CategoryUtils {

    private CategoryUtils() {
    }

    public static List<String> getCategories(Context context) {
        return new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.expense_categories)));
    }

    public static String getCategoryInitial(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "?";
        }
        return category.trim().substring(0, 1).toUpperCase(Locale.getDefault());
    }

    @ColorRes
    public static int getCategoryColor(String category) {
        if (category == null) {
            return R.color.color_badge_other;
        }
        switch (category.toLowerCase(Locale.getDefault())) {
            case "food":
                return R.color.color_badge_food;
            case "transport":
                return R.color.color_badge_transport;
            case "shopping":
                return R.color.color_badge_shopping;
            case "bills":
                return R.color.color_badge_bills;
            case "entertainment":
                return R.color.color_badge_entertainment;
            case "health":
                return R.color.color_badge_health;
            case "travel":
                return R.color.color_badge_travel;
            case "education":
                return R.color.color_badge_education;
            default:
                return R.color.color_badge_other;
        }
    }
}
