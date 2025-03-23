package com.example.dailydasoha;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DataEntryActivity extends BaseActivity {
    private EditText etDate;
    private CheckBox cbWorkingDay;
    private EditText etClass1to5, etClass6to8, etClass9to10;
    private Spinner spinnerGrains;
    private Button btnSubmit, btnCancel;
    private FirebaseFirestore db;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormatter;
    private String documentId;

    // Standard amounts per student
    private static final double MILK_PER_STUDENT_15 = 18.0;   // ml for class 1-5
    private static final double RICE_PER_STUDENT_15 = 100.0;  // g for class 1-5
    private static final double WHEAT_PER_STUDENT_15 = 100.0; // g for class 1-5 (same as rice)
    private static final double DHAL_PER_STUDENT_15 = 20.0;   // g for class 1-5
    private static final double OIL_PER_STUDENT_15 = 5.0;     // ml for class 1-5
    private static final double SALT_PER_STUDENT_15 = 2.0;    // g for class 1-5

    private static final double MILK_PER_STUDENT_68 = 18.0;   // ml for class 6-8
    private static final double RICE_PER_STUDENT_68 = 150.0;  // g for class 6-8
    private static final double WHEAT_PER_STUDENT_68 = 150.0; // g for class 6-8 (same as rice)
    private static final double DHAL_PER_STUDENT_68 = 30.0;   // g for class 6-8
    private static final double OIL_PER_STUDENT_68 = 7.5;     // ml for class 6-8
    private static final double SALT_PER_STUDENT_68 = 4.0;    // g for class 6-8

    private static final double MILK_PER_STUDENT_910 = 18.0;   // ml for class 9-10
    private static final double RICE_PER_STUDENT_910 = 200.0;  // g for class 9-10
    private static final double WHEAT_PER_STUDENT_910 = 200.0; // g for class 9-10 (same as rice)
    private static final double DHAL_PER_STUDENT_910 = 40.0;   // g for class 9-10
    private static final double OIL_PER_STUDENT_910 = 10.0;    // ml for class 9-10
    private static final double SALT_PER_STUDENT_910 = 4.0;    // g for class 9-10

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_entry);

        db = FirebaseFirestore.getInstance();
        selectedDate = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        initializeViews();
        setupSpinner();
        setupClickListeners();
        
        // Check if editing existing entry
        documentId = getIntent().getStringExtra("DOCUMENT_ID");
        if (documentId != null) {
            loadExistingData();
        } else {
            updateDateDisplay(); // Set default date as today
        }

        // Check if in edit mode
        if (getIntent().getBooleanExtra("EDIT_MODE", false)) {
            setupEditMode();
        }
    }

    private void initializeViews() {
        etDate = findViewById(R.id.etDate);
        cbWorkingDay = findViewById(R.id.cbWorkingDay);
        etClass1to5 = findViewById(R.id.etClass1to5);
        etClass6to8 = findViewById(R.id.etClass6to8);
        etClass9to10 = findViewById(R.id.etClass9to10);
        spinnerGrains = findViewById(R.id.spinnerGrains);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        // Set initial state of attendance fields
        setAttendanceFieldsEnabled(cbWorkingDay.isChecked());

        // Add checkbox listener
        cbWorkingDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setAttendanceFieldsEnabled(isChecked);
            if (!isChecked) {
                // Clear attendance fields when unchecked
                etClass1to5.setText("0");
                etClass6to8.setText("0");
                etClass9to10.setText("0");
                // Set grain type to None
                spinnerGrains.setSelection(0);
            }
            // Enable/disable grain spinner based on working day
            spinnerGrains.setEnabled(isChecked);
        });
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.grains_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrains.setAdapter(adapter);
        
        // Set default selection to "None"
        spinnerGrains.setSelection(0);
    }

    private void setupClickListeners() {
        etDate.setOnClickListener(v -> showDatePicker());
        btnSubmit.setOnClickListener(v -> submitData());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // Check if date already exists before updating display
                checkDateExists();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void checkDateExists() {
        // Set time to start of day for consistent comparison
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);
        
        long startOfDay = selectedDate.getTimeInMillis();
        selectedDate.add(Calendar.DAY_OF_MONTH, 1);
        long startOfNextDay = selectedDate.getTimeInMillis();
        selectedDate.add(Calendar.DAY_OF_MONTH, -1); // Reset to selected date

        if (documentId == null) { // Only check if this is a new entry
            db.collection("daily_data")
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThan("date", startOfNextDay)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Date already exists
                        showError("An entry for this date already exists. Please choose a different date or edit the existing entry.");
                        // Reset to today's date
                        selectedDate.setTimeInMillis(System.currentTimeMillis());
                    } else {
                        // Date is available, update display
                        updateDateDisplay();
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Error checking date: " + e.getMessage());
                    selectedDate.setTimeInMillis(System.currentTimeMillis());
                    updateDateDisplay();
                });
        } else {
            // If editing, just update the display
            updateDateDisplay();
        }
    }

    private void updateDateDisplay() {
        etDate.setText(dateFormatter.format(selectedDate.getTime()));
    }

    private void loadExistingData() {
        long date = getIntent().getLongExtra("DATE", System.currentTimeMillis());
        selectedDate.setTimeInMillis(date);
        updateDateDisplay();
        
        boolean isWorkingDay = getIntent().getBooleanExtra("IS_WORKING_DAY", false);
        cbWorkingDay.setChecked(isWorkingDay);
        setAttendanceFieldsEnabled(isWorkingDay);  // Update fields state
        
        etClass1to5.setText(String.valueOf(getIntent().getLongExtra("ATTENDANCE_1_5", 0)));
        etClass6to8.setText(String.valueOf(getIntent().getLongExtra("ATTENDANCE_6_8", 0)));
        etClass9to10.setText(String.valueOf(getIntent().getLongExtra("ATTENDANCE_9_10", 0)));
        
        String grainType = getIntent().getStringExtra("GRAIN_TYPE");
        if (grainType != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinnerGrains.getAdapter();
            int position = adapter.getPosition(grainType);
            spinnerGrains.setSelection(position);
        }
    }

    private void setAttendanceFieldsEnabled(boolean enabled) {
        etClass1to5.setEnabled(enabled);
        etClass6to8.setEnabled(enabled);
        etClass9to10.setEnabled(enabled);
        spinnerGrains.setEnabled(enabled);

        if (!enabled) {
            // When disabled, set text to "0" instead of leaving empty
            etClass1to5.setText("0");
            etClass6to8.setText("0");
            etClass9to10.setText("0");
            spinnerGrains.setSelection(0); // Set to "None"
        }
        
        // Change background color to indicate disabled state
        int backgroundColor = enabled ? 
            android.R.color.white : android.R.color.darker_gray;
        etClass1to5.setBackgroundResource(backgroundColor);
        etClass6to8.setBackgroundResource(backgroundColor);
        etClass9to10.setBackgroundResource(backgroundColor);
    }

    private boolean validateInputs() {
        if (!cbWorkingDay.isChecked()) {
            // If not a working day, set attendance to 0 and allow submission
            etClass1to5.setText("0");
            etClass6to8.setText("0");
            etClass9to10.setText("0");
            return true;
        }

        // Only validate attendance fields if it's a working day
        if (etClass1to5.getText().toString().isEmpty() ||
            etClass6to8.getText().toString().isEmpty() ||
            etClass9to10.getText().toString().isEmpty()) {
            showToast("Please fill all attendance fields for working day");
            return false;
        }

        // Validate grain selection only for working days
        String selectedGrain = spinnerGrains.getSelectedItem().toString();
        if (selectedGrain.equals("None")) {
            showToast("Please select Rice or Wheat");
            return false;
        }

        try {
            Integer.parseInt(etClass1to5.getText().toString());
            Integer.parseInt(etClass6to8.getText().toString());
            Integer.parseInt(etClass9to10.getText().toString());
        } catch (NumberFormatException e) {
            showToast("Please enter valid numbers for attendance");
            return false;
        }

        return true;
    }

    private void submitData() {
        if (!validateInputs()) {
            return;
        }

        showProgressDialog("Submitting data...");

        // Get the selected date's timestamp for midnight (start of day)
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDate.getTimeInMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        // Set end of day
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();

        // Check if data already exists for this date
        db.collection("daily_data")
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThanOrEqualTo("date", endOfDay)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        hideProgressDialog();
                        // Show confirmation dialog if data exists
                        new AlertDialog.Builder(this)
                            .setTitle("Data Already Exists")
                            .setMessage("Data for this date already exists. Do you want to overwrite it?")
                            .setPositiveButton("Overwrite", (dialog, which) -> {
                                // Delete existing data and submit new data
                                String docId = task.getResult().getDocuments().get(0).getId();
                                updateExistingData(docId);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                // Do nothing, just dismiss
                            })
                            .show();
                    } else {
                        // No existing data, proceed with submission
                        submitNewData();
                    }
                } else {
                    hideProgressDialog();
                    showError("Error checking existing data: " + task.getException().getMessage());
                }
            });
    }

    private void updateExistingData(String documentId) {
        showProgressDialog("Updating data...");
        Map<String, Object> data = collectData();
        
        db.collection("daily_data")
            .document(documentId)
            .set(data)
            .addOnSuccessListener(aVoid -> {
                hideProgressDialog();
                showToast("Data updated successfully");
                finish();
            })
            .addOnFailureListener(e -> {
                hideProgressDialog();
                showError("Error updating data: " + e.getMessage());
            });
    }

    private void submitNewData() {
        Map<String, Object> data = collectData();
        
        db.collection("daily_data")
            .add(data)
            .addOnSuccessListener(documentReference -> {
                hideProgressDialog();
                showToast("Data submitted successfully");
                finish();
            })
            .addOnFailureListener(e -> {
                hideProgressDialog();
                showError("Error submitting data: " + e.getMessage());
            });
    }

    private Map<String, Object> collectData() {
        Map<String, Object> data = new HashMap<>();
        data.put("date", selectedDate.getTimeInMillis());
        data.put("isWorkingDay", cbWorkingDay.isChecked());
        
        // Get attendance data
        int attendance15 = Integer.parseInt(etClass1to5.getText().toString());
        int attendance68 = Integer.parseInt(etClass6to8.getText().toString());
        int attendance910 = Integer.parseInt(etClass9to10.getText().toString());
        
        data.put("attendance1to5", attendance15);
        data.put("attendance6to8", attendance68);
        data.put("attendance9to10", attendance910);

        // Get selected grain type
        String selectedGrain = spinnerGrains.getSelectedItem().toString();

        // Calculate and store inventory data based on attendance
        if (cbWorkingDay.isChecked()) {
            // Class 1-5
            data.put("milk15", (int)(attendance15 * MILK_PER_STUDENT_15));
            // Set rice or wheat based on selection
            if (selectedGrain.equals("Rice")) {
                data.put("rice15", (int)(attendance15 * RICE_PER_STUDENT_15));
                data.put("wheat15", 0);
            } else if (selectedGrain.equals("Wheat")) {
                data.put("rice15", 0);
                data.put("wheat15", (int)(attendance15 * WHEAT_PER_STUDENT_15));
            } else {
                data.put("rice15", 0);
                data.put("wheat15", 0);
            }
            data.put("dhal15", (int)(attendance15 * DHAL_PER_STUDENT_15));
            data.put("oil15", (int)(attendance15 * OIL_PER_STUDENT_15));
            data.put("salt15", (int)(attendance15 * SALT_PER_STUDENT_15));

            // Class 6-8
            data.put("milk68", (int)(attendance68 * MILK_PER_STUDENT_68));
            // Set rice or wheat based on selection
            if (selectedGrain.equals("Rice")) {
                data.put("rice68", (int)(attendance68 * RICE_PER_STUDENT_68));
                data.put("wheat68", 0);
            } else if (selectedGrain.equals("Wheat")) {
                data.put("rice68", 0);
                data.put("wheat68", (int)(attendance68 * WHEAT_PER_STUDENT_68));
            } else {
                data.put("rice68", 0);
                data.put("wheat68", 0);
            }
            data.put("dhal68", (int)(attendance68 * DHAL_PER_STUDENT_68));
            data.put("oil68", (int)(attendance68 * OIL_PER_STUDENT_68));
            data.put("salt68", (int)(attendance68 * SALT_PER_STUDENT_68));

            // Class 9-10
            data.put("milk910", (int)(attendance910 * MILK_PER_STUDENT_910));
            // Set rice or wheat based on selection
            if (selectedGrain.equals("Rice")) {
                data.put("rice910", (int)(attendance910 * RICE_PER_STUDENT_910));
                data.put("wheat910", 0);
            } else if (selectedGrain.equals("Wheat")) {
                data.put("rice910", 0);
                data.put("wheat910", (int)(attendance910 * WHEAT_PER_STUDENT_910));
            } else {
                data.put("rice910", 0);
                data.put("wheat910", 0);
            }
            data.put("dhal910", (int)(attendance910 * DHAL_PER_STUDENT_910));
            data.put("oil910", (int)(attendance910 * OIL_PER_STUDENT_910));
            data.put("salt910", (int)(attendance910 * SALT_PER_STUDENT_910));
        } else {
            // Set all inventory values to 0 for non-working days
            data.put("milk15", 0);
            data.put("rice15", 0);
            data.put("wheat15", 0);
            data.put("dhal15", 0);
            data.put("oil15", 0);
            data.put("salt15", 0);

            data.put("milk68", 0);
            data.put("rice68", 0);
            data.put("wheat68", 0);
            data.put("dhal68", 0);
            data.put("oil68", 0);
            data.put("salt68", 0);

            data.put("milk910", 0);
            data.put("rice910", 0);
            data.put("wheat910", 0);
            data.put("dhal910", 0);
            data.put("oil910", 0);
            data.put("salt910", 0);
        }

        return data;
    }

    private void setupEditMode() {
        // Set title
        setTitle("Edit Entry");

        // Get data from intent
        long date = getIntent().getLongExtra("DATE", 0);
        boolean isWorkingDay = getIntent().getBooleanExtra("IS_WORKING_DAY", true);
        long attendance15 = getIntent().getLongExtra("ATTENDANCE_1_5", 0);
        long attendance68 = getIntent().getLongExtra("ATTENDANCE_6_8", 0);
        long attendance910 = getIntent().getLongExtra("ATTENDANCE_9_10", 0);

        // Set values to views
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        etDate.setText(dateFormatter.format(cal.getTime()));
        cbWorkingDay.setChecked(isWorkingDay);
        etClass1to5.setText(String.valueOf(attendance15));
        etClass6to8.setText(String.valueOf(attendance68));
        etClass9to10.setText(String.valueOf(attendance910));

        // Disable date editing in edit mode
        etDate.setEnabled(false);
    }
} 