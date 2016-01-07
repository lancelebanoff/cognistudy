package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class NewChallengeActivity extends CogniActivity {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     */
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);
        mIntent = getIntent();

        displayOpponent();
    }

    private void displayOpponent() {
        int opponentId = mIntent.getIntExtra(Constants.IntentExtra.OpponentId.OPPONENT_ID, Constants.IntentExtra.OpponentId.UNKNOWN);
        if(opponentId == Constants.IntentExtra.OpponentId.UNKNOWN) {
            // Switch Submit button to Continue button
            ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
            viewSwitcher.showNext();
        }
        else {
            TextView txtPlayerName = (TextView) findViewById(R.id.txtPlayerName);
            txtPlayerName.setText("Player " + opponentId);
        }
    }

    public void navigateToChooseBoardConfigurationActivity(View view) {
        Intent intent = new Intent(this, ChooseBoardConfigurationActivity.class);
        startActivity(intent);
        finish();
    }
}
