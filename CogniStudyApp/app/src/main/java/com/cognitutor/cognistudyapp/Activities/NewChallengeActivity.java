package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.CogniCheckBox;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private ArrayList<CogniCheckBox> mSubjectCheckboxes;
    private ArrayList<CheckBox> mCategoryCheckboxes;
    private ScrollView mSvCategories;
    private RadioButton mRbDefaultTest;
    private RadioButton mRbDefaultOpponent;

    private String mSelectedTest;
    private HashSet<String> mSelectedSubjects;
    private HashSet<String> mSelectedCategories;
    private String mSelectedOpponent;
    private HashMap<CheckBox, String> mCategoryCheckboxToCategory;

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
        mSelectedSubjects = new HashSet<>();
        mSelectedCategories = new HashSet<>();
        mSelectedOpponent = "";
        mCategoryCheckboxToCategory = new HashMap<>();

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
        String[] subjectNames = Constants.Subject.getSubjects();

        LinearLayout llSubjectsHolder = (LinearLayout) findViewById(R.id.llSubjectsHolder);
        int numColumns = 2;

        LinearLayout llSubjectRow = null;
        for(int i = 0; i < subjectNames.length; i++) {
            if (i % numColumns == 0) {
                llSubjectRow = new LinearLayout(this);
                llSubjectRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                llSubjectRow.setOrientation(LinearLayout.HORIZONTAL);
                llSubjectsHolder.addView(llSubjectRow);
            }

            CogniCheckBox checkBox = new CogniCheckBox(this);
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            checkBox.setText(subjectNames[i]);
            checkBox.setOnClickListener(new CogniCheckBox.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addOrRemoveSelectedSubject((CogniCheckBox) view);
                }
            });
            llSubjectRow.addView(checkBox);
            mSubjectCheckboxes.add(checkBox);

            // TODO:2 Initialize chosen tests, subjects, and categories to whatever was chosen last time
        }
    }

    private void drawCategories() {
        mSvCategories = new ScrollView(this);
        LinearLayout llCategories = new LinearLayout(this);
        llCategories.setOrientation(LinearLayout.VERTICAL);
        llCategories.setPadding(0, 100, 0, 0);
        mSvCategories.addView(llCategories);

        String[] subjects = Constants.Subject.getSubjects();
        for(String subject : subjects) {
            String[] categoriesInSubject = Constants.SubjectToCategory.get(subject);
            for(String category : categoriesInSubject) {
                View checkBoxView = View.inflate(this, R.layout.checkbox_category, null);
                CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
                mCategoryCheckboxToCategory.put(checkBox, category);
                if(mSelectedCategories.contains(category)) {
                    checkBox.setChecked(true);
                }
                checkBox.setOnClickListener(new CheckBox.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addOrRemoveSelectedCategory((CheckBox) v);
                    }
                });
                checkBox.setText(subject + ": " + category);
                llCategories.addView(checkBoxView);
                mCategoryCheckboxes.add(checkBox);
            }
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
//            Challenge.getChallengeInBackground(challengeId)
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
        List<String> subjectsInSelectedTest = Arrays.asList(Constants.TestToSubject.get(mSelectedTest));
        mSelectedSubjects.addAll(subjectsInSelectedTest);
        mSelectedCategories.clear();
        List<String> categoriesInSelectedTest = Arrays.asList(Constants.TestToCategory.get(mSelectedTest));
        mSelectedCategories.addAll(categoriesInSelectedTest);
        for(CogniCheckBox cbSubject : mSubjectCheckboxes) {
            cbSubject.setChecked(false);
            String subject = cbSubject.getText().toString();
            if (subjectsInSelectedTest.contains(subject)) {
                cbSubject.setChecked(true);
            }
        }
        for(CheckBox cbCategory : mCategoryCheckboxes) {
            cbCategory.setChecked(false);
            if(categoriesInSelectedTest.contains(mCategoryCheckboxToCategory.get(cbCategory))) {
                cbCategory.setChecked(true);
            }
        }
    }

    public void addOrRemoveSelectedSubject(CogniCheckBox cbSubject) {
        String subject = cbSubject.getText().toString();
        List<String> categoriesInSelectedSubject = Arrays.asList(Constants.SubjectToCategory.get(subject));
        if(cbSubject.isChecked()) {
            mSelectedSubjects.add(subject);

            // Set subject's corresponding categories to be chosen
            mSelectedCategories.addAll(categoriesInSelectedSubject);
            for(CheckBox cbCategory : mCategoryCheckboxes) {
                String category = mCategoryCheckboxToCategory.get(cbCategory);
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
                String category = mCategoryCheckboxToCategory.get(cbCategory);
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

    public void addOrRemoveSelectedCategory(CheckBox cbCategory) {
        String category = mCategoryCheckboxToCategory.get(cbCategory);
        if(cbCategory.isChecked()) {
            mSelectedCategories.add(category);

            // Set category's corresponding subject to be chosen
            for(CogniCheckBox cbSubject : mSubjectCheckboxes) {
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

            // If category's corresponding subject has no categories selected, then uncheck that subject
            for(CogniCheckBox cbSubject : mSubjectCheckboxes) {
                String subject = cbSubject.getText().toString();
                List<String> categoriesInSubject = Arrays.asList(Constants.SubjectToCategory.get(subject));
                if(categoriesInSubject.contains(category) && Collections.disjoint(mSelectedCategories, categoriesInSubject)) {
                    cbSubject.setChecked(false);
                    mSelectedSubjects.remove(subject);
                }
            }
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
        PublicUserData.getPublicUserDataInBackground().continueWithTask(new Continuation<PublicUserData, Task<Object>>() {
            @Override
            public Task<Object> then(Task<PublicUserData> task) throws Exception {
                final PublicUserData user1PublicUserData = task.getResult();
                final ChallengeUserData user1Data = new ChallengeUserData(user1PublicUserData,
                        new ArrayList<String>(mSelectedSubjects), new ArrayList<String>(mSelectedCategories));
                user1Data.saveInBackground();

                final String challengeType = getChallengeType();
                final Challenge challenge = new Challenge(user1Data, challengeType);
                return challenge.saveInBackground().continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        Exception e = task.getError();
                        if (e == null) {
                            if (challengeType.equals(Constants.ChallengeType.PRACTICE)) {
                                savePracticeChallenge(challenge, user1PublicUserData);
                            } else if (mSelectedOpponent.equals(Constants.OpponentType.FRIEND)) {
                                navigateToChooseOpponentActivity(challenge.getObjectId(), 1);
                            } else {
                                navigateToChooseBoardConfigurationActivity(challenge.getObjectId(), 1);
                            }
                        } else {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            }
        });
    }

    private void savePracticeChallenge(final Challenge challenge, PublicUserData publicUserData) {
        challenge.setCurTurnUserId(publicUserData.getBaseUserId());
        challenge.setActivated(true);
        challenge.setAccepted(true);
        challenge.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                navigateToPracticeChallengeActivity(challenge.getObjectId(), 1);
            }
        });
    }

    private void saveExistingChallenge() {
        final String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);

        Challenge.getChallengeInBackground(challengeId)
                .onSuccess(new Continuation<Challenge, Void>() {
                    @Override
                    public Void then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        ChallengeUserData user2Data = challenge.getUser2Data().fetchIfNeeded();
                        user2Data.setSubjects(new ArrayList<String>(mSelectedSubjects));
                        user2Data.setCategories(new ArrayList<String>(mSelectedCategories));
                        user2Data.saveInBackground();

                        navigateToChooseBoardConfigurationActivity(challengeId, 2);

                        return null;
                    }
                });
    }

    private void navigateToPracticeChallengeActivity(String challengeId, int user1or2) {
        Intent intent = new Intent(this, PracticeChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, challengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, user1or2);
        startActivity(intent);
        finish();
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
