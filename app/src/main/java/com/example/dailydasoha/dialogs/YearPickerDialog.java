package com.example.dailydasoha.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.example.dailydasoha.R;

public class YearPickerDialog {
    private Context context;
    private OnYearSetListener listener;
    private int year;
    private String title;
    private AlertDialog dialog;

    public interface OnYearSetListener {
        void onYearSet(int year);
    }

    public static class Builder {
        private final YearPickerDialog dialog;

        public Builder(Context context, OnYearSetListener listener, int year) {
            dialog = new YearPickerDialog(context, listener, year);
        }

        public Builder setTitle(String title) {
            dialog.setTitle(title);
            return this;
        }

        public YearPickerDialog build() {
            return dialog;
        }
    }

    private YearPickerDialog(Context context, OnYearSetListener listener, int year) {
        this.context = context;
        this.listener = listener;
        this.year = year;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_year_picker, null);

        NumberPicker yearPicker = view.findViewById(R.id.yearPicker);
        yearPicker.setMinValue(2020);
        yearPicker.setMaxValue(2030);
        yearPicker.setValue(year);

        builder.setView(view)
                .setTitle(title)
                .setPositiveButton("OK", (dialog, which) -> {
                    listener.onYearSet(yearPicker.getValue());
                })
                .setNegativeButton("Cancel", null);

        dialog = builder.create();
        dialog.show();
    }
} 