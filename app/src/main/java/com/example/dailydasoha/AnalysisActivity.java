package com.example.dailydasoha;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.app.DatePickerDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;

public class AnalysisActivity extends BaseActivity {
    private String startDateStr, endDateStr;
    private int workingDaysCount, nonWorkingDaysCount;
    private double avgAttendance15, avgAttendance68, avgAttendance910;
    private int totalMilk, totalRice, totalWheat, totalDhal, totalOil, totalSalt;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Analysis");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load and display analysis data
        loadAnalysisData();
    }

    private void loadAnalysisData() {
        // Initialize views
        EditText etStartDate = findViewById(R.id.etStartDate);
        EditText etEndDate = findViewById(R.id.etEndDate);
        TextView tvWorkingDays = findViewById(R.id.tvWorkingDays);
        TextView tvNonWorkingDays = findViewById(R.id.tvNonWorkingDays);
        TextView tvAvgAttendance15 = findViewById(R.id.tvAvgAttendance15);
        TextView tvAvgAttendance68 = findViewById(R.id.tvAvgAttendance68);
        TextView tvAvgAttendance910 = findViewById(R.id.tvAvgAttendance910);
        TextView tvTotalMilk = findViewById(R.id.tvTotalMilk);
        TextView tvTotalRice = findViewById(R.id.tvTotalRice);
        TextView tvTotalWheat = findViewById(R.id.tvTotalWheat);
        TextView tvTotalDhal = findViewById(R.id.tvTotalDhal);
        TextView tvTotalOil = findViewById(R.id.tvTotalOil);
        TextView tvTotalSalt = findViewById(R.id.tvTotalSalt);

        // Set date pickers
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate, dateFormat));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate, dateFormat));

        // Load data when both dates are selected
        etEndDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !etStartDate.getText().toString().isEmpty() && !etEndDate.getText().toString().isEmpty()) {
                loadDataForDateRange(etStartDate.getText().toString(), etEndDate.getText().toString());
            }
        });
    }

    private void showDatePickerDialog(EditText editText, SimpleDateFormat dateFormat) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                editText.setText(dateFormat.format(selectedDate.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadDataForDateRange(String startDate, String endDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            startCal.setTime(dateFormat.parse(startDate));
            endCal.setTime(dateFormat.parse(endDate));

            // Set start to beginning of day and end to end of day
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);

            startDateStr = startDate;
            endDateStr = endDate;

            // Query Firestore for data in date range
            db.collection("daily_data")
                .whereGreaterThanOrEqualTo("date", startCal.getTimeInMillis())
                .whereLessThanOrEqualTo("date", endCal.getTimeInMillis())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Reset counters
                    workingDaysCount = 0;
                    nonWorkingDaysCount = 0;
                    double totalAttendance15 = 0, totalAttendance68 = 0, totalAttendance910 = 0;
                    totalMilk = 0;
                    totalRice = 0;
                    totalWheat = 0;
                    totalDhal = 0;
                    totalOil = 0;
                    totalSalt = 0;

                    // Process each document
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        boolean isWorkingDay = document.getBoolean("isWorkingDay");
                        if (isWorkingDay) {
                            workingDaysCount++;
                            // Sum attendance
                            totalAttendance15 += document.getLong("attendance1to5");
                            totalAttendance68 += document.getLong("attendance6to8");
                            totalAttendance910 += document.getLong("attendance9to10");
                            // Sum inventory
                            totalMilk += document.getLong("milk15") + document.getLong("milk68") + document.getLong("milk910");
                            totalRice += document.getLong("rice15") + document.getLong("rice68") + document.getLong("rice910");
                            totalWheat += document.getLong("wheat15") + document.getLong("wheat68") + document.getLong("wheat910");
                            totalDhal += document.getLong("dhal15") + document.getLong("dhal68") + document.getLong("dhal910");
                            totalOil += document.getLong("oil15") + document.getLong("oil68") + document.getLong("oil910");
                            totalSalt += document.getLong("salt15") + document.getLong("salt68") + document.getLong("salt910");
                        } else {
                            nonWorkingDaysCount++;
                        }
                    }

                    // Calculate averages
                    avgAttendance15 = workingDaysCount > 0 ? totalAttendance15 / workingDaysCount : 0;
                    avgAttendance68 = workingDaysCount > 0 ? totalAttendance68 / workingDaysCount : 0;
                    avgAttendance910 = workingDaysCount > 0 ? totalAttendance910 / workingDaysCount : 0;

                    // Update UI
                    updateUI();
                })
                .addOnFailureListener(e -> showError("Error loading data: " + e.getMessage()));

        } catch (Exception e) {
            showError("Error parsing dates: " + e.getMessage());
        }
    }

    private void updateUI() {
        // Update TextViews with the calculated data
        TextView tvWorkingDays = findViewById(R.id.tvWorkingDays);
        TextView tvNonWorkingDays = findViewById(R.id.tvNonWorkingDays);
        TextView tvAvgAttendance15 = findViewById(R.id.tvAvgAttendance15);
        TextView tvAvgAttendance68 = findViewById(R.id.tvAvgAttendance68);
        TextView tvAvgAttendance910 = findViewById(R.id.tvAvgAttendance910);
        TextView tvTotalMilk = findViewById(R.id.tvTotalMilk);
        TextView tvTotalRice = findViewById(R.id.tvTotalRice);
        TextView tvTotalWheat = findViewById(R.id.tvTotalWheat);
        TextView tvTotalDhal = findViewById(R.id.tvTotalDhal);
        TextView tvTotalOil = findViewById(R.id.tvTotalOil);
        TextView tvTotalSalt = findViewById(R.id.tvTotalSalt);

        tvWorkingDays.setText("Working Days: " + workingDaysCount);
        tvNonWorkingDays.setText("Non-Working Days: " + nonWorkingDaysCount);
        tvAvgAttendance15.setText(String.format("Class 1-5: %.2f", avgAttendance15));
        tvAvgAttendance68.setText(String.format("Class 6-8: %.2f", avgAttendance68));
        tvAvgAttendance910.setText(String.format("Class 9-10: %.2f", avgAttendance910));
        tvTotalMilk.setText("Milk: " + totalMilk + " g");
        tvTotalRice.setText("Rice: " + totalRice + " g");
        tvTotalWheat.setText("Wheat: " + totalWheat + " g");
        tvTotalDhal.setText("Dhal: " + totalDhal + " g");
        tvTotalOil.setText("Oil: " + totalOil + " g");
        tvTotalSalt.setText("Salt: " + totalSalt + " g");
    }
} 