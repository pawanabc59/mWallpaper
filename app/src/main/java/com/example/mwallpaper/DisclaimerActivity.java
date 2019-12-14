package com.example.mwallpaper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class DisclaimerActivity extends AppCompatActivity {

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.loadNightModeState() == true) {

            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_disclaimer);
    }
}
