package com.example.expensetracker;

import android.app.Application;

import com.example.expensetracker.utils.AppPreferences;
import com.example.expensetracker.utils.NotificationHelper;
import com.google.firebase.FirebaseApp;

public class ExpenseTrackerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        AppPreferences.applyTheme(this);
        NotificationHelper.createBudgetAlertChannel(this);
    }
}
