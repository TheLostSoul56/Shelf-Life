package com.example.shelflife;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Set splash screen layout
        setContentView(R.layout.activity_splash_screen);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Apply padding to prevent system UI overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Delay before going to login screen
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, Login.class);
            startActivity(intent);
            finish();
        }, 3000); // 3 seconds
    }
}
