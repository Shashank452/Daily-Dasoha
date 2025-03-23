package com.example.dailydasoha;

import android.os.Bundle;
import android.widget.TextView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormatSymbols;
import java.util.Map;
import java.util.HashMap;

import com.example.dailydasoha.dialogs.MonthPickerDialog;
import com.example.dailydasoha.adapters.ReportPrintAdapter;

public class MonthlyReportActivity extends BaseActivity {
    private TextView tvPeriodHeader;
    private TextView tvStudents15, tvStudents68, tvStudents910;
    private TextView tvMilk15, tvMilk68, tvMilk910;
    private TextView tvRice15, tvRice68, tvRice910;
    private TextView tvWheat15, tvWheat68, tvWheat910;
    private TextView tvDhal15, tvDhal68, tvDhal910;
    private TextView tvOil15, tvOil68, tvOil910;
    private TextView tvSalt15, tvSalt68, tvSalt910;
    private FirebaseFirestore db;
    private int month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupToolbar();
        showMonthYearPicker();
    }

    private void initializeViews() {
        tvPeriodHeader = findViewById(R.id.tvPeriodHeader);
        tvStudents15 = findViewById(R.id.tvStudents15);
        tvStudents68 = findViewById(R.id.tvStudents68);
        tvStudents910 = findViewById(R.id.tvStudents910);
        tvMilk15 = findViewById(R.id.tvMilk15);
        tvMilk68 = findViewById(R.id.tvMilk68);
        tvMilk910 = findViewById(R.id.tvMilk910);
        tvRice15 = findViewById(R.id.tvRice15);
        tvRice68 = findViewById(R.id.tvRice68);
        tvRice910 = findViewById(R.id.tvRice910);
        tvWheat15 = findViewById(R.id.tvWheat15);
        tvWheat68 = findViewById(R.id.tvWheat68);
        tvWheat910 = findViewById(R.id.tvWheat910);
        tvDhal15 = findViewById(R.id.tvDhal15);
        tvDhal68 = findViewById(R.id.tvDhal68);
        tvDhal910 = findViewById(R.id.tvDhal910);
        tvOil15 = findViewById(R.id.tvOil15);
        tvOil68 = findViewById(R.id.tvOil68);
        tvOil910 = findViewById(R.id.tvOil910);
        tvSalt15 = findViewById(R.id.tvSalt15);
        tvSalt68 = findViewById(R.id.tvSalt68);
        tvSalt910 = findViewById(R.id.tvSalt910);

        findViewById(R.id.btnPrint).setOnClickListener(v -> printReport());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Monthly Report");
        }
    }

    private void showMonthYearPicker() {
        Calendar cal = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(
            this,
            (selectedMonth, selectedYear) -> {
                month = selectedMonth;
                year = selectedYear;
                fetchAndDisplayData();
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH)
        );
        
        builder.setMinYear(2000)
            .setMaxYear(cal.get(Calendar.YEAR))
            .setTitle("Select Month and Year")
            .build()
            .show();
    }

    private void fetchAndDisplayData() {
        showProgressDialog("Generating report...");
        
        // Set period header
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        tvPeriodHeader.setText(dateFormat.format(cal.getTime()));
        
        // Create date range for query
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        
        startCal.set(year, month, 1, 0, 0, 0);
        endCal.set(year, month, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        
        try {
            db.collection("daily_data")
                .whereGreaterThanOrEqualTo("date", startCal.getTimeInMillis())
                .whereLessThanOrEqualTo("date", endCal.getTimeInMillis())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    hideProgressDialog();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        showToast("No data found for selected month");
                        return;
                    }
                    
                    ReportData reportData = new ReportData();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Boolean isWorkingDay = document.getBoolean("isWorkingDay");
                            if (isWorkingDay != null && isWorkingDay) {
                                // Students
                                reportData.addStudents15(getLongValue(document, "attendance1to5"));
                                reportData.addStudents68(getLongValue(document, "attendance6to8"));
                                reportData.addStudents910(getLongValue(document, "attendance9to10"));

                                // Milk
                                reportData.addMilk15(getLongValue(document, "milk15"));
                                reportData.addMilk68(getLongValue(document, "milk68"));
                                reportData.addMilk910(getLongValue(document, "milk910"));

                                // Rice
                                reportData.addRice15(getLongValue(document, "rice15"));
                                reportData.addRice68(getLongValue(document, "rice68"));
                                reportData.addRice910(getLongValue(document, "rice910"));

                                // Wheat
                                reportData.addWheat15(getLongValue(document, "wheat15"));
                                reportData.addWheat68(getLongValue(document, "wheat68"));
                                reportData.addWheat910(getLongValue(document, "wheat910"));

                                // Dhal
                                reportData.addDhal15(getLongValue(document, "dhal15"));
                                reportData.addDhal68(getLongValue(document, "dhal68"));
                                reportData.addDhal910(getLongValue(document, "dhal910"));

                                // Oil
                                reportData.addOil15(getLongValue(document, "oil15"));
                                reportData.addOil68(getLongValue(document, "oil68"));
                                reportData.addOil910(getLongValue(document, "oil910"));

                                // Salt
                                reportData.addSalt15(getLongValue(document, "salt15"));
                                reportData.addSalt68(getLongValue(document, "salt68"));
                                reportData.addSalt910(getLongValue(document, "salt910"));
                            }
                        } catch (Exception e) {
                            showError("Error processing document: " + e.getMessage());
                        }
                    }
                    
                    displayReport(reportData);
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    showError("Error fetching data: " + e.getMessage());
                });
        } catch (Exception e) {
            hideProgressDialog();
            showError("Error: " + e.getMessage());
        }
    }

    private int getLongValue(QueryDocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value != null ? value.intValue() : 0;
    }

    private void displayReport(ReportData data) {
        // Student attendance
        tvStudents15.setText(String.valueOf(data.getStudents15()));
        tvStudents68.setText(String.valueOf(data.getStudents68()));
        tvStudents910.setText(String.valueOf(data.getStudents910()));

        // Milk usage
        tvMilk15.setText(String.format(Locale.getDefault(), "%d g", data.getMilk15()));
        tvMilk68.setText(String.format(Locale.getDefault(), "%d g", data.getMilk68()));
        tvMilk910.setText(String.format(Locale.getDefault(), "%d g", data.getMilk910()));

        // Rice usage
        tvRice15.setText(String.format(Locale.getDefault(), "%d g", data.getRice15()));
        tvRice68.setText(String.format(Locale.getDefault(), "%d g", data.getRice68()));
        tvRice910.setText(String.format(Locale.getDefault(), "%d g", data.getRice910()));

        // Wheat usage
        tvWheat15.setText(String.format(Locale.getDefault(), "%d g", data.getWheat15()));
        tvWheat68.setText(String.format(Locale.getDefault(), "%d g", data.getWheat68()));
        tvWheat910.setText(String.format(Locale.getDefault(), "%d g", data.getWheat910()));

        // Dhal usage
        tvDhal15.setText(String.format(Locale.getDefault(), "%d g", data.getDhal15()));
        tvDhal68.setText(String.format(Locale.getDefault(), "%d g", data.getDhal68()));
        tvDhal910.setText(String.format(Locale.getDefault(), "%d g", data.getDhal910()));

        // Oil usage
        tvOil15.setText(String.format(Locale.getDefault(), "%d g", data.getOil15()));
        tvOil68.setText(String.format(Locale.getDefault(), "%d g", data.getOil68()));
        tvOil910.setText(String.format(Locale.getDefault(), "%d g", data.getOil910()));

        // Salt usage
        tvSalt15.setText(String.format(Locale.getDefault(), "%d g", data.getSalt15()));
        tvSalt68.setText(String.format(Locale.getDefault(), "%d g", data.getSalt68()));
        tvSalt910.setText(String.format(Locale.getDefault(), "%d g", data.getSalt910()));
    }

    private void printReport() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " - Monthly Report";

        Map<String, Object> reportData = collectReportData(); // Collect your report data
        String period = String.format(Locale.getDefault(), "%s %d", 
            new DateFormatSymbols().getMonths()[month], year);

        PrintDocumentAdapter printAdapter = new ReportPrintAdapter(this, reportData, 
            "Monthly Report", period);
        printManager.print(jobName, printAdapter, null);
    }

    private Map<String, Object> collectReportData() {
        Map<String, Object> reportData = new HashMap<>();
        
        // Get text values and convert to numbers
        reportData.put("workingdays15", getNumberFromText(tvStudents15));
        reportData.put("workingdays68", getNumberFromText(tvStudents68));
        reportData.put("workingdays910", getNumberFromText(tvStudents910));
        
        reportData.put("students15", getNumberFromText(tvStudents15));
        reportData.put("students68", getNumberFromText(tvStudents68));
        reportData.put("students910", getNumberFromText(tvStudents910));
        
        reportData.put("milk15", getNumberFromText(tvMilk15));
        reportData.put("milk68", getNumberFromText(tvMilk68));
        reportData.put("milk910", getNumberFromText(tvMilk910));
        
        reportData.put("rice15", getNumberFromText(tvRice15));
        reportData.put("rice68", getNumberFromText(tvRice68));
        reportData.put("rice910", getNumberFromText(tvRice910));
        
        reportData.put("wheat15", getNumberFromText(tvWheat15));
        reportData.put("wheat68", getNumberFromText(tvWheat68));
        reportData.put("wheat910", getNumberFromText(tvWheat910));
        
        reportData.put("dhal15", getNumberFromText(tvDhal15));
        reportData.put("dhal68", getNumberFromText(tvDhal68));
        reportData.put("dhal910", getNumberFromText(tvDhal910));
        
        reportData.put("oil15", getNumberFromText(tvOil15));
        reportData.put("oil68", getNumberFromText(tvOil68));
        reportData.put("oil910", getNumberFromText(tvOil910));
        
        reportData.put("salt15", getNumberFromText(tvSalt15));
        reportData.put("salt68", getNumberFromText(tvSalt68));
        reportData.put("salt910", getNumberFromText(tvSalt910));
        
        return reportData;
    }

    private int getNumberFromText(TextView textView) {
        String text = textView.getText().toString();
        // Remove any non-digit characters (like "g" or "ml")
        text = text.replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Helper class to store report data
    private static class ReportData {
        private int students15, students68, students910;
        private int milk15, milk68, milk910;
        private int rice15, rice68, rice910;
        private int wheat15, wheat68, wheat910;
        private int dhal15, dhal68, dhal910;
        private int oil15, oil68, oil910;
        private int salt15, salt68, salt910;

        // Student attendance methods
        public void addStudents15(int count) { students15 += count; }
        public void addStudents68(int count) { students68 += count; }
        public void addStudents910(int count) { students910 += count; }
        
        // Milk methods
        public void addMilk15(int amount) { milk15 += amount; }
        public void addMilk68(int amount) { milk68 += amount; }
        public void addMilk910(int amount) { milk910 += amount; }
        
        // Rice methods
        public void addRice15(int amount) { rice15 += amount; }
        public void addRice68(int amount) { rice68 += amount; }
        public void addRice910(int amount) { rice910 += amount; }
        
        // Wheat methods
        public void addWheat15(int amount) { wheat15 += amount; }
        public void addWheat68(int amount) { wheat68 += amount; }
        public void addWheat910(int amount) { wheat910 += amount; }
        
        // Dhal methods
        public void addDhal15(int amount) { dhal15 += amount; }
        public void addDhal68(int amount) { dhal68 += amount; }
        public void addDhal910(int amount) { dhal910 += amount; }
        
        // Oil methods
        public void addOil15(int amount) { oil15 += amount; }
        public void addOil68(int amount) { oil68 += amount; }
        public void addOil910(int amount) { oil910 += amount; }
        
        // Salt methods
        public void addSalt15(int amount) { salt15 += amount; }
        public void addSalt68(int amount) { salt68 += amount; }
        public void addSalt910(int amount) { salt910 += amount; }

        // Getters
        public int getStudents15() { return students15; }
        public int getStudents68() { return students68; }
        public int getStudents910() { return students910; }
        
        public int getMilk15() { return milk15; }
        public int getMilk68() { return milk68; }
        public int getMilk910() { return milk910; }
        
        public int getRice15() { return rice15; }
        public int getRice68() { return rice68; }
        public int getRice910() { return rice910; }
        
        public int getWheat15() { return wheat15; }
        public int getWheat68() { return wheat68; }
        public int getWheat910() { return wheat910; }
        
        public int getDhal15() { return dhal15; }
        public int getDhal68() { return dhal68; }
        public int getDhal910() { return dhal910; }
        
        public int getOil15() { return oil15; }
        public int getOil68() { return oil68; }
        public int getOil910() { return oil910; }
        
        public int getSalt15() { return salt15; }
        public int getSalt68() { return salt68; }
        public int getSalt910() { return salt910; }
    }
} 