package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class RegistrationActivity extends CogniActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    public void navigateToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
