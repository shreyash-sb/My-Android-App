package com.example.expensetracker.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expensetracker.models.Budget;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class BudgetRepository {

    @Nullable
    public ListenerRegistration listenBudgetForMonth(@NonNull String monthKey,
                                                     @NonNull DataCallback<Budget> callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return null;
        }

        return FirebaseProvider.getBudgetsCollection(userId)
                .document(monthKey)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }
                    try {
                        callback.onSuccess(mapBudget(value));
                    } catch (Exception exception) {
                        callback.onFailure(exception);
                    }
                });
    }

    public void saveBudget(@NonNull Budget budget, @NonNull OperationCallback callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        budget.setUserId(userId);
        budget.setId(budget.getMonthKey());
        FirebaseProvider.getBudgetsCollection(userId)
                .document(budget.getMonthKey())
                .set(budget)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    private Budget mapBudget(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }

        Budget budget = snapshot.toObject(Budget.class);
        if (budget != null && budget.getId() == null) {
            budget.setId(snapshot.getId());
        }
        return budget;
    }
}
