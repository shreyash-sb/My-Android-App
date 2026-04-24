package com.example.expensetracker.firebase;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    public boolean isLoggedIn() {
        return FirebaseProvider.getAuth().getCurrentUser() != null;
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return FirebaseProvider.getAuth().getCurrentUser();
    }

    @Nullable
    public String getCurrentUserId() {
        return FirebaseProvider.getCurrentUserId();
    }

    public void login(String email, String password, DataCallback<FirebaseUser> callback) {
        FirebaseProvider.getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(callback::onFailure);
    }

    public void register(String email, String password, DataCallback<FirebaseUser> callback) {
        FirebaseProvider.getAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(callback::onFailure);
    }

    public void sendPasswordResetEmail(String email, OperationCallback callback) {
        FirebaseProvider.getAuth()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void logout() {
        FirebaseProvider.getAuth().signOut();
    }
}
