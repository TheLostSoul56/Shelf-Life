package com.example.shelflife;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editEmail;
    private Button buttonSendEmail;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editEmail = findViewById(R.id.editEmail);
        buttonSendEmail = findViewById(R.id.buttonSendEmail);
        firebaseAuth = FirebaseAuth.getInstance();

        buttonSendEmail.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                editEmail.setError("Email is required");
                return;
            }

            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
