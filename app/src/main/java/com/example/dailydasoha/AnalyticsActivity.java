package com.example.dailydasoha;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends BaseActivity {
    private BarChart attendanceChart;
    private PieChart grainChart;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupCharts();
        loadAnalyticsData();
    }

    private void initializeViews() {
        attendanceChart = findViewById(R.id.attendanceChart);
        grainChart = findViewById(R.id.grainChart);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupCharts() {
        // Setup Attendance Chart
        attendanceChart.getDescription().setEnabled(false);
        attendanceChart.setDrawGridBackground(false);
        XAxis xAxis = attendanceChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        attendanceChart.getAxisLeft().setDrawGridLines(false);
        attendanceChart.getAxisRight().setEnabled(false);
        attendanceChart.setFitBars(true);

        // Setup Grain Chart
        grainChart.getDescription().setEnabled(false);
        grainChart.setHoleRadius(35f);
        grainChart.setTransparentCircleRadius(40f);
        grainChart.setDrawHoleEnabled(true);
        grainChart.setRotationEnabled(true);
    }

    private void loadAnalyticsData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("daily_data")
                .orderBy("date")
                .limit(7)  // Last 7 days
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        List<BarEntry> attendanceEntries = new ArrayList<>();
                        List<String> dates = new ArrayList<>();
                        Map<String, Integer> grainCounts = new HashMap<>();

                        int index = 0;
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Attendance data
                            float total = document.getLong("attendance1to5") +
                                    document.getLong("attendance6to8") +
                                    document.getLong("attendance9to10");
                            attendanceEntries.add(new BarEntry(index, total));

                            // Date for X-axis
                            long timestamp = document.getLong("date");
                            dates.add(sdf.format(new Date(timestamp)));

                            // Grain type data
                            String grainType = document.getString("grainType");
                            grainCounts.put(grainType, grainCounts.getOrDefault(grainType, 0) + 1);

                            index++;
                        }

                        updateAttendanceChart(attendanceEntries, dates);
                        updateGrainChart(grainCounts);
                    } else {
                        showError("Error loading analytics data: " + task.getException().getMessage());
                    }
                });
    }

    private void updateAttendanceChart(List<BarEntry> entries, List<String> dates) {
        BarDataSet dataSet = new BarDataSet(entries, "Daily Attendance");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        
        attendanceChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        attendanceChart.setData(data);
        attendanceChart.invalidate();
    }

    private void updateGrainChart(Map<String, Integer> grainCounts) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : grainCounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Grain Types");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        grainChart.setData(data);
        grainChart.invalidate();
    }
} 