package com.example.dailydasoha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.print.PrintManager;
import android.content.Context;
import android.print.PrintDocumentAdapter;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.Set;
import java.util.HashSet;
import com.example.dailydasoha.adapters.PrintAdapter;

public class LedgerActivity extends BaseActivity {
    private TableLayout tableLayout;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ledger");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        initializeViews();

        db.collection("daily_data")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        List<QueryDocumentSnapshot> documents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            documents.add(document);
                        }

                        // Sort documents by date
                        Collections.sort(documents, (doc1, doc2) -> {
                            Calendar cal1 = Calendar.getInstance();
                            Calendar cal2 = Calendar.getInstance();
                            cal1.setTimeInMillis(doc1.getLong("date"));
                            cal2.setTimeInMillis(doc2.getLong("date"));

                            // Compare year first
                            int yearCompare = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
                            if (yearCompare != 0) return yearCompare;

                            // If same year, compare month
                            int monthCompare = cal1.get(Calendar.MONTH) - cal2.get(Calendar.MONTH);
                            if (monthCompare != 0) return monthCompare;

                            // If same month, compare day
                            return cal1.get(Calendar.DAY_OF_MONTH) - cal2.get(Calendar.DAY_OF_MONTH);
                        });

                        // Add rows in sorted order with alternating backgrounds
                        boolean isAlternate = false;
                        for (QueryDocumentSnapshot document : documents) {
                            addDataRow(document, isAlternate);
                            isAlternate = !isAlternate; // Toggle for next row
                        }
                    } else {
                        showError("Error loading data: " + task.getException().getMessage());
                    }
                });

        // Add print button click listener
        findViewById(R.id.btnPrint).setOnClickListener(v -> printLedger());
    }

    private void initializeViews() {
        tableLayout = findViewById(R.id.tableLayout);
        progressBar = findViewById(R.id.progressBar);
        
        // Add header row
        TableRow headerRow = new TableRow(this);
        
        // Basic Info
        headerRow.addView(createHeaderTextView("Date"));
        headerRow.addView(createHeaderTextView("Working\nDay"));
        
        // Student Counts
        headerRow.addView(createHeaderTextView("Students\n(1-5)"));
        headerRow.addView(createHeaderTextView("Students\n(6-8)"));
        headerRow.addView(createHeaderTextView("Students\n(9-10)"));

        // Class 1-5 Inventory
        headerRow.addView(createHeaderTextView("Milk\n(1-5)"));
        headerRow.addView(createHeaderTextView("Rice\n(1-5)"));
        headerRow.addView(createHeaderTextView("Wheat\n(1-5)"));
        headerRow.addView(createHeaderTextView("Dhal\n(1-5)"));
        headerRow.addView(createHeaderTextView("Oil\n(1-5)"));
        headerRow.addView(createHeaderTextView("Salt\n(1-5)"));

        // Class 6-8 Inventory
        headerRow.addView(createHeaderTextView("Milk\n(6-8)"));
        headerRow.addView(createHeaderTextView("Rice\n(6-8)"));
        headerRow.addView(createHeaderTextView("Wheat\n(6-8)"));
        headerRow.addView(createHeaderTextView("Dhal\n(6-8)"));
        headerRow.addView(createHeaderTextView("Oil\n(6-8)"));
        headerRow.addView(createHeaderTextView("Salt\n(6-8)"));

        // Class 9-10 Inventory
        headerRow.addView(createHeaderTextView("Milk\n(9-10)"));
        headerRow.addView(createHeaderTextView("Rice\n(9-10)"));
        headerRow.addView(createHeaderTextView("Wheat\n(9-10)"));
        headerRow.addView(createHeaderTextView("Dhal\n(9-10)"));
        headerRow.addView(createHeaderTextView("Oil\n(9-10)"));
        headerRow.addView(createHeaderTextView("Salt\n(9-10)"));

        tableLayout.addView(headerRow);
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        
        // Create layout params with center alignment
        TableRow.LayoutParams params = new TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);  // Add margins for spacing
        textView.setLayoutParams(params);
        
        // Apply header style
        textView.setTextAppearance(R.style.TableHeaderStyle);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        
        // Set minimum width based on column type
        if (text.startsWith("Date")) {
            textView.setMinWidth(dpToPx(90));
        } else if (text.startsWith("Working")) {
            textView.setMinWidth(dpToPx(60));
        } else {
            textView.setMinWidth(dpToPx(50));
        }
        
        // Add padding for better spacing
        textView.setPadding(dpToPx(4), dpToPx(8), dpToPx(4), dpToPx(8));
        
        return textView;
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private TextView createDataTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return textView;
    }

    private String formatDateWithDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        
        // Format to ensure consistent display with leading zeros
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    private void addDataRow(QueryDocumentSnapshot document, boolean isAlternate) {
        View rowView = getLayoutInflater().inflate(R.layout.ledger_row_item, null);
        
        // Apply alternate row background if needed
        if (isAlternate) {
            rowView.setBackgroundColor(getResources().getColor(R.color.table_row_alt_bg));
        }

        // Format date
        TextView tvDate = rowView.findViewById(R.id.tvDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(document.getLong("date"))));

        // Format working day
        TextView tvWorkingDay = rowView.findViewById(R.id.tvWorkingDay);
        boolean isWorkingDay = document.getBoolean("isWorkingDay");
        tvWorkingDay.setText(isWorkingDay ? "✓" : "✗");
        tvWorkingDay.setTextColor(getResources().getColor(
            isWorkingDay ? R.color.working_day_yes : R.color.working_day_no));

        // Set student counts
        setNumberText(rowView, R.id.tvClass1to5, document.getLong("attendance1to5"));
        setNumberText(rowView, R.id.tvClass6to8, document.getLong("attendance6to8"));
        setNumberText(rowView, R.id.tvClass9to10, document.getLong("attendance9to10"));

        // Class 1-5 Inventory
        setInventoryText(rowView, R.id.tvMilk15, document.getLong("milk15"), "g");
        setInventoryText(rowView, R.id.tvRice15, document.getLong("rice15"), "g");
        setInventoryText(rowView, R.id.tvWheat15, document.getLong("wheat15"), "g");
        setInventoryText(rowView, R.id.tvDhal15, document.getLong("dhal15"), "g");
        setInventoryText(rowView, R.id.tvOil15, document.getLong("oil15"), "g");
        setInventoryText(rowView, R.id.tvSalt15, document.getLong("salt15"), "g");

        // Class 6-8 Inventory
        setInventoryText(rowView, R.id.tvMilk68, document.getLong("milk68"), "g");
        setInventoryText(rowView, R.id.tvRice68, document.getLong("rice68"), "g");
        setInventoryText(rowView, R.id.tvWheat68, document.getLong("wheat68"), "g");
        setInventoryText(rowView, R.id.tvDhal68, document.getLong("dhal68"), "g");
        setInventoryText(rowView, R.id.tvOil68, document.getLong("oil68"), "g");
        setInventoryText(rowView, R.id.tvSalt68, document.getLong("salt68"), "g");

        // Class 9-10 Inventory
        setInventoryText(rowView, R.id.tvMilk910, document.getLong("milk910"), "g");
        setInventoryText(rowView, R.id.tvRice910, document.getLong("rice910"), "g");
        setInventoryText(rowView, R.id.tvWheat910, document.getLong("wheat910"), "g");
        setInventoryText(rowView, R.id.tvDhal910, document.getLong("dhal910"), "g");
        setInventoryText(rowView, R.id.tvOil910, document.getLong("oil910"), "g");
        setInventoryText(rowView, R.id.tvSalt910, document.getLong("salt910"), "g");

        // Add options menu
        ImageButton btnOptions = rowView.findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(v -> showOptionsMenu(v, document));

        tableLayout.addView(rowView);
    }

    private void setNumberText(View row, int viewId, Long value) {
        TextView textView = row.findViewById(viewId);
        textView.setText(String.valueOf(value != null ? value : 0));
    }

    private void setInventoryText(View row, int viewId, Long value, String unit) {
        TextView textView = row.findViewById(viewId);
        if (value != null && value > 0) {
            textView.setText(String.format(Locale.getDefault(), "%d%s", value, unit));
        } else {
            textView.setText("0" + unit);
        }
    }

    private void showOptionsMenu(View view, QueryDocumentSnapshot document) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.ledger_item_menu);
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit) {
                editEntry(document);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                confirmDelete(document);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void editEntry(QueryDocumentSnapshot document) {
        Intent intent = new Intent(this, DataEntryActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("DOCUMENT_ID", document.getId());
        intent.putExtra("DATE", document.getLong("date"));
        intent.putExtra("IS_WORKING_DAY", document.getBoolean("isWorkingDay"));
        intent.putExtra("ATTENDANCE_1_5", document.getLong("attendance1to5"));
        intent.putExtra("ATTENDANCE_6_8", document.getLong("attendance6to8"));
        intent.putExtra("ATTENDANCE_9_10", document.getLong("attendance9to10"));
        startActivity(intent);
    }

    private void confirmDelete(QueryDocumentSnapshot document) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Delete", (dialog, which) -> deleteEntry(document))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteEntry(QueryDocumentSnapshot document) {
        db.collection("daily_data")
            .document(document.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                showToast("Entry deleted successfully");
                recreate(); // Reload the activity to refresh the data
            })
            .addOnFailureListener(e -> showError("Error deleting entry: " + e.getMessage()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu resource
        getMenuInflater().inflate(R.menu.ledger_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_calendar) {
            showCalendarDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCalendarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_calendar_view, null);
        CalendarView calendarView = view.findViewById(R.id.calendarView);

        // Get all dates from Firestore
        db.collection("daily_data")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Create map to store document IDs by date
                Map<Long, String> dateToDocumentId = new HashMap<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Long date = document.getLong("date");
                    if (date != null) {
                        // Normalize to start of day
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(date);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        dateToDocumentId.put(cal.getTimeInMillis(), document.getId());

                        // Color the date in calendar
                        View dayView = calendarView.getChildAt(0);
                        if (dayView instanceof ViewGroup) {
                            ViewGroup viewGroup = (ViewGroup) dayView;
                            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                                View child = viewGroup.getChildAt(i);
                                if (child instanceof TextView) {
                                    TextView textView = (TextView) child;
                                    try {
                                        Calendar textCal = Calendar.getInstance();
                                        textCal.setTimeInMillis(date);
                                        if (textCal.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(textView.getText().toString())) {
                                            textView.setBackgroundColor(getResources().getColor(R.color.data_exists));
                                            textView.setTextColor(Color.WHITE);
                                        }
                                    } catch (NumberFormatException e) {
                                        // Skip non-numeric text
                                    }
                                }
                            }
                        }
                    }
                }

                // Set date change listener
                calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedCal.set(Calendar.MILLISECOND, 0);
                    long selectedDate = selectedCal.getTimeInMillis();

                    String documentId = dateToDocumentId.get(selectedDate);
                    if (documentId != null) {
                        // Show options for existing data
                        new AlertDialog.Builder(this)
                            .setTitle("Data Found")
                            .setMessage("What would you like to do with this entry?")
                            .setPositiveButton("View/Edit", (dialog, which) -> {
                                // Get the document and open in edit mode
                                db.collection("daily_data").document(documentId)
                                    .get()
                                    .addOnSuccessListener(document -> {
                                        Intent intent = new Intent(this, DataEntryActivity.class);
                                        intent.putExtra("EDIT_MODE", true);
                                        intent.putExtra("DOCUMENT_ID", documentId);
                                        intent.putExtra("DATE", document.getLong("date"));
                                        intent.putExtra("IS_WORKING_DAY", document.getBoolean("isWorkingDay"));
                                        intent.putExtra("ATTENDANCE_1_5", document.getLong("attendance1to5"));
                                        intent.putExtra("ATTENDANCE_6_8", document.getLong("attendance6to8"));
                                        intent.putExtra("ATTENDANCE_9_10", document.getLong("attendance9to10"));
                                        startActivity(intent);
                                    });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    } else {
                        // Color the selected date red if no data exists
                        View dayView = view1.findViewById(android.R.id.content);
                        if (dayView != null) {
                            dayView.setBackgroundColor(getResources().getColor(R.color.no_data));
                        }
                        
                        // Offer to add new entry for this date
                        new AlertDialog.Builder(this)
                            .setTitle("No Data Found")
                            .setMessage("Would you like to add an entry for this date?")
                            .setPositiveButton("Add Entry", (dialog, which) -> {
                                Intent intent = new Intent(this, DataEntryActivity.class);
                                intent.putExtra("SELECTED_DATE", selectedDate);
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    }
                });
            });

        builder.setView(view)
               .setTitle("Data Entry Calendar")
               .setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void printLedger() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Ledger";

        PrintDocumentAdapter printAdapter = new PrintAdapter(this, db);
        printManager.print(jobName, printAdapter, null);
    }
} 