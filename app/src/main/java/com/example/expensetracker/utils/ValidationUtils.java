package com.example.expensetracker.utils;

import android.text.TextUtils;
import android.util.Patterns;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isNameValid(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    public static boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isPhoneValid(String phone) {
        String normalized = normalizePhone(phone);
        return !TextUtils.isEmpty(normalized) && normalized.length() >= 10 && normalized.length() <= 15;
    }

    public static boolean isLoginIdentifierValid(String value) {
        return isEmailValid(value) || isPhoneValid(value);
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
