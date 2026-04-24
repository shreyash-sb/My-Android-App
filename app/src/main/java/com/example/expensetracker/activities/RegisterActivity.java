package com.example.expensetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityRegisterBinding;
import com.example.expensetracker.firebase.AuthRepository;
import com.example.expensetracker.firebase.DataCallback;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.firebase.UserRepository;
import com.example.expensetracker.models.UserProfile;
import com.example.expensetracker.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(view -> attemptRegister());
        binding.tvLoginAction.setOnClickListener(view -> closeScreen());
        binding.etPassword.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
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

    private void attemptRegister() {
        clearErrors();
        String name = getText(binding.etName.getText());
        String email = getText(binding.etEmail.getText());
        String phone = ValidationUtils.normalizePhone(getText(binding.etPhone.getText()));
        String password = getText(binding.etPassword.getText());

        if (!ValidationUtils.isNameValid(name)) {
            binding.tilName.setError(getString(R.string.name_required));
            return;
        }

        if (!ValidationUtils.isEmailValid(email)) {
            binding.tilEmail.setError(TextUtils.isEmpty(email) ? getString(R.string.email_required) : getString(R.string.invalid_email));
            return;
        }

        if (!ValidationUtils.isPhoneValid(phone)) {
            binding.tilPhone.setError(getString(R.string.invalid_mobile));
            return;
        }

        if (!ValidationUtils.isPasswordValid(password)) {
            binding.tilPassword.setError(TextUtils.isEmpty(password) ? getString(R.string.password_required) : getString(R.string.password_too_short));
            return;
        }

        setLoading(true);
        authRepository.register(email, password, new DataCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser data) {
                if (data == null) {
                    setLoading(false);
                    showMessage(binding.getRoot(), getString(R.string.generic_error));
                    return;
                }

                UserProfile userProfile = new UserProfile(data.getUid(), name, email, phone, System.currentTimeMillis());
                userRepository.createUserProfile(userProfile, new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        setLoading(false);
                        openScreen(new Intent(RegisterActivity.this, DashboardActivity.class), true, true);
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        setLoading(false);
                        authRepository.logout();
                        showMessage(binding.getRoot(), getString(R.string.save_failed));
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                setLoading(false);
                showMessage(
                        binding.getRoot(),
                        UserRepository.ERROR_PHONE_IN_USE.equals(exception.getMessage())
                                ? getString(R.string.mobile_in_use)
                                : getString(R.string.register_failed)
                );
            }
        });
    }

    private void clearErrors() {
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
    }

    private void setLoading(boolean isLoading) {
        binding.progressRegister.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
        binding.etName.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPhone.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.tvLoginAction.setEnabled(!isLoading);
        binding.btnRegister.setText(isLoading ? R.string.creating_account : R.string.register_action);
    }

    private String getText(@Nullable CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
