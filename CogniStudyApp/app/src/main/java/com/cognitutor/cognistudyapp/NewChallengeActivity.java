package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class NewChallengeActivity extends CogniActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);
    }

    public void navigateToChooseBoardConfigurationActivity(View view) {
        Intent intent = new Intent(this, ChooseBoardConfigurationActivity.class);
        startActivity(intent);
        finish();
    }
}
