package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setButtonOnClickListeners();
    }

    private void setButtonOnClickListeners() {
        View btnFacebookSignIn = findViewById(R.id.btnFacebookSignIn);
        btnFacebookSignIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                navigateToMainActivity();
            }
        });

        View btnEmailSignIn = findViewById(R.id.btnEmailSignIn);
        btnEmailSignIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                navigateToMainActivity();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
