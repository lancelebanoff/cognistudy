package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ChooseBoardConfigurationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_board_configuration);
        // TODO:L2 Delete challenge from database when hitting back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void navigateToChallengeActivity(View view) {
        Intent intent = new Intent(this, ChallengeActivity.class);
        startActivity(intent);
    }
}
