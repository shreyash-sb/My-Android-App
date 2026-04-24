package com.example.expensetracker.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface DataCallback<T> {
    void onSuccess(@Nullable T data);

    void onFailure(@NonNull Exception exception);
}
