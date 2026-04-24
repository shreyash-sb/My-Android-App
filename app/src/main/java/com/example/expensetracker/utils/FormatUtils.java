package com.example.expensetracker.utils;

import android.text.TextUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FormatUtils {

    private static final Locale INDIA_LOCALE = new Locale("en", "IN");

    private FormatUtils() {
    }

    public static String formatCurrency(double amount) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        return numberFormat.format(amount);
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    public static String formatMonthLabel(long millis) {
        return new SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    public static String formatTimeStamp(long millis) {
        return new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    public static String monthKeyFromMillis(long millis) {
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date(millis));
    }

    public static long createDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static Calendar calendarFromMillis(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    public static boolean isSameDay(long first, long second) {
        Calendar firstCalendar = calendarFromMillis(first);
        Calendar secondCalendar = calendarFromMillis(second);
        return firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR)
                && firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameMonth(long millis, String monthKey) {
        return TextUtils.equals(monthKeyFromMillis(millis), monthKey);
    }

    public static String buildMonthKey(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return monthKeyFromMillis(calendar.getTimeInMillis());
    }

    public static Set<String> buildMonthOptions(List<Long> dates) {
        Set<String> monthOptions = new LinkedHashSet<>();
        for (Long date : dates) {
            monthOptions.add(formatMonthLabel(date));
        }
        return monthOptions;
    }
}
