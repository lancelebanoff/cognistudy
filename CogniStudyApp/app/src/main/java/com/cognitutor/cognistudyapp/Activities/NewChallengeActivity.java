package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class NewChallengeActivity extends CogniActivity {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     *      USER1OR2: int
     *      CHALLENGE_ID: string
     */
    private Intent mIntent;
    private ArrayList<CheckBox> mSubjectCheckboxes;
    private ArrayList<CheckBox> mCategoryCheckboxes;
    private ScrollView mSvCategories;
    private RadioButton mRbDefaultTest;
    private RadioButton mRbDefaultOpponent;

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
        mCategoryCheckboxes = new ArrayList<>();
        mSvCategories = null;
        mRbDefaultTest = null;
        mRbDefaultOpponent = null;

        mSelectedTest = "";
        mSelectedSubjects = new ArrayList<>();
        mSelectedCategories = new ArrayList<>();
        mSelectedOpponent = "";

        displayTests();
        displaySubjects();
        drawCategories();
        displayOpponent();
        setDefaults();
    }

    private void displayTests() {
        RadioGroup mRgTests = (RadioGroup) findViewById(R.id.rgTests);
        mRgTests.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeSelectedTest((RadioButton) findViewById(checkedId));
            }
        });

        String[] testNames = Constants.Test.getTests();
        for(String testName : testNames) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(testName);
            mRgTests.addView(radioButton);

            // Initialize chosen test to Both
            // TODO:2 Initialize chosen tests, subjects, and categories to whatever was chosen last time
            if(testName.equals(DEFAULT_TEST)) {
                mRbDefaultTest = radioButton;
            }
        }
    }

    private void displaySubjects() {
        LinearLayout llSubjects = (LinearLayout) findViewById(R.id.llSubjects);
        String[] subjectNames = Constants.Subject.getSubjects();
        for(String subjectName : subjectNames) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(subjectName);
            checkBox.setOnClickListener(new CheckBox.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addOrRemoveSelectedSubject((CheckBox) view);
                }
            });
            llSubjects.addView(checkBox);
            mSubjectCheckboxes.add(checkBox);

            // Initialize all subjects to be chosen
            // TODO:2 Initialize chosen tests, subjects, and categories to whatever was chosen last time
            checkBox.performClick();
        }
    }

    private void drawCategories() {
        mSvCategories = new ScrollView(this);
        LinearLayout llCategories = new LinearLayout(this);
        llCategories.setOrientation(LinearLayout.VERTICAL);
        mSvCategories.addView(llCategories);

        String[] categories = Constants.Category.getCategories();
        for(String category : categories) {
            View checkBoxView = View.inflate(this, R.layout.checkbox_category, null);
            CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
            if(mSelectedCategories.contains(category)) {
                checkBox.setChecked(true);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton cbCategory, boolean isChecked) {
                    addOrRemoveSelectedCategory(cbCategory);
                }
            });
            checkBox.setText(category);
            llCategories.addView(checkBoxView);
            mCategoryCheckboxes.add(checkBox);
        }
    }

    private void displayOpponent() {
        int user1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        if(user1or2 == 1) {
            // Switch from opponent info to radio button to choose opponent
            ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
            viewSwitcher.showNext();

            RadioGroup mRgOpponents = (RadioGroup) findViewById(R.id.rgOpponents);
            mRgOpponents.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    mSelectedOpponent = ((RadioButton)findViewById(checkedId)).getText().toString();
                }
            });

            String[] opponentTypes = Constants.OpponentType.getOpponentTypes();
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
            LinearLayout llOpponent = (LinearLayout) findViewById(R.id.llOpponent);
            llOpponent.setVisibility(View.INVISIBLE);

