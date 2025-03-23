package com.example.dailydasoha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends BaseActivity {
    private EditText etEmail, etPassword, etConfirmPassword, etName, etSchoolName;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etName = findViewById(R.id.etName);
        etSchoolName = findViewById(R.id.etSchoolName);
        btnSignUp = findViewById(R.id.btnSignUp);
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String schoolName = etSchoolName.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword, name, schoolName)) {
            return;
        }

        // Show progress
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        btnSignUp.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                btnSignUp.setEnabled(true);
                
                if (task.isSuccessful()) {
                    saveUserData(name, schoolName);
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorMessage = task.getException() != null ? 
                        task.getException().getMessage() : "Unknown error occurred";
                    showError("Sign up failed: " + errorMessage);
                }
            })
            .addOnFailureListener(e -> {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                btnSignUp.setEnabled(true);
                showError("Sign up failed: " + e.getMessage());
            });
    }

    private boolean validateInputs(String email, String password, 
                                 String confirmPassword, String name, String schoolName) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() 
            || name.isEmpty() || schoolName.isEmpty()) {
            showToast("Please fill all fields");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return false;
        }

        if (password.length() < 6) {
            showToast("Password should be at least 6 characters long");
            return false;
        }

        return true;
    }

    private void saveUserData(String name, String schoolName) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("schoolName", schoolName);
        user.put("email", mAuth.getCurrentUser().getEmail());
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
            .set(user)
            .addOnFailureListener(e -> 
                showError("Error saving user data: " + e.getMessage()));
    }
} 