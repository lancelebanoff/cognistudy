package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewChallengeActivity extends CogniActivity {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     */
    private Intent mIntent;
    private LinearLayout mLlSubjects;
    private RadioGroup mRgTests;
    private RadioGroup mRgOpponents;
    private Challenge mChallenge;
    private String mSelectedTest;
    private ArrayList<String> mSelectedSubjects;
    private ArrayList<String> mSelectedCategories;
    private ArrayList<CheckBox> mSubjectCheckboxes;
    private RadioButton mRbDefaultTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);
        mIntent = getIntent();

        mChallenge = null;
        mSelectedTest = "";
        mSelectedSubjects = new ArrayList<>();
        mSelectedCategories = new ArrayList<>();
        mSubjectCheckboxes = new ArrayList<>();
        mRbDefaultTest = null;

        displayTests();
        displaySubjects();
        displayOpponent();
        mRbDefaultTest.performClick();
    }

    private void displayTests() {
        mRgTests = (RadioGroup) findViewById(R.id.rgTests);
        mRgTests.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeSelectedTest((RadioButton) findViewById(checkedId));
            }
        });

        String[] testNames = Constants.getAllConstants(Constants.Test.class);
        for(String testName : testNames) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(testName);
            mRgTests.addView(radioButton);

            // Initialize chosen test to Both
            // TODO:1 Initialize chosen tests, subjects, and categories to whatever was chosen last time
            if(testName.equals(Constants.Test.BOTH)) {
                mRbDefaultTest = radioButton;
            }
        }
    }

    private void displaySubjects() {
        mLlSubjects = (LinearLayout) findViewById(R.id.llSubjects);
        String[] subjectNames = Constants.getAllConstants(Constants.Subject.class);
        for(String subjectName : subjectNames) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(subjectName);
            checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    addOrRemoveSelectedSubject((CheckBox) buttonView);
                }
            });
            mLlSubjects.addView(checkBox);
            mSubjectCheckboxes.add(checkBox);

            // Initialize all subjects to be chosen
            // TODO:1 Initialize chosen tests, subjects, and categories to whatever was chosen last time
            checkBox.performClick();
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

    private void saveChallenge() {
        if(mChallenge == null) {
            mChallenge = new Challenge();
            ChallengeUserData user1Data = new ChallengeUserData();
            try {
                user1Data.setPublicUserData(UserUtils.getPublicUserData());
            } catch (ParseException e) {
                handleParseError(e);
            }
            user1Data.setSubjects(mSelectedSubjects);
            user1Data.setCategories(mSelectedCategories);
            user1Data.saveInBackground();

            mChallenge.setUser1Data(user1Data);
        }
    }

    // Changing selected test resets all of the selected subjects and categories
    public void changeSelectedTest(RadioButton rbTest) {
        mSelectedTest = rbTest.getText().toString();
        mSelectedSubjects.clear();
        mSelectedCategories.clear();
        List<String> subjectsInSelectedTest = Arrays.asList(Constants.TestToSubject.get(mSelectedTest));
        for(CheckBox subjectCheckbox : mSubjectCheckboxes) {
            subjectCheckbox.setChecked(false);
            if(subjectsInSelectedTest.contains(subjectCheckbox.getText().toString())) {
                subjectCheckbox.performClick();
            }
        }
    }

    public void addOrRemoveSelectedSubject(CheckBox cbSubject) {
        String subject = cbSubject.getText().toString();
        List<String> categoriesInSelectedSubject = Arrays.asList(Constants.SubjectToCategory.get(subject));
        if(cbSubject.isChecked()) {
            mSelectedSubjects.add(subject);
            mSelectedCategories.addAll(categoriesInSelectedSubject);
        }
        else {
            mSelectedSubjects.remove(subject);
            mSelectedCategories.removeAll(categoriesInSelectedSubject);
        }
    }

    public void onClick_btnChooseCategories(View view) {
        saveChallenge();
        Intent intent = new Intent(this, ChooseCategoriesActivity.class);
        startActivity(intent);
    }

    public void onClick_btnPlayNow(View view) {
        Intent intent = new Intent(this, ChooseBoardConfigurationActivity.class);
        startActivity(intent);
        finish();
    }
}
