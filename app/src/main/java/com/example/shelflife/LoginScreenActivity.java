package com.example.shelflife;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginScreenActivity extends AppCompatActivity {

    private EditText mEmailInput, mPasswordInput;
    private Button mLoginButton, mRegisterButton, mForgotPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Link UI elements
        mEmailInput = findViewById(R.id.emailInput);
        mPasswordInput = findViewById(R.id.passwordInput);
        mLoginButton = findViewById(R.id.loginButton);
        mRegisterButton = findViewById(R.id.registerButton);
        mForgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        // Login button test mode — always goes to MainActivity
        mLoginButton.setOnClickListener(v -> {
            Toast.makeText(this, "Login clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Register button test
        mRegisterButton.setOnClickListener(v -> {
            Toast.makeText(this, "Register button clicked", Toast.LENGTH_SHORT).show();
            // TODO: Add RegisterActivity navigation here later
        });

        // Forgot password button test
        mForgotPasswordButton.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
            // TODO: Add ForgotPasswordActivity navigation here later
        });
    }
}
