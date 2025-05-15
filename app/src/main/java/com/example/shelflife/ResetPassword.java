package com.example.shelflife;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {
    // UI Components
    private TextInputEditText emailInput;
    private TextInputLayout emailLayout;
    private Button resetPasswordButton;
    private Button cancelButton;
    private TextView returnToLoginText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize UI elements
        initializeViews();

        // Check if email was passed from login screen
        String passedEmail = getIntent().getStringExtra("email");
        if (passedEmail != null && !passedEmail.isEmpty()) {
            emailInput.setText(passedEmail);
        }

        // Set up click listeners for all interactive elements
        setupClickListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        emailLayout = findViewById(R.id.emailLayout);
        resetPasswordButton = findViewById(R.id.Reset_Password);
        cancelButton = findViewById(R.id.cancel_Button);
        returnToLoginText = findViewById(R.id.returnToLoginText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Reset Password button click listener
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResetPassword();
            }
        });

        // Cancel button click listener
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simply finish the activity and go back
                finish();
            }
        });

        // Return to Login text click listener
        returnToLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to login screen
                finish();
            }
        });
    }

    private void handleResetPassword() {
        // Get email input
        String email = emailInput.getText().toString().trim();

        // Validate email
        if (!validateEmail(email)) {
            return; // Exit if validation fails
        }

        // Check network connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Use Firebase to send password reset email
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Reset loading state
                    setLoadingState(false);

                    if (task.isSuccessful()) {
                        // Show success dialog
                        showSuccessDialog(email);
                    } else {
                        // Show error message
                        Toast.makeText(ResetPassword.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showSuccessDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Email Sent")
                .setMessage("A password reset link has been sent to " + email + ". Please check your inbox and follow the instructions.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Return to login screen when user clicks OK
                    finish();
                })
                .setCancelable(false) // Prevent dismissing by tapping outside
                .show();
    }

    private boolean validateEmail(String email) {
        // Check if email is empty
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            return false;
        }

        // Check if email format is valid
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            return false;
        }

        // Clear any previous errors
        emailLayout.setError(null);
        return true;
    }

    private void setButtonsEnabled(boolean enabled) {
        resetPasswordButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        returnToLoginText.setEnabled(enabled);
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            resetPasswordButton.setText("Sending...");
            progressBar.setVisibility(View.VISIBLE);
            setButtonsEnabled(false);
        } else {
            resetPasswordButton.setText(R.string.Reset);
            progressBar.setVisibility(View.GONE);
            setButtonsEnabled(true);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
