package com.example.expensetracker.firebase;

import androidx.annotation.NonNull;

public interface OperationCallback {
    void onSuccess();

    void onFailure(@NonNull Exception exception);
}
