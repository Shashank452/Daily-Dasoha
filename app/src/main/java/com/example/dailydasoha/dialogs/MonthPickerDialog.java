package com.example.dailydasoha.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.example.dailydasoha.R;

public class MonthPickerDialog {
    private Context context;
    private AlertDialog dialog;
    private OnDateSetListener listener;
    private int year;
    private int month;
    private int minYear;
    private int maxYear;
    private String title;

    public interface OnDateSetListener {
        void onDateSet(int month, int year);
    }

    private MonthPickerDialog(Context context, OnDateSetListener listener, int year, int month) {
        this.context = context;
        this.listener = listener;
        this.year = year;
        this.month = month;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_month_picker, null);
        
        NumberPicker monthPicker = dialogView.findViewById(R.id.monthPicker);
        NumberPicker yearPicker = dialogView.findViewById(R.id.yearPicker);

        // Setup month picker
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", 
                                     "July", "August", "September", "October", "November", "December"};
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);
        monthPicker.setValue(month);

        // Setup year picker
        yearPicker.setMinValue(minYear);
        yearPicker.setMaxValue(maxYear);
        yearPicker.setValue(year);

        builder.setView(dialogView)
               .setTitle(title)
               .setPositiveButton("OK", (dialog, which) -> {
                   listener.onDateSet(monthPicker.getValue(), yearPicker.getValue());
               })
               .setNegativeButton("Cancel", null);

        dialog = builder.create();
        dialog.show();
    }

    public static class Builder {
        private Context context;
        private OnDateSetListener listener;
        private int year;
        private int month;
        private int minYear = 2000;
        private int maxYear = 2100;
        private String title = "Select Month and Year";

        public Builder(Context context, OnDateSetListener listener, int year, int month) {
            this.context = context;
            this.listener = listener;
            this.year = year;
            this.month = month;
        }

        public Builder setMinYear(int minYear) {
            this.minYear = minYear;
            return this;
        }

        public Builder setMaxYear(int maxYear) {
            this.maxYear = maxYear;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public MonthPickerDialog build() {
            MonthPickerDialog dialog = new MonthPickerDialog(context, listener, year, month);
            dialog.minYear = minYear;
            dialog.maxYear = maxYear;
            dialog.title = title;
            return dialog;
        }
    }
}