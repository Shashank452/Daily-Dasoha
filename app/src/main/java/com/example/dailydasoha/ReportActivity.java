package com.example.dailydasoha;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dailydasoha.models.DailyData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private ArrayList<DailyData> reportData;
    private int month;
    private int year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Report");
        }

        // Get data from intent
        reportData = (ArrayList<DailyData>) getIntent().getSerializableExtra("REPORT_DATA");
        month = getIntent().getIntExtra("MONTH", -1);
        year = getIntent().getIntExtra("YEAR", -1);

        // Update title based on report type
        String title = month >= 0 ? 
            String.format(Locale.getDefault(), "Report - %s %d", 
                new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date(0, month, 1)), year) :
            String.format(Locale.getDefault(), "Yearly Report - %d", year);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        tableLayout = findViewById(R.id.tableLayout);
        setupTable();
    }

    private void setupTable() {
        // Add header row
        addHeaderRow();

        // Add data rows
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        int totalAttendance = 0;
        int workingDays = 0;

        for (DailyData data : reportData) {
            TableRow row = (TableRow) LayoutInflater.from(this)
                .inflate(R.layout.report_table_row, tableLayout, false);

            ((TextView) row.findViewById(R.id.textDate))
                .setText(sdf.format(new Date(data.getDate())));
            ((TextView) row.findViewById(R.id.textWorkingDay))
                .setText(data.isWorkingDay() ? "Yes" : "No");
            ((TextView) row.findViewById(R.id.textClass1to5))
                .setText(String.valueOf(data.getAttendance1to5()));
            ((TextView) row.findViewById(R.id.textClass6to8))
                .setText(String.valueOf(data.getAttendance6to8()));
            ((TextView) row.findViewById(R.id.textClass9to10))
                .setText(String.valueOf(data.getAttendance9to10()));
            ((TextView) row.findViewById(R.id.textGrainType))
                .setText(data.getGrainType());
            ((TextView) row.findViewById(R.id.textTotal))
                .setText(String.valueOf(data.getTotalAttendance()));

            if (data.isWorkingDay()) {
                workingDays++;
                totalAttendance += data.getTotalAttendance();
            }

            tableLayout.addView(row);
        }

        // Add summary row
        addSummaryRow(workingDays, totalAttendance);
    }

    private void addHeaderRow() {
        TableRow headerRow = (TableRow) LayoutInflater.from(this)
            .inflate(R.layout.report_table_header, tableLayout, false);
        tableLayout.addView(headerRow);
    }

    private void addSummaryRow(int workingDays, int totalAttendance) {
        TableRow summaryRow = (TableRow) LayoutInflater.from(this)
            .inflate(R.layout.report_table_summary, tableLayout, false);

        ((TextView) summaryRow.findViewById(R.id.textWorkingDays))
            .setText(String.valueOf(workingDays));
        ((TextView) summaryRow.findViewById(R.id.textTotalAttendance))
            .setText(String.valueOf(totalAttendance));
        ((TextView) summaryRow.findViewById(R.id.textAverageAttendance))
            .setText(String.format(Locale.getDefault(), "%.1f", 
                workingDays > 0 ? (float) totalAttendance / workingDays : 0));

        tableLayout.addView(summaryRow);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 