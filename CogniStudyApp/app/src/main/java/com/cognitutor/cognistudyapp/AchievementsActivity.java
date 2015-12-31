package com.cognitutor.cognistudyapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        // TODO:1 back button goes to menu fragment
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
