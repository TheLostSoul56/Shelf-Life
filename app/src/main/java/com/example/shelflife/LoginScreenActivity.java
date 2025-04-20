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
    private Button mLoginButton, mRegisterButton;

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

        mEmailInput = findViewById(R.id.emailInput);
        mPasswordInput = findViewById(R.id.passwordInput);
        mLoginButton = findViewById(R.id.loginButton);
        mRegisterButton = findViewById(R.id.registerButton);

        mLoginButton.setOnClickListener(v -> {
            String email = mEmailInput.getText().toString().trim();
            String password = mPasswordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        mRegisterButton.setOnClickListener(v -> {
            Toast.makeText(this, "Redirect to registration page (coming soon)", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, RegisterActivity.class)); // Future use
        });
    }
}
