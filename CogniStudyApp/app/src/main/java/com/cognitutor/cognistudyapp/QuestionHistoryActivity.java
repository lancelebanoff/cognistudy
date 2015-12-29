package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class QuestionHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void navigateToPastQuestionActivity(View view) {
        Intent intent = new Intent(this, PastQuestionActivity.class);
        startActivity(intent);
    }
}
