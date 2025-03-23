package com.example.dailydasoha;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private android.app.ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        tvSignUp.setOnClickListener(v -> 
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        tvForgotPassword.setOnClickListener(v -> 
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> {
            if (mGoogleSignInClient != null) {
                signInWithGoogle();
            } else {
                showError("Google Sign-In not initialized");
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }

        showProgressDialog("Signing in...");
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                hideProgressDialog();
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showError("Failed to get user data");
                    }
                } else {
                    showError("Authentication failed: " + task.getException().getMessage());
                }
            });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    showError("No Google account selected");
                }
            } catch (ApiException e) {
                showError("Google sign in failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            showError("Invalid Google token");
            return;
        }

        showProgressDialog("Signing in with Google...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                hideProgressDialog();
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showError("Failed to get user data");
                    }
                } else {
                    showError("Authentication failed: " + task.getException().getMessage());
                }
            });
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}