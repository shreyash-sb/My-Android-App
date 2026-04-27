package com.example.expensetracker.utils;

import android.text.TextUtils;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern GMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@gmail\\.com$", Pattern.CASE_INSENSITIVE);

    private ValidationUtils() {
    }

    public static boolean isNameValid(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    public static boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && GMAIL_PATTERN.matcher(normalizeEmail(email)).matches();
    }

    public static boolean isPhoneValid(String phone) {
        String normalized = normalizePhone(phone);
        return !TextUtils.isEmpty(normalized) && normalized.length() == 10;
    }

    public static boolean isLoginIdentifierValid(String value) {
        return isEmailValid(value) || isPhoneValid(value);
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.US);
    }

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[^0-9]", "").trim();
    }

    public static boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password) && password.trim().length() >= 6;
    }

    public static boolean isAmountValid(String value) {
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        try {
            return Double.parseDouble(value.trim()) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
