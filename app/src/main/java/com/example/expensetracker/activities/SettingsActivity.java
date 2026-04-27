package com.example.expensetracker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivitySettingsBinding;
import com.example.expensetracker.utils.AppPreferences;
import com.example.expensetracker.utils.DropdownUtils;

import java.util.Arrays;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> updatePermissionUi());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarSettings, true);

        setupDropdowns();
        bindSettings();
        binding.switchBudgetAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> toggleAlertControls(isChecked));
        binding.btnRequestNotifications.setOnClickListener(view -> requestNotificationPermission());
        binding.btnSaveSettings.setOnClickListener(view -> saveSettings());
        updatePermissionUi();
    }

    private void setupDropdowns() {
        ArrayAdapter<String> themeAdapter = DropdownUtils.createAdapter(
                this,
                Arrays.asList(getResources().getStringArray(R.array.theme_modes))
        );
        binding.actvThemeMode.setAdapter(themeAdapter);
        DropdownUtils.setupDropdown(binding.actvThemeMode, binding.tilThemeMode);

        ArrayAdapter<String> thresholdAdapter = DropdownUtils.createAdapter(
                this,
                Arrays.asList(getResources().getStringArray(R.array.alert_threshold_labels))
        );
        binding.actvAlertThreshold.setAdapter(thresholdAdapter);
        DropdownUtils.setupDropdown(binding.actvAlertThreshold, binding.tilAlertThreshold);
    }

    private void bindSettings() {
        binding.actvThemeMode.setText(getThemeLabel(AppPreferences.getThemeMode(this)), false);
        binding.switchBudgetAlerts.setChecked(AppPreferences.isBudgetAlertEnabled(this));
        binding.actvAlertThreshold.setText(getThresholdLabel(AppPreferences.getBudgetAlertThreshold(this)), false);
        toggleAlertControls(binding.switchBudgetAlerts.isChecked());
    }

    private void toggleAlertControls(boolean enabled) {
        binding.tilAlertThreshold.setEnabled(enabled);
        binding.actvAlertThreshold.setEnabled(enabled);
        binding.btnRequestNotifications.setEnabled(enabled);
        binding.tvNotificationPermissionHint.setAlpha(enabled ? 1f : 0.5f);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void updatePermissionUi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.btnRequestNotifications.setVisibility(View.GONE);
            binding.tvNotificationPermissionHint.setText(R.string.settings_permission_not_required_hint);
            return;
        }

        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        binding.btnRequestNotifications.setVisibility(View.VISIBLE);
        binding.btnRequestNotifications.setText(granted
                ? R.string.settings_notification_permission_granted
                : R.string.settings_notification_permission);
        binding.tvNotificationPermissionHint.setText(granted
                ? R.string.settings_permission_granted_hint
                : R.string.settings_permission_needed_hint);
    }

    private void saveSettings() {
        String themeValue = getThemeValue(binding.actvThemeMode.getText() == null ? "" : binding.actvThemeMode.getText().toString());
        boolean alertsEnabled = binding.switchBudgetAlerts.isChecked();
        int thresholdValue = getThresholdValue(binding.actvAlertThreshold.getText() == null ? "" : binding.actvAlertThreshold.getText().toString());

        boolean requiresRestart = !AppPreferences.getThemeMode(this).equals(themeValue);

        AppPreferences.setThemeMode(this, themeValue);
        AppPreferences.setBudgetAlertEnabled(this, alertsEnabled);
        AppPreferences.setBudgetAlertThreshold(this, thresholdValue);

        if (requiresRestart) {
            openScreen(new Intent(this, SplashActivity.class), true, true);
            return;
        }
        showMessage(binding.getRoot(), getString(R.string.settings_saved));
    }

    private String getThemeLabel(String value) {
        if (AppPreferences.THEME_LIGHT.equals(value)) {
            return getString(R.string.theme_light_label);
        }
        if (AppPreferences.THEME_DARK.equals(value)) {
            return getString(R.string.theme_dark_label);
        }
        return getString(R.string.theme_system_label);
    }

    private String getThemeValue(String label) {
        if (getString(R.string.theme_light_label).equals(label)) {
            return AppPreferences.THEME_LIGHT;
        }
        if (getString(R.string.theme_dark_label).equals(label)) {
            return AppPreferences.THEME_DARK;
        }
        return AppPreferences.THEME_SYSTEM;
    }

    private String getThresholdLabel(int threshold) {
        return threshold + "%";
    }

    private int getThresholdValue(String label) {
        String clean = label.replace("%", "").trim();
        try {
            return Integer.parseInt(clean);
        } catch (NumberFormatException exception) {
            return AppPreferences.getBudgetAlertThreshold(this);
        }
    }
}
