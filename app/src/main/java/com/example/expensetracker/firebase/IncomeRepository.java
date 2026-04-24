package com.example.expensetracker.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expensetracker.models.Income;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class IncomeRepository {

    @Nullable
    public ListenerRegistration listenToIncomes(@NonNull ListDataCallback<Income> callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return null;
        }

        return FirebaseProvider.getIncomesCollection(userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }
                    try {
                        callback.onSuccess(mapIncomes(value));
                    } catch (Exception exception) {
                        callback.onFailure(exception);
                    }
                });
    }

    public void addIncome(@NonNull Income income, @NonNull OperationCallback callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        DocumentReference documentReference = FirebaseProvider.getIncomesCollection(userId).document();
        income.setId(documentReference.getId());
        income.setUserId(userId);

        documentReference
                .set(income)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void updateIncome(@NonNull Income income, @NonNull OperationCallback callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        DocumentReference documentReference = FirebaseProvider.getIncomesCollection(userId).document(income.getId());
        income.setUserId(userId);
        documentReference
                .set(income)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteIncome(@NonNull Income income, @NonNull OperationCallback callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        FirebaseProvider.getIncomesCollection(userId)
                .document(income.getId())
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void fetchIncomesOnce(@NonNull ListDataCallback<Income> callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        FirebaseProvider.getIncomesCollection(userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        callback.onSuccess(mapIncomes(queryDocumentSnapshots));
                    } catch (Exception exception) {
                        callback.onFailure(exception);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @NonNull
    private List<Income> mapIncomes(@Nullable QuerySnapshot value) {
        List<Income> incomes = new ArrayList<>();
        if (value == null) {
            return incomes;
        }

        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
            try {
                Income income = documentSnapshot.toObject(Income.class);
                if (income != null) {
                    income.setId(documentSnapshot.getId());
                    incomes.add(income);
                }
            } catch (RuntimeException ignored) {
                // Ignore malformed documents so one bad record does not crash the screen.
            }
        }
        return incomes;
    }
}