//            String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
//            Challenge.getChallenge(challengeId)
//                    .onSuccess(new Continuation<Challenge, Void>() {
//                        @Override
//                        public Void then(Task<Challenge> task) throws Exception {
//                            // Retrieve opponent's name and photo
//                            Challenge challenge = task.getResult();
//                            ChallengeUserData user1Data = challenge.getUser1Data().fetchIfNeeded();
//                            PublicUserData user1PublicUserData = user1Data.getPublicUserData();
//                            String user1Name = user1PublicUserData.getDisplayName();
//                            ParseFile user1Picture = user1PublicUserData.getProfilePic();
//
//                            // Display opponent's name and photo
//                            TextView txtPlayerName = (TextView) findViewById(R.id.txtName);
//                            txtPlayerName.setText(user1Name);
//                            RoundedImageView profilePic = (RoundedImageView) findViewById(R.id.imgProfileRounded);
//                            profilePic.setParseFile(user1Picture);
//                            profilePic.loadInBackground();
//
//                            return null;
//                        }
//                    });
        }
    }

    private void setDefaults() {
        mRbDefaultTest.performClick();
        if(mRbDefaultOpponent != null) {
            mRbDefaultOpponent.performClick();
        }
    }

    // Changing selected test resets all of the selected subjects and categories
    public void changeSelectedTest(RadioButton rbTest) {
        mSelectedTest = rbTest.getText().toString();
        mSelectedSubjects.clear();
        mSelectedCategories.clear();
        for(CheckBox cbSubject : mSubjectCheckboxes) {
            cbSubject.setChecked(false);
        }
        List<String> categoriesInSelectedTest = Arrays.asList(Constants.TestToCategory.get(mSelectedTest));
        for(CheckBox cbCategory : mCategoryCheckboxes) {
            cbCategory.setChecked(false);
            if(categoriesInSelectedTest.contains(cbCategory.getText().toString())) {
                cbCategory.setChecked(true);
            }
        }
    }

    public void addOrRemoveSelectedSubject(CheckBox cbSubject) {
        String subject = cbSubject.getText().toString();
        List<String> categoriesInSelectedSubject = Arrays.asList(Constants.SubjectToCategory.get(subject));
        if(cbSubject.isChecked()) {
            mSelectedSubjects.add(subject);

            // Set subject's corresponding categories to be chosen
            mSelectedCategories.addAll(categoriesInSelectedSubject);
            for(CheckBox cbCategory : mCategoryCheckboxes) {
                String category = cbCategory.getText().toString();
                if(categoriesInSelectedSubject.contains(category) && !cbCategory.isChecked()) {
                    cbCategory.setChecked(true);
                }
            }
        }
        else {
            mSelectedSubjects.remove(subject);

            // Set subject's corresponding categories to be unchosen
            mSelectedCategories.removeAll(categoriesInSelectedSubject);
            for(CheckBox cbCategory : mCategoryCheckboxes) {
                String category = cbCategory.getText().toString();
                if(categoriesInSelectedSubject.contains(category) && cbCategory.isChecked()) {
                    cbCategory.setChecked(false);
                }
            }
        }
    }

    public void onClick_btnChooseCategories(View view) {
        displayCategories();
    }

    private void displayCategories() {
        if(mSvCategories.getParent() != null) {
            ((ViewGroup) mSvCategories.getParent()).removeView(mSvCategories);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_categories);
        builder.setView(mSvCategories)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).show();
    }

    public void addOrRemoveSelectedCategory(CompoundButton cbCategory) {
        String category = cbCategory.getText().toString();
        if(cbCategory.isChecked()) {
            mSelectedCategories.add(category);

            // Set category's corresponding subject to be chosen
            for(CheckBox cbSubject : mSubjectCheckboxes) {
                String subject = cbSubject.getText().toString();
                List<String> categoriesInSubject = Arrays.asList(Constants.SubjectToCategory.get(subject));
                if(categoriesInSubject.contains(category) && !mSelectedSubjects.contains(subject)) {
                    cbSubject.setChecked(true);
                    mSelectedSubjects.add(subject);
                }
            }
        }
        else {
            mSelectedCategories.remove(category);
        }
    }

    public void onClick_btnPlayNow(View view) {
        saveChallenge();
    }

    private void saveChallenge() {
        int user1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        if(user1or2 == 1) {
            saveNewChallenge();
        }
        else {
            saveExistingChallenge();
        }
    }

    private void saveNewChallenge() {
        PublicUserData user1PublicUserData = PublicUserData.getPublicUserData();
        ChallengeUserData user1Data = new ChallengeUserData(user1PublicUserData, mSelectedSubjects,
                mSelectedCategories);
        user1Data.saveInBackground();

        final Challenge challenge = new Challenge(user1Data, getChallengeType());
        challenge.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    if (mSelectedOpponent.equals(Constants.OpponentType.FRIEND)) {
                        navigateToChooseOpponentActivity(challenge.getObjectId(), 1);
                    } else {
                        navigateToChooseBoardConfigurationActivity(challenge.getObjectId(), 1);
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveExistingChallenge() {
        final String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);

        Challenge.getChallenge(challengeId)
                .onSuccess(new Continuation<Challenge, Void>() {
                    @Override
                    public Void then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        ChallengeUserData user2Data = challenge.getUser2Data().fetchIfNeeded();
                        user2Data.setSubjects(mSelectedSubjects);
                        user2Data.setCategories(mSelectedCategories);
                        user2Data.saveInBackground();

                        navigateToChooseBoardConfigurationActivity(challengeId, 2);

                        return null;
                    }
                });
    }

    private void navigateToChooseBoardConfigurationActivity(String challengeId, int user1or2) {
        Intent intent = new Intent(this, ChooseBoardConfigurationActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, challengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, user1or2);
        startActivity(intent);
        finish();
    }

    private void navigateToChooseOpponentActivity(String challengeId, int user1or2) {
        Intent intent = new Intent(this, ChooseOpponentActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, challengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, user1or2);
        startActivity(intent);
        finish();
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
