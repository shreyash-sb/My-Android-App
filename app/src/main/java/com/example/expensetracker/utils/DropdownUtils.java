package com.example.expensetracker.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expensetracker.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public final class DropdownUtils {

    private DropdownUtils() {
    }

    @NonNull
    public static ArrayAdapter<String> createAdapter(@NonNull Context context,
                                                     @NonNull List<String> items) {
        return new ArrayAdapter<>(context, R.layout.item_dropdown_option, items);
    }

    public static void setupDropdown(@NonNull MaterialAutoCompleteTextView autoCompleteTextView,
                                     @Nullable TextInputLayout textInputLayout) {
        autoCompleteTextView.setKeyListener(null);
        autoCompleteTextView.setCursorVisible(false);
        autoCompleteTextView.setOnClickListener(view -> autoCompleteTextView.showDropDown());
        autoCompleteTextView.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.showDropDown();
            }
        });
        autoCompleteTextView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                autoCompleteTextView.showDropDown();
            }
            return false;
        });
        if (textInputLayout != null) {
            textInputLayout.setEndIconOnClickListener(view -> autoCompleteTextView.showDropDown());
        }
    }
}
