package com.example.expensetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityLoginBinding;
import com.example.expensetracker.firebase.AuthRepository;
import com.example.expensetracker.firebase.DataCallback;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.firebase.UserRepository;
import com.example.expensetracker.models.UserProfile;
import com.example.expensetracker.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(view -> attemptLogin());
        binding.tvForgotPassword.setOnClickListener(view -> sendPasswordReset());
        binding.tvRegisterAction.setOnClickListener(view ->
                openScreen(new Intent(this, RegisterActivity.class), false, false));
        binding.etPassword.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authRepository.isLoggedIn()) {
            openScreen(new Intent(this, DashboardActivity.class), true, true);
        }
    }

    private void attemptLogin() {
        clearErrors();
        String identifier = getText(binding.etEmail.getText());
        String password = getText(binding.etPassword.getText());

        if (!ValidationUtils.isLoginIdentifierValid(identifier)) {
            binding.tilEmail.setError(TextUtils.isEmpty(identifier)
                    ? getString(R.string.login_identifier_required)
                    : getString(R.string.invalid_login_identifier));
            return;
        }

        if (!ValidationUtils.isPasswordValid(password)) {
            binding.tilPassword.setError(TextUtils.isEmpty(password) ? getString(R.string.password_required) : getString(R.string.password_too_short));
            return;
        }

        setLoading(true);
        if (ValidationUtils.isEmailValid(identifier)) {
            loginWithEmail(identifier, password);
            return;
        }

        String normalizedPhone = ValidationUtils.normalizePhone(identifier);
        userRepository.findUserByPhone(normalizedPhone, new DataCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                if (data == null || TextUtils.isEmpty(data.getEmail())) {
                    setLoading(false);
                    showMessage(binding.getRoot(), getString(R.string.mobile_login_not_found));
                    return;
                }
                loginWithEmail(data.getEmail(), password);
            }

            @Override
            public void onFailure(Exception exception) {
                setLoading(false);
                showMessage(binding.getRoot(), getString(R.string.login_failed));
            }
        });
    }

    private void loginWithEmail(String email, String password) {
        authRepository.login(email, password, new DataCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser data) {
                userRepository.fetchCurrentUserProfile(new DataCallback<UserProfile>() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                        userRepository.syncPhoneLookupForProfile(profile);
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        // Keep login fast even if the profile sync is not available yet.
                    }
                });
                setLoading(false);
                openScreen(new Intent(LoginActivity.this, DashboardActivity.class), true, true);
            }

            @Override
            public void onFailure(Exception exception) {
                setLoading(false);
                showMessage(binding.getRoot(), getString(R.string.login_failed));
            }
        });
    }

    private void sendPasswordReset() {
        String email = getText(binding.etEmail.getText());
        if (!ValidationUtils.isEmailValid(email)) {
            binding.tilEmail.setError(getString(R.string.enter_email_first));
            binding.etEmail.requestFocus();
            return;
        }

        binding.tilEmail.setError(null);
        binding.tvForgotPassword.setEnabled(false);
        authRepository.sendPasswordResetEmail(email, new OperationCallback() {
            @Override
            public void onSuccess() {
                binding.tvForgotPassword.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.reset_password_sent));
            }

            @Override
            public void onFailure(Exception exception) {
                binding.tvForgotPassword.setEnabled(true);
                showMessage(binding.getRoot(), getString(R.string.reset_password_failed));
            }
        });
    }

    private void clearErrors() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
    }

    private void setLoading(boolean isLoading) {
        binding.progressLogin.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.tvForgotPassword.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? R.string.logging_in : R.string.login_action);
    }

    private String getText(@Nullable CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
