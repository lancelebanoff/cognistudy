package com.cognitutor.cognistudyapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckedTextView;
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
    private ArrayList<CheckBox> mSubjectCheckboxes;
    private RadioButton mRbDefaultTest;
    private RadioButton mRbDefaultOpponent;
    private AlertDialog mDialogCategories;

    private Challenge mChallenge;
    private String mSelectedTest;
    private ArrayList<String> mSelectedSubjects;
    private ArrayList<String> mSelectedCategories;
    private String mSelectedOpponent;

    private final String DEFAULT_TEST = Constants.Test.BOTH;
    private final String DEFAULT_OPPONENT = Constants.OpponentType.COMPUTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);
        mIntent = getIntent();

        mSubjectCheckboxes = new ArrayList<>();
        mRbDefaultTest = null;
        mRbDefaultOpponent = null;
        mDialogCategories = null;

        mChallenge = null;
        mSelectedTest = "";
        mSelectedSubjects = new ArrayList<>();
        mSelectedCategories = new ArrayList<>();
        mSelectedOpponent = "";

        displayTests();
        displaySubjects();
        displayOpponent();
        setDefaults();
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
            if(testName.equals(DEFAULT_TEST)) {
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
            mRgOpponents.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    mSelectedOpponent = ((RadioButton)findViewById(checkedId)).getText().toString();
                }
            });

            String[] opponentTypes = Constants.getAllConstants(Constants.OpponentType.class);
            for(String opponentType : opponentTypes) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(opponentType);
                mRgOpponents.addView(radioButton);

                if(opponentType.equals(DEFAULT_OPPONENT)) {
                    mRbDefaultOpponent = radioButton;
                }
            }
        }
        else {
            TextView txtPlayerName = (TextView) findViewById(R.id.txtPlayerName);
            txtPlayerName.setText("Player " + opponentId);
        }
    }

    private void setDefaults() {
        mRbDefaultTest.performClick();
        mRbDefaultOpponent.performClick();
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
        displayCategories();

//        saveChallenge();
//        Intent intent = new Intent(this, ChooseCategoriesActivity.class);
//        startActivity(intent);
    }

    public void onClick_btnPlayNow(View view) {
        Intent intent = new Intent(this, ChooseBoardConfigurationActivity.class);
        startActivity(intent);
        finish();
    }

    private void displayCategories() {
        String[] categories = Constants.getAllConstants(Constants.Category.class);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        mDialogCategories = dialogBuilder
                .setTitle(R.string.title_dialog_choose_categories)
                .setMultiChoiceItems(categories, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int indexSelected, boolean isChecked) {
                        addOrRemoveSelectedCategory(indexSelected);
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on OK
                        //  You can write the code  to save the selected item here
                    }
                }).create();
        mDialogCategories.show();
    }

    public void addOrRemoveSelectedCategory(int indexSelected) {
        AppCompatCheckedTextView cbCategory =
                (AppCompatCheckedTextView) mDialogCategories.getListView().getChildAt(indexSelected);
        String category = cbCategory.getText().toString();
        if(cbCategory.isChecked()) {
            mSelectedCategories.add(category);
        }
        else {
            mSelectedCategories.remove(category);
        }
    }

    private void saveChallenge() {
        mChallenge = new Challenge();
        ChallengeUserData user1Data = new ChallengeUserData();
        try {
            user1Data.setPublicUserData(UserUtils.getPublicUserData());
        } catch (ParseException e) {
            handleParseError(e);
        }
        mChallenge.setUser1Data(user1Data);

        user1Data.setSubjects(mSelectedSubjects);
        user1Data.setCategories(mSelectedCategories);
        user1Data.saveInBackground();

        mChallenge.setChallengeType(getChallengeType());

        mChallenge.saveInBackground();
    }

    private String getChallengeType() {
        switch(mSelectedOpponent) {
            case Constants.OpponentType.FRIEND:
            case Constants.OpponentType.RANDOM:
                return Constants.ChallengeType.TWO_PLAYER;
            case Constants.OpponentType.COMPUTER:
                return Constants.ChallengeType.ONE_PLAYER;
            case Constants.OpponentType.PRACTICE:
                return Constants.ChallengeType.PRACTICE;
        }
        return null;
    }
}
