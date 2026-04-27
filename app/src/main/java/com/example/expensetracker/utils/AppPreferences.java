package com.example.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public final class AppPreferences {

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private static final String PREFS_NAME = "trackify_preferences";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_ALERT_ENABLED = "budget_alert_enabled";
    private static final String KEY_ALERT_THRESHOLD = "budget_alert_threshold";
    private static final String KEY_LAST_BUDGET_ALERT_SIGNATURE = "last_budget_alert_signature";
    private static final int DEFAULT_ALERT_THRESHOLD = 80;

    private AppPreferences() {
    }

    @NonNull
    private static SharedPreferences prefs(@NonNull Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void applyTheme(@NonNull Context context) {
        AppCompatDelegate.setDefaultNightMode(getThemeModeValue(context));
    }

    public static int getThemeModeValue(@NonNull Context context) {
        String value = prefs(context).getString(KEY_THEME, THEME_SYSTEM);
        if (THEME_LIGHT.equals(value)) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        if (THEME_DARK.equals(value)) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    @NonNull
    public static String getThemeMode(@NonNull Context context) {
        return prefs(context).getString(KEY_THEME, THEME_SYSTEM);
    }

    public static void setThemeMode(@NonNull Context context, @NonNull String mode) {
        prefs(context).edit().putString(KEY_THEME, mode).apply();
        applyTheme(context);
    }

    public static boolean isBudgetAlertEnabled(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_ALERT_ENABLED, true);
    }

    public static void setBudgetAlertEnabled(@NonNull Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ALERT_ENABLED, enabled).apply();
    }

    public static int getBudgetAlertThreshold(@NonNull Context context) {
        return prefs(context).getInt(KEY_ALERT_THRESHOLD, DEFAULT_ALERT_THRESHOLD);
    }

    public static void setBudgetAlertThreshold(@NonNull Context context, int threshold) {
        prefs(context).edit().putInt(KEY_ALERT_THRESHOLD, threshold).apply();
    }

    @NonNull
    public static String getLastBudgetAlertSignature(@NonNull Context context) {
        return prefs(context).getString(KEY_LAST_BUDGET_ALERT_SIGNATURE, "");
    }

    public static void setLastBudgetAlertSignature(@NonNull Context context, @NonNull String signature) {
        prefs(context).edit().putString(KEY_LAST_BUDGET_ALERT_SIGNATURE, signature).apply();
    }

    public static void clearLastBudgetAlertSignature(@NonNull Context context) {
        prefs(context).edit().remove(KEY_LAST_BUDGET_ALERT_SIGNATURE).apply();
    }
}
