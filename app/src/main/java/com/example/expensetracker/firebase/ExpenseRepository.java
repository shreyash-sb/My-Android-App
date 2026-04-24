package com.example.expensetracker.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expensetracker.models.Expense;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {

    @Nullable
    public ListenerRegistration listenToExpenses(@NonNull ListDataCallback<Expense> callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return null;
        }

        return FirebaseProvider.getExpensesCollection(userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }
                    try {
                        callback.onSuccess(mapExpenses(value));
                    } catch (Exception exception) {
                        callback.onFailure(exception);
                    }
                });
    }

    public void fetchExpensesOnce(@NonNull ListDataCallback<Expense> callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        FirebaseProvider.getExpensesCollection(userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        callback.onSuccess(mapExpenses(queryDocumentSnapshots));
                    } catch (Exception exception) {
                        callback.onFailure(exception);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void addExpense(@NonNull Expense expense, @NonNull OperationCallback callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        DocumentReference documentReference = FirebaseProvider.getExpensesCollection(userId).document();
        expense.setId(documentReference.getId());
        expense.setUserId(userId);
        expense.setImageUrl(null);

        saveExpense(documentReference, expense, callback);
    }

    public void updateExpense(@NonNull Expense expense, @NonNull OperationCallback callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        DocumentReference documentReference = FirebaseProvider.getExpensesCollection(userId).document(expense.getId());
        expense.setUserId(userId);
        expense.setImageUrl(null);

        saveExpense(documentReference, expense, callback);
    }

    public void deleteExpense(@NonNull Expense expense, @NonNull OperationCallback callback) {
        String userId = FirebaseProvider.getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        FirebaseProvider.getExpensesCollection(userId)
                .document(expense.getId())
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    private void saveExpense(DocumentReference documentReference,
                             Expense expense,
                             OperationCallback callback) {
        documentReference
                .set(expense)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @NonNull
    private List<Expense> mapExpenses(@Nullable QuerySnapshot value) {
        List<Expense> expenses = new ArrayList<>();
        if (value == null) {
            return expenses;
        }

        value.getDocuments().forEach(documentSnapshot -> {
            try {
                Expense expense = documentSnapshot.toObject(Expense.class);
                if (expense != null) {
                    expense.setId(documentSnapshot.getId());
                    expenses.add(expense);
                }
            } catch (RuntimeException ignored) {
                // Ignore malformed documents so one bad record does not crash the screen.
            }
        });
        return expenses;
    }
}
