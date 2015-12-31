package com.cognitutor.cognistudyapp;

import android.os.Bundle;
import android.view.View;

public class BattleshipAttackActivity extends CogniActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battleship_attack);
    }

    public void navigateToChallengeActivity(View view) {
        finish();
    }
}
