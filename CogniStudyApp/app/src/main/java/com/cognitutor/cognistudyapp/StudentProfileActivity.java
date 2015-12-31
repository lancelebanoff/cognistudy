package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StudentProfileActivity extends CogniActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
    }

    public void navigateToNewChallengeActivity(View view) {
        Intent intent = new Intent(this, NewChallengeActivity.class);
        // TODO:1 opponent is already chosen
        startActivity(intent);
    }
}
