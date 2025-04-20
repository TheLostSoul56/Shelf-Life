package com.example.shelflife;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterNewAccountActivity extends AppCompatActivity {

    private EditText mEmailInput, mPasswordInput, mReenterPasswordInput,
            mFirstNameInput, mLastNameInput, mPhoneNumberInput, mCityInput, mStateInput;
    private CheckBox mTermsCheckbox;
    private Button mRegisterSubmitButton;
    private TextView mToggleTermsText, mTermsParagraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_new_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Link all UI components
        mEmailInput = findViewById(R.id.emailInput);
        mPasswordInput = findViewById(R.id.passwordInput);
        mReenterPasswordInput = findViewById(R.id.reenterPasswordInput);
        mFirstNameInput = findViewById(R.id.firstNameInput);
        mLastNameInput = findViewById(R.id.lastNameInput);
        mPhoneNumberInput = findViewById(R.id.phoneNumberInput);
        mCityInput = findViewById(R.id.cityInput);
        mStateInput = findViewById(R.id.stateInput);
        mTermsCheckbox = findViewById(R.id.termsCheckbox);
        mRegisterSubmitButton = findViewById(R.id.registerSubmitButton);
        mToggleTermsText = findViewById(R.id.toggleTermsText);
        mTermsParagraph = findViewById(R.id.termsParagraph);

        // Toggle terms visibility
        mToggleTermsText.setOnClickListener(v -> {
            if (mTermsParagraph.getVisibility() == View.GONE) {
                mTermsParagraph.setVisibility(View.VISIBLE);
                mToggleTermsText.setText("Hide Terms and Conditions ▲");
            } else {
                mTermsParagraph.setVisibility(View.GONE);
                mToggleTermsText.setText("Show Terms and Conditions ▼");
            }
        });

        // Register button logic
        mRegisterSubmitButton.setOnClickListener(v -> {
            String email = mEmailInput.getText().toString().trim();
            String password = mPasswordInput.getText().toString().trim();
            String rePassword = mReenterPasswordInput.getText().toString().trim();
            String firstName = mFirstNameInput.getText().toString().trim();
            String lastName = mLastNameInput.getText().toString().trim();
            String phone = mPhoneNumberInput.getText().toString().trim();
            String city = mCityInput.getText().toString().trim();
            String state = mStateInput.getText().toString().trim();

            // Validate fields
            if (email.isEmpty() || password.isEmpty() || rePassword.isEmpty()
                    || firstName.isEmpty() || lastName.isEmpty()
                    || phone.isEmpty() || city.isEmpty() || state.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!password.equals(rePassword)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!mTermsCheckbox.isChecked()) {
                Toast.makeText(this, "You must agree to the terms and conditions.", Toast.LENGTH_LONG).show();
                return;
            }

            // All good — go back to login screen
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginScreenActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
