package com.example.dailydasoha;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceTrendsActivity extends AppCompatActivity {
    private LineChart chart;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_trends);

        db = FirebaseFirestore.getInstance();
        chart = findViewById(R.id.chart);
        progressBar = findViewById(R.id.progressBar);

        setupChart();
        loadData();
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long millis = (long) value;
                return mFormat.format(new Date(millis));
            }
        });
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("daily_data")
            .orderBy("date")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Entry> class1to5Entries = new ArrayList<>();
                List<Entry> class6to8Entries = new ArrayList<>();
                List<Entry> class9to10Entries = new ArrayList<>();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    long date = document.getLong("date");
                    float x = date;
                    
                    class1to5Entries.add(new Entry(x, document.getLong("attendance1to5")));
                    class6to8Entries.add(new Entry(x, document.getLong("attendance6to8")));
                    class9to10Entries.add(new Entry(x, document.getLong("attendance9to10")));
                }

                LineDataSet set1 = createDataSet(class1to5Entries, "Class 1-5", Color.RED);
                LineDataSet set2 = createDataSet(class6to8Entries, "Class 6-8", Color.GREEN);
                LineDataSet set3 = createDataSet(class9to10Entries, "Class 9-10", Color.BLUE);

                LineData lineData = new LineData(set1, set2, set3);
                chart.setData(lineData);
                chart.invalidate();

                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                // Handle error
            });
    }

    private LineDataSet createDataSet(List<Entry> entries, String label, int color) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setLineWidth(2f);
        set.setCircleColor(color);
        set.setCircleRadius(3f);
        set.setDrawCircleHole(false);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setFillAlpha(65);
        set.setFillColor(color);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }
} 