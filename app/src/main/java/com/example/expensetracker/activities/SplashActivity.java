package com.example.expensetracker.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.expensetracker.databinding.ActivitySplashBinding;
import com.example.expensetracker.firebase.AuthRepository;
import com.example.expensetracker.utils.AppPreferences;
import com.example.expensetracker.utils.NotificationHelper;

public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = this::navigateNext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPreferences.applyTheme(this);
        NotificationHelper.createBudgetAlertChannel(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        playIntroAnimation();
        handler.postDelayed(navigateRunnable, 1500);
    }

    private void playIntroAnimation() {
        binding.logoContainer.setAlpha(0f);
        binding.progressGroup.setAlpha(0f);
        binding.tvSplashVersion.setAlpha(0f);

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(binding.logoFrame, "scaleX", 0.88f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(binding.logoFrame, "scaleY", 0.88f, 1f);
        ObjectAnimator containerAlpha = ObjectAnimator.ofFloat(binding.logoContainer, "alpha", 0f, 1f);
        ObjectAnimator titleTranslation = ObjectAnimator.ofFloat(binding.tvSplashTitle, "translationY", 24f, 0f);
        ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(binding.tvSplashSubtitle, "alpha", 0f, 1f);
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(binding.tvSplashTagline, "alpha", 0f, 1f);
        ObjectAnimator progressAlpha = ObjectAnimator.ofFloat(binding.progressGroup, "alpha", 0f, 1f);
        ObjectAnimator versionAlpha = ObjectAnimator.ofFloat(binding.tvSplashVersion, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                logoScaleX,
                logoScaleY,
                containerAlpha,
                titleTranslation,
                subtitleAlpha,
                taglineAlpha,
                progressAlpha,
                versionAlpha
        );
        animatorSet.setDuration(760);
        animatorSet.start();
    }

    private void navigateNext() {
        Class<?> target = authRepository.isLoggedIn() ? DashboardActivity.class : LoginActivity.class;
        openScreen(new Intent(this, target), true, true);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateRunnable);
        super.onDestroy();
    }
}
