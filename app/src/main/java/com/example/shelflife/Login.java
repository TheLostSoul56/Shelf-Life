package com.example.shelflife;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button loginButton, buttonPromptChangePassword;
    private ProgressBar progressBar;
    private TextView registerRedirect, forgotPasswordText;
    private FirebaseAuth auth;

    private int failedLoginAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.ProgressBar);
        registerRedirect = findViewById(R.id.loginRegisterNow);
        forgotPasswordText = findViewById(R.id.textForgotPassword);
        buttonPromptChangePassword = findViewById(R.id.buttonPromptChangePassword);

        registerRedirect.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Registration.class));
            finish();
        });

        forgotPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, ForgotPasswordActivity.class));
        });

        buttonPromptChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                failedLoginAttempts = 0;
                FirebaseUser user = auth.getCurrentUser();
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                failedLoginAttempts++;
                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                if (failedLoginAttempts >= 3) {
                    buttonPromptChangePassword.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
