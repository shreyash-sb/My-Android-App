package com.example.expensetracker.activities;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.utils.AppPreferences;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppPreferences.wrapContext(newBase));
    }

    protected void setupToolbar(@NonNull MaterialToolbar toolbar, boolean showBackButton) {
        if (showBackButton) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(view -> closeScreen());
        }
    }

    @SuppressWarnings("deprecation")
    protected void openScreen(@NonNull Intent intent, boolean finishCurrent, boolean clearTask) {
        if (clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        if (finishCurrent) {
            finish();
        }
    }

    @SuppressWarnings("deprecation")
    protected void closeScreen() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    protected void showMessage(@NonNull View anchor, @NonNull String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }
}
