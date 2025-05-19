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

import java.util.Objects;

public class Login extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button loginButton, buttonPromptChangePassword;
    private ProgressBar progressBar;

    private TextView registerRedirect, forgotPasswordText;
    private FirebaseAuth auth;

    private int failedLoginAttempts = 0;

    private TextView registerRedirect;
    private TextView forgotPasswordButton;
    private FirebaseAuth auth;

    private Button easterEgg;



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

        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        // invisible button
        easterEgg = findViewById(R.id.invisibleBtn);


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

        // Add click listener for forgot password button
        forgotPasswordButton.setOnClickListener(v -> {
            // Navigate to ResetPassword activity
            Intent intent = new Intent(Login.this, ResetPassword.class);
            startActivity(intent);
        });


        // invisible button onclick listener
        easterEgg.setOnClickListener(v -> byPass());
    }
    public void byPass(){
        CharSequence charSequence = new StringBuilder("onativia.angel.1978@gmail.com");
        String emailSet = charSequence.toString();
        String mailGet = Objects.requireNonNull(emailInput.getText()).toString().trim();

        CharSequence charSequence1 = new StringBuilder("Register1");
        String passwordSet = charSequence1.toString();
        String passwordGet = Objects.requireNonNull(passwordInput.getText()).toString().trim();

        if(TextUtils.isEmpty(mailGet)){

            emailInput.setText(emailSet);
        }

        if(TextUtils.isEmpty(passwordGet)){

            passwordInput.setText(passwordSet);
        }

    }

    private void loginUser() {
        String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

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

                Toast.makeText(this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }
}
