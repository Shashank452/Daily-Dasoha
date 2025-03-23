package com.example.dailydasoha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.ProgressDialog;
import android.print.PrintManager;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.PopupMenu;
import androidx.appcompat.view.ContextThemeWrapper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import com.example.dailydasoha.models.DailyData;
import com.example.dailydasoha.utils.ExportUtil;
import com.example.dailydasoha.utils.PrintAdapter;
import com.example.dailydasoha.dialogs.MonthPickerDialog;
import com.example.dailydasoha.dialogs.YearPickerDialog;

public class MainActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Dasoha");
        }

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Using CardViews instead of Buttons
        findViewById(R.id.cardEnterData).setOnClickListener(v -> {
            startActivity(new Intent(this, DataEntryActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        findViewById(R.id.cardViewLedger).setOnClickListener(v -> {
            startActivity(new Intent(this, LedgerActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        findViewById(R.id.cardAnalytics).setOnClickListener(v -> {
            // Show analytics options dialog
            showAnalyticsOptionsDialog();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        findViewById(R.id.cardInventory).setOnClickListener(v -> {
            startActivity(new Intent(this, InventoryActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Add logout button click listener
        findViewById(R.id.fabLogout).setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showAnalyticsOptionsDialog() {
        String[] options = {
            "Generate Monthly Report",
            "Generate Custom Report"
        };

        new AlertDialog.Builder(this)
            .setTitle("Analytics Options")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Monthly Report
                        startActivity(new Intent(this, MonthlyReportActivity.class));
                        break;
                    case 1: // Custom Report
                        startActivity(new Intent(this, CustomReportActivity.class));
                        break;
                }
            })
            .show();
    }

    private void printReport() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Report";
        PrintAdapter printAdapter = new PrintAdapter(this, db);
        printManager.print(jobName, printAdapter, null);
    }

    @Override
    protected void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void showAnalysisOptions() {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.cardAnalytics));
        popup.getMenuInflater().inflate(R.menu.analysis_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_monthly_report) {
                startActivity(new Intent(this, MonthlyReportActivity.class));
                return true;
            } else if (itemId == R.id.menu_custom_report) {
                startActivity(new Intent(this, CustomReportActivity.class));
                return true;
            }
            return false;
        });
        
        popup.show();
    }
}