package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChooseBoardConfigurationActivity extends CogniActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_board_configuration);
        // TODO:2 Delete challenge from database when hitting back button
    }

    public void navigateToChallengeActivity(View view) {
        Intent intent = new Intent(this, ChallengeActivity.class);
        startActivity(intent);
        finish();
    }
}
