package com.example.expensetracker.firebase;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expensetracker.models.UserProfile;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    public static final String ERROR_PHONE_IN_USE = "PHONE_IN_USE";

    public void createUserProfile(@NonNull UserProfile profile, @NonNull OperationCallback callback) {
        FirebaseProvider.getFirestore()
                .runTransaction(transaction -> {
                    DocumentReference userDocument = FirebaseProvider.getUserDocument(profile.getUserId());
                    String phone = profile.getPhone();
                    if (!TextUtils.isEmpty(phone)) {
                        DocumentReference phoneLookupDocument = FirebaseProvider.getPhoneLookupDocument(phone);
                        DocumentSnapshot existingPhoneLookup = transaction.get(phoneLookupDocument);
                        String existingUserId = existingPhoneLookup.getString("userId");
                        if (existingPhoneLookup.exists()
                                && !TextUtils.isEmpty(existingUserId)
                                && !profile.getUserId().equals(existingUserId)) {
                            throw new IllegalStateException(ERROR_PHONE_IN_USE);
                        }
                        transaction.set(phoneLookupDocument, buildPhoneLookup(profile));
                    }
                    transaction.set(userDocument, profile);
                    return null;
                })
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUserProfile(@NonNull UserProfile profile, @NonNull OperationCallback callback) {
        FirebaseProvider.getFirestore()
                .runTransaction(transaction -> {
                    DocumentReference userDocument = FirebaseProvider.getUserDocument(profile.getUserId());
                    DocumentSnapshot existingUserSnapshot = transaction.get(userDocument);
                    UserProfile existingProfile = mapProfile(existingUserSnapshot);
                    String oldPhone = existingProfile != null ? existingProfile.getPhone() : null;
                    String newPhone = profile.getPhone();

                    if (!TextUtils.isEmpty(newPhone)) {
                        DocumentReference newPhoneLookupDocument = FirebaseProvider.getPhoneLookupDocument(newPhone);
                        DocumentSnapshot newPhoneLookup = transaction.get(newPhoneLookupDocument);
                        String existingUserId = newPhoneLookup.getString("userId");
                        if (newPhoneLookup.exists()
                                && !TextUtils.isEmpty(existingUserId)
                                && !profile.getUserId().equals(existingUserId)) {
                            throw new IllegalStateException(ERROR_PHONE_IN_USE);
                        }
                        transaction.set(newPhoneLookupDocument, buildPhoneLookup(profile));
                    }

                    transaction.set(userDocument, profile);

                    if (!TextUtils.isEmpty(oldPhone) && !TextUtils.equals(oldPhone, newPhone)) {
                        transaction.delete(FirebaseProvider.getPhoneLookupDocument(oldPhone));
                    }
                    return null;
                })
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void fetchCurrentUserProfile(@NonNull DataCallback<UserProfile> callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        FirebaseProvider.getUserDocument(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    try {
                        callback.onSuccess(mapProfile(snapshot));
                    } catch (Exception exception) {
                        callback.onFailure(exception);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void findUserByPhone(@NonNull String phone, @NonNull DataCallback<UserProfile> callback) {
        FirebaseProvider.getPhoneLookupDocument(phone)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists()) {
                        callback.onSuccess(null);
                        return;
                    }
                    UserProfile profile = new UserProfile();
                    profile.setUserId(snapshot.getString("userId"));
                    profile.setEmail(snapshot.getString("email"));
                    profile.setPhone(phone);
                    callback.onSuccess(profile);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void syncPhoneLookupForProfile(@Nullable UserProfile profile) {
        if (profile == null
                || TextUtils.isEmpty(profile.getUserId())
                || TextUtils.isEmpty(profile.getPhone())
                || TextUtils.isEmpty(profile.getEmail())) {
            return;
        }

        FirebaseProvider.getPhoneLookupDocument(profile.getPhone())
                .set(buildPhoneLookup(profile));
    }

    @NonNull
    private Map<String, Object> buildPhoneLookup(@NonNull UserProfile profile) {
        Map<String, Object> lookup = new HashMap<>();
        lookup.put("userId", profile.getUserId());
        lookup.put("email", profile.getEmail());
        lookup.put("phone", profile.getPhone());
        return lookup;
    }

    @Nullable
    private UserProfile mapProfile(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        UserProfile profile = snapshot.toObject(UserProfile.class);
        if (profile != null && profile.getUserId() == null) {
            profile.setUserId(snapshot.getId());
        }
        return profile;
    }
}
