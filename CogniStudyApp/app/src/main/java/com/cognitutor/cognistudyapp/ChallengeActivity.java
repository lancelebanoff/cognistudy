package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ChallengeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void navigateToQuestionActivity(View view) {
        Intent intent = new Intent(this, QuestionActivity.class);
        startActivity(intent);
    }

    public void navigateToQuestionHistoryActivity(View view) {
        Intent intent = new Intent(this, QuestionHistoryActivity.class);
        startActivity(intent);
    }

    public void navigateToChallengeAnalyticsActivity(View view) {
        Intent intent = new Intent(this, ChallengeAnalyticsActivity.class);
        startActivity(intent);
    }
}
