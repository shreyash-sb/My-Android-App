package com.example.expensetracker.firebase;

import androidx.annotation.Nullable;

import com.example.expensetracker.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public final class FirebaseProvider {

    private FirebaseProvider() {
    }

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Nullable
    public static String getCurrentUserId() {
        return getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;
    }

    public static DocumentReference getUserDocument(String userId) {
        return getFirestore().collection(AppConstants.COLLECTION_USERS).document(userId);
    }

    public static CollectionReference getExpensesCollection(String userId) {
        return getUserDocument(userId).collection(AppConstants.COLLECTION_EXPENSES);
    }

    public static CollectionReference getIncomesCollection(String userId) {
        return getUserDocument(userId).collection(AppConstants.COLLECTION_INCOMES);
    }

    public static CollectionReference getBudgetsCollection(String userId) {
        return getUserDocument(userId).collection(AppConstants.COLLECTION_BUDGETS);
    }

    public static CollectionReference getGoalsCollection(String userId) {
        return getUserDocument(userId).collection(AppConstants.COLLECTION_GOALS);
    }

    public static DocumentReference getPhoneLookupDocument(String phone) {
        return getFirestore().collection(AppConstants.COLLECTION_PHONE_LOOKUP).document(phone);
    }
}
