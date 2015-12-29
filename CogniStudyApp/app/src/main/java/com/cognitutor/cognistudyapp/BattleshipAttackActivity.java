package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class BattleshipAttackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battleship_attack);
    }

    public void navigateToChallengeActivity(View view) {
        Intent intent = new Intent(this, ChallengeActivity.class);
        startActivity(intent);
    }
}
