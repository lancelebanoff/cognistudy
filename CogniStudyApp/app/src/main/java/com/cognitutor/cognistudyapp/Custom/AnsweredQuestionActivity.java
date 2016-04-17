package com.cognitutor.cognistudyapp.Custom;

import android.os.Bundle;
import android.view.View;

import com.cognitutor.cognistudyapp.Activities.QuestionActivity;
import com.cognitutor.cognistudyapp.R;

/**
 * Created by Kevin on 4/16/2016.
 */
public abstract class AnsweredQuestionActivity extends QuestionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CogniButton btnContinue = (CogniButton) findViewById(R.id.btnContinue);
        btnContinue.setVisibility(View.GONE);
    }
}
