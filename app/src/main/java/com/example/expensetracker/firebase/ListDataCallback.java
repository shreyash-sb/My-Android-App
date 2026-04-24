package com.example.expensetracker.firebase;

import androidx.annotation.NonNull;

import java.util.List;

public interface ListDataCallback<T> {
    void onSuccess(@NonNull List<T> data);

    void onFailure(@NonNull Exception exception);
}
