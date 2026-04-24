package com.example.expensetracker.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expensetracker.models.SavingsGoal;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class SavingsGoalRepository {

    @Nullable
    public ListenerRegistration listenGoalForMonth(@NonNull String monthKey,
                                                   @NonNull DataCallback<SavingsGoal> callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return null;
        }

        return FirebaseProvider.getGoalsCollection(userId)
                .document(monthKey)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }
                    try {
                        callback.onSuccess(mapGoal(value));
                    } catch (Exception exception) {
                        callback.onFailure(exception);
                    }
                });
    }

    public void saveGoal(@NonNull SavingsGoal goal, @NonNull OperationCallback callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        goal.setUserId(userId);
        goal.setId(goal.getMonthKey());
        FirebaseProvider.getGoalsCollection(userId)
                .document(goal.getMonthKey())
                .set(goal)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    private SavingsGoal mapGoal(@Nullable DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }

        SavingsGoal goal = snapshot.toObject(SavingsGoal.class);
        if (goal != null && goal.getId() == null) {
            goal.setId(snapshot.getId());
        }
        return goal;
    }
}
