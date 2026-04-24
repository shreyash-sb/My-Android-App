package com.example.expensetracker.activities;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityProfileBinding;
import com.example.expensetracker.firebase.AuthRepository;
import com.example.expensetracker.firebase.DataCallback;
import com.example.expensetracker.firebase.OperationCallback;
import com.example.expensetracker.firebase.UserRepository;
import com.example.expensetracker.models.UserProfile;
import com.example.expensetracker.utils.FormatUtils;
import com.example.expensetracker.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();
    private UserProfile currentProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbarProfile, true);
        binding.btnSaveProfile.setOnClickListener(view -> saveProfile());
        loadProfile();
    }

    private void loadProfile() {
        userRepository.fetchCurrentUserProfile(new DataCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                currentProfile = data;
                bindProfile();
            }

            @Override
            public void onFailure(Exception exception) {
                showMessage(binding.getRoot(), getString(R.string.load_failed));
            }
        });
    }

    private void bindProfile() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        String email = currentProfile != null && !TextUtils.isEmpty(currentProfile.getEmail())
                ? currentProfile.getEmail()
                : (currentUser != null ? currentUser.getEmail() : "");
        String name = currentProfile != null ? currentProfile.getName() : "";
        String phone = currentProfile != null ? currentProfile.getPhone() : "";
        long createdAt = currentProfile != null && currentProfile.getCreatedAt() > 0
                ? currentProfile.getCreatedAt()
                : System.currentTimeMillis();

        binding.etProfileName.setText(name);
        binding.etProfilePhone.setText(phone);
        binding.etProfileEmail.setText(email);
        binding.tvProfileSince.setText(getString(
                R.string.drawer_profile_since_format,
                FormatUtils.formatMonthLabel(createdAt)
        ));
    }

    private void saveProfile() {
        String userId = authRepository.getCurrentUserId();
        FirebaseUser firebaseUser = authRepository.getCurrentUser();
        if (userId == null || firebaseUser == null || firebaseUser.getEmail() == null) {
            showMessage(binding.getRoot(), getString(R.string.auth_required));
            return;
        }

        binding.tilProfileName.setError(null);
        binding.tilProfilePhone.setError(null);

        String name = getText(binding.etProfileName.getText());
        String phone = ValidationUtils.normalizePhone(getText(binding.etProfilePhone.getText()));

        if (!ValidationUtils.isNameValid(name)) {
            binding.tilProfileName.setError(getString(R.string.name_required));
            return;
        }
        if (!ValidationUtils.isPhoneValid(phone)) {
            binding.tilProfilePhone.setError(getString(R.string.invalid_mobile));
            return;
        }

        long createdAt = currentProfile != null && currentProfile.getCreatedAt() > 0
                ? currentProfile.getCreatedAt()
                : System.currentTimeMillis();

        UserProfile updatedProfile = new UserProfile(
                userId,
                name,
                firebaseUser.getEmail(),
                phone,
                createdAt
        );

        setLoading(true);
        userRepository.updateUserProfile(updatedProfile, new OperationCallback() {
            @Override
            public void onSuccess() {
                currentProfile = updatedProfile;
                setLoading(false);
                showMessage(binding.getRoot(), getString(R.string.profile_saved));
            }

            @Override
            public void onFailure(Exception exception) {
                setLoading(false);
                showMessage(
                        binding.getRoot(),
                        UserRepository.ERROR_PHONE_IN_USE.equals(exception.getMessage())
                                ? getString(R.string.mobile_in_use)
                                : getString(R.string.update_failed)
                );
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressProfile.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnSaveProfile.setEnabled(!loading);
        binding.etProfileName.setEnabled(!loading);
        binding.etProfilePhone.setEnabled(!loading);
    }

    private String getText(@Nullable CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
