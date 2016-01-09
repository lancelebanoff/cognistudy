package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.R;

public class NewChallengeActivity extends CogniActivity {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     */
    private Intent mIntent;
    private RadioGroup mRgTests;
    private RadioGroup mRgSubjects;
    private RadioGroup mRgOpponents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);
        mIntent = getIntent();

        displayTests();
        displaySubjects();
        displayOpponent();
    }

    private void displayTests() {
        mRgTests = (RadioGroup) findViewById(R.id.rgTests);
        String[] testNames = Constants.getAllConstants(Constants.Test.class);
        for(String testName : testNames) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(testName);
            mRgTests.addView(radioButton);
        }
    }

    private void displaySubjects() {
        mRgSubjects = (RadioGroup) findViewById(R.id.rgSubjects);
        String[] subjectNames = Constants.getAllConstants(Constants.Subject.class);
        for(String subjectName : subjectNames) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(subjectName);
            mRgSubjects.addView(radioButton);
        }
    }

    private void displayOpponent() {
        int opponentId = mIntent.getIntExtra(Constants.IntentExtra.OpponentId.OPPONENT_ID, Constants.IntentExtra.OpponentId.UNKNOWN);
        if(opponentId == Constants.IntentExtra.OpponentId.UNKNOWN) {
            // Switch from opponent info to radio button to choose opponent
            ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
            viewSwitcher.showNext();

            mRgOpponents = (RadioGroup) findViewById(R.id.rgOpponents);
            String[] opponentTypes = Constants.getAllConstants(Constants.OpponentType.class);
            for(String opponentType : opponentTypes) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(opponentType);
                mRgOpponents.addView(radioButton);
            }
        }
        else {
            TextView txtPlayerName = (TextView) findViewById(R.id.txtPlayerName);
            txtPlayerName.setText("Player " + opponentId);
        }
    }

    public void navigateToChooseCategoriesActivity(View view) {
        Intent intent = new Intent(this, ChooseCategoriesActivity.class);
        startActivity(intent);
    }

    public void navigateToChooseBoardConfigurationActivity(View view) {
        Intent intent = new Intent(this, ChooseBoardConfigurationActivity.class);
        startActivity(intent);
        finish();
    }
}
