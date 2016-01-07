package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cognitutor.cognistudyapp.R;

public class QuestionHistoryActivity extends CogniActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_history);
    }

    public void navigateToPastQuestionActivity(View view) {
        Intent intent = new Intent(this, PastQuestionActivity.class);
        startActivity(intent);
    }
}
