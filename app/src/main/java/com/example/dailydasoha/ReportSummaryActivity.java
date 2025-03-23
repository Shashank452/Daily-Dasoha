package com.example.dailydasoha;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportSummaryActivity extends BaseActivity {
    private TextView tvPeriodHeader, tvWorkingDays;
    private TextView tvStudents15, tvStudents68, tvStudents910;
    private TextView tvTotalMilk, tvTotalRice, tvTotalWheat, tvTotalDhal, tvTotalOil, tvTotalSalt;
    private FirebaseFirestore db;
    private int month, year;
    private boolean isMonthlyReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_summary_layout);

        // Get parameters from intent
        month = getIntent().getIntExtra("MONTH", -1);
        year = getIntent().getIntExtra("YEAR", -1);
        isMonthlyReport = month != -1;

        initializeViews();
        setupToolbar();
        fetchAndDisplayData();
    }

    private void initializeViews() {
        tvPeriodHeader = findViewById(R.id.tvPeriodHeader);
        tvWorkingDays = findViewById(R.id.tvWorkingDays);
        tvStudents15 = findViewById(R.id.tvStudents15);
        tvStudents68 = findViewById(R.id.tvStudents68);
        tvStudents910 = findViewById(R.id.tvStudents910);
        tvTotalMilk = findViewById(R.id.tvTotalMilk);
        tvTotalRice = findViewById(R.id.tvTotalRice);
        tvTotalWheat = findViewById(R.id.tvTotalWheat);
        tvTotalDhal = findViewById(R.id.tvTotalDhal);
        tvTotalOil = findViewById(R.id.tvTotalOil);
        tvTotalSalt = findViewById(R.id.tvTotalSalt);

        db = FirebaseFirestore.getInstance();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isMonthlyReport ? "Monthly Report" : "Yearly Report");
        }
    }

    private void fetchAndDisplayData() {
        // Set period header
        String periodText = isMonthlyReport ? 
            new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(getDate(month, year)) :
            String.valueOf(year);
        tvPeriodHeader.setText(periodText);

        // Create date range for query
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        
        if (isMonthlyReport) {
            startCal.set(year, month, 1, 0, 0, 0);
            endCal.set(year, month, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        } else {
            startCal.set(year, 0, 1, 0, 0, 0);
            endCal.set(year, 11, 31, 23, 59, 59);
        }

        // Query Firestore
        db.collection("daily_data")
            .whereGreaterThanOrEqualTo("date", startCal.getTimeInMillis())
            .whereLessThanOrEqualTo("date", endCal.getTimeInMillis())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int workingDays = 0;
                int students15 = 0, students68 = 0, students910 = 0;
                int totalMilk = 0, totalRice = 0, totalWheat = 0, totalDhal = 0, totalOil = 0, totalSalt = 0;

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    if (document.getBoolean("isWorkingDay")) {
                        workingDays++;
                        
                        // Sum up student attendance
                        students15 += document.getLong("attendance1to5").intValue();
                        students68 += document.getLong("attendance6to8").intValue();
                        students910 += document.getLong("attendance9to10").intValue();

                        // Sum up inventory usage
                        totalMilk += sumInventoryItem(document, "milk");
                        totalRice += sumInventoryItem(document, "rice");
                        totalWheat += sumInventoryItem(document, "wheat");
                        totalDhal += sumInventoryItem(document, "dhal");
                        totalOil += sumInventoryItem(document, "oil");
                        totalSalt += sumInventoryItem(document, "salt");
                    }
                }

                // Display results
                tvWorkingDays.setText(String.valueOf(workingDays));
                tvStudents15.setText(String.valueOf(students15));
                tvStudents68.setText(String.valueOf(students68));
                tvStudents910.setText(String.valueOf(students910));

                tvTotalMilk.setText(String.format(Locale.getDefault(), "%d ml", totalMilk));
                tvTotalRice.setText(String.format(Locale.getDefault(), "%d g", totalRice));
                tvTotalWheat.setText(String.format(Locale.getDefault(), "%d g", totalWheat));
                tvTotalDhal.setText(String.format(Locale.getDefault(), "%d g", totalDhal));
                tvTotalOil.setText(String.format(Locale.getDefault(), "%d ml", totalOil));
                tvTotalSalt.setText(String.format(Locale.getDefault(), "%d g", totalSalt));
            })
            .addOnFailureListener(e -> showError("Error fetching data: " + e.getMessage()));
    }

    private int sumInventoryItem(QueryDocumentSnapshot doc, String item) {
        return doc.getLong(item + "15").intValue() +
               doc.getLong(item + "68").intValue() +
               doc.getLong(item + "910").intValue();
    }

    private Date getDate(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        return cal.getTime();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 