package com.example.shelflife;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    private static final String TAG = "Registration";

    private TextInputEditText firstNameInput, lastNameInput, phoneInput, emailInput, passwordInput;
    private Button registerButton;
    private ProgressBar progressBar;
    private TextView loginRedirect;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        firstNameInput = findViewById(R.id.register_fname);
        lastNameInput = findViewById(R.id.register_lname);
        phoneInput = findViewById(R.id.register_phone);
        emailInput = findViewById(R.id.register_email);
        passwordInput = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.ProgressBar);
        loginRedirect = findViewById(R.id.loginNow);

        loginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(Registration.this, Login.class));
            finish();
        });

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fname = firstNameInput.getText().toString().trim();
        String lname = lastNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(fname)) {
            firstNameInput.setError("First name is required");
            return;
        }
        if (TextUtils.isEmpty(lname)) {
            lastNameInput.setError("Last name is required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("Fname", fname);
                    userMap.put("Lname", lname);
                    userMap.put("phone", phone);
                    userMap.put("email", email);

                    db.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to save user: ", e));
                }

                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Registration.this, Login.class));
                finish();
            } else {
                Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
