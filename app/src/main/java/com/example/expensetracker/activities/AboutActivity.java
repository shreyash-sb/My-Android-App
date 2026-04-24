package com.example.expensetracker.activities;

import android.content.pm.PackageInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityAboutBinding;

public class AboutActivity extends BaseActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarAbout, true);
        binding.tvAboutVersion.setText(getString(R.string.about_version_format, getAppVersionName()));
    }

    private String getAppVersionName() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName == null ? "1.0" : packageInfo.versionName;
        } catch (Exception exception) {
            return "1.0";
        }
    }
}
