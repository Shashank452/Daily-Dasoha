package com.example.dailydasoha;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InventoryActivity extends BaseActivity {
    // Class 1-5 fields
    private EditText etRice15, etWheat15, etMilk15, etDhal15, etOil15, etSalt15;
    // Class 6-8 fields
    private EditText etRice68, etWheat68, etMilk68, etDhal68, etOil68, etSalt68;
    // Class 9-10 fields
    private EditText etRice910, etWheat910, etMilk910, etDhal910, etOil910, etSalt910;
    
    private TextView tvLastUpdated;
    private Button btnUpdate;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupClickListeners();
        loadInventoryData();
    }

    private void initializeViews() {
        // Class 1-5
        etRice15 = findViewById(R.id.etRice15);
        etWheat15 = findViewById(R.id.etWheat15);
        etMilk15 = findViewById(R.id.etMilk15);
        etDhal15 = findViewById(R.id.etDhal15);
        etOil15 = findViewById(R.id.etOil15);
        etSalt15 = findViewById(R.id.etSalt15);

        // Class 6-8
        etRice68 = findViewById(R.id.etRice68);
        etWheat68 = findViewById(R.id.etWheat68);
        etMilk68 = findViewById(R.id.etMilk68);
        etDhal68 = findViewById(R.id.etDhal68);
        etOil68 = findViewById(R.id.etOil68);
        etSalt68 = findViewById(R.id.etSalt68);

        // Class 9-10
        etRice910 = findViewById(R.id.etRice910);
        etWheat910 = findViewById(R.id.etWheat910);
        etMilk910 = findViewById(R.id.etMilk910);
        etDhal910 = findViewById(R.id.etDhal910);
        etOil910 = findViewById(R.id.etOil910);
        etSalt910 = findViewById(R.id.etSalt910);

        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnUpdate.setOnClickListener(v -> updateInventory());
    }

    private void loadInventoryData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("inventory")
                .document("current")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Class 1-5
                            etRice15.setText(String.valueOf(document.getLong("rice15")));
                            etWheat15.setText(String.valueOf(document.getLong("wheat15")));
                            etMilk15.setText(String.valueOf(document.getLong("milk15")));
                            etDhal15.setText(String.valueOf(document.getLong("dhal15")));
                            etOil15.setText(String.valueOf(document.getLong("oil15")));
                            etSalt15.setText(String.valueOf(document.getLong("salt15")));

                            // Class 6-8
                            etRice68.setText(String.valueOf(document.getLong("rice68")));
                            etWheat68.setText(String.valueOf(document.getLong("wheat68")));
                            etMilk68.setText(String.valueOf(document.getLong("milk68")));
                            etDhal68.setText(String.valueOf(document.getLong("dhal68")));
                            etOil68.setText(String.valueOf(document.getLong("oil68")));
                            etSalt68.setText(String.valueOf(document.getLong("salt68")));

                            // Class 9-10
                            etRice910.setText(String.valueOf(document.getLong("rice910")));
                            etWheat910.setText(String.valueOf(document.getLong("wheat910")));
                            etMilk910.setText(String.valueOf(document.getLong("milk910")));
                            etDhal910.setText(String.valueOf(document.getLong("dhal910")));
                            etOil910.setText(String.valueOf(document.getLong("oil910")));
                            etSalt910.setText(String.valueOf(document.getLong("salt910")));
                            
                            Long timestamp = document.getLong("lastUpdated");
                            if (timestamp != null) {
                                String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                                        .format(new java.util.Date(timestamp));
                                tvLastUpdated.setText("Last Updated: " + date);
                            }
                        }
                    } else {
                        showError("Error loading inventory: " + 
                            (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                });
    }

    private void updateInventory() {
        if (!validateInputs()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> inventory = new HashMap<>();
        
        // Class 1-5
        inventory.put("rice15", Long.parseLong(etRice15.getText().toString()));
        inventory.put("wheat15", Long.parseLong(etWheat15.getText().toString()));
        inventory.put("milk15", Long.parseLong(etMilk15.getText().toString()));
        inventory.put("dhal15", Long.parseLong(etDhal15.getText().toString()));
        inventory.put("oil15", Long.parseLong(etOil15.getText().toString()));
        inventory.put("salt15", Long.parseLong(etSalt15.getText().toString()));

        // Class 6-8
        inventory.put("rice68", Long.parseLong(etRice68.getText().toString()));
        inventory.put("wheat68", Long.parseLong(etWheat68.getText().toString()));
        inventory.put("milk68", Long.parseLong(etMilk68.getText().toString()));
        inventory.put("dhal68", Long.parseLong(etDhal68.getText().toString()));
        inventory.put("oil68", Long.parseLong(etOil68.getText().toString()));
        inventory.put("salt68", Long.parseLong(etSalt68.getText().toString()));

        // Class 9-10
        inventory.put("rice910", Long.parseLong(etRice910.getText().toString()));
        inventory.put("wheat910", Long.parseLong(etWheat910.getText().toString()));
        inventory.put("milk910", Long.parseLong(etMilk910.getText().toString()));
        inventory.put("dhal910", Long.parseLong(etDhal910.getText().toString()));
        inventory.put("oil910", Long.parseLong(etOil910.getText().toString()));
        inventory.put("salt910", Long.parseLong(etSalt910.getText().toString()));

        inventory.put("lastUpdated", System.currentTimeMillis());

        db.collection("inventory")
                .document("current")
                .set(inventory)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        showToast("Inventory updated successfully");
                        loadInventoryData(); // Reload to show updated timestamp
                    } else {
                        showError("Error updating inventory: " + task.getException().getMessage());
                    }
                });
    }

    private boolean validateInputs() {
        EditText[][] allFields = {
            {etRice15, etWheat15, etMilk15, etDhal15, etOil15, etSalt15},
            {etRice68, etWheat68, etMilk68, etDhal68, etOil68, etSalt68},
            {etRice910, etWheat910, etMilk910, etDhal910, etOil910, etSalt910}
        };

        for (EditText[] fields : allFields) {
            for (EditText field : fields) {
                if (field.getText().toString().isEmpty()) {
                    showToast("Please fill all fields");
                    return false;
                }
                try {
                    Long.parseLong(field.getText().toString());
                } catch (NumberFormatException e) {
                    showToast("Please enter valid numbers");
                    return false;
                }
            }
        }
        return true;
    }
} 