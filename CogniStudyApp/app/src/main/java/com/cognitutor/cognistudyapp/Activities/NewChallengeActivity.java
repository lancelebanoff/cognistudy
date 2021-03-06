package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.ViewFlipper;

import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.CogniCheckBox;
import com.cognitutor.cognistudyapp.Custom.CogniRadioButton;
import com.cognitutor.cognistudyapp.Custom.CogniRadioGroup;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    private CogniRadioGroup mRgTests;
    private ArrayList<CogniCheckBox> mSubjectCheckboxes;
    private ArrayList<CheckBox> mCategoryCheckboxes;
    private ScrollView mSvCategories;
    private CogniRadioButton mRbDefaultTest;
    private CogniRadioButton mRbDefaultOpponent;
    private ViewFlipper mViewFlipper;

    private String mSelectedTest;
    private HashSet<String> mSelectedSubjects;
    private HashSet<String> mSelectedCategories;
    private String mSelectedOpponent;
    private HashMap<CheckBox, String> mCategoryCheckboxToCategory;
    private String mOpponentBaseUserId;
    private ChallengeUserData mUser2Data;

    private String DEFAULT_TEST = Constants.Test.BOTH;
    private String DEFAULT_OPPONENT = Constants.OpponentType.FRIEND;
    private final List<String> mEnabledSubjects = Arrays.asList(new String[] {
            Constants.Subject.MATH,
            Constants.Subject.ENGLISH,
            Constants.Subject.SCIENCE
    });
    private final List<String> mEnabledCategories = Arrays.asList(new String[] {
            Constants.Category.PRE_ALGEBRA,
            Constants.Category.USAGE_AND_MECHANICS,
            Constants.Category.RHETORICAL_SKILLS,
            Constants.Category.DATA_REPRESENTATION,
            Constants.Category.RESEARCH_SUMMARIES,
            Constants.Category.CONFLICTING_VIEWPOINTS
    });

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
        mViewFlipper = (ViewFlipper) findViewById(R.id.viewSwitcher);

        mSelectedTest = "";
        mSelectedSubjects = new HashSet<>();
        mSelectedCategories = new HashSet<>();
        mSelectedOpponent = "";
        mCategoryCheckboxToCategory = new HashMap<>();
        mOpponentBaseUserId = "";
        mUser2Data = null;

        displayTests();
        displaySubjects();
        drawCategories();

        boolean firstTime = showTutorialDialogIfNeeded(Constants.Tutorial.NEW_CHALLENGE, null);
        if (firstTime) {
            DEFAULT_OPPONENT = Constants.OpponentType.COMPUTER;
        }

        displayOpponent();
        setDefaults();
    }

    private void displayTests() {
        LinearLayout llTestsHolder = (LinearLayout) findViewById(R.id.llTestsHolder);
        mRgTests = new CogniRadioGroup();

        String[] testNames = Constants.Test.getTests();
        for (String testName : testNames) {
            CogniRadioButton radioButton = new CogniRadioButton(this);
            mRgTests.add(radioButton);
            radioButton.setOnClickListener(new CogniRadioButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeSelectedTest((CogniRadioButton) v);
                    showOrHideNextButton();
                }
            });
            radioButton.setText(testName);
            llTestsHolder.addView(radioButton);
            radioButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            // Initialize chosen test to Both
            // TODO:2 Initialize chosen tests, subjects, and categories to whatever was chosen last time
            if (testName.equals(DEFAULT_TEST)) {
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

            CogniCheckBox checkBox = new CogniCheckBox(this, getDrawableLeftResIdForSubject(subjectNames[i]));
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            checkBox.setText(subjectNames[i]);
            checkBox.setOnClickListener(new CogniCheckBox.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addOrRemoveSelectedSubject((CogniCheckBox) view);
                    showOrHideNextButton();
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
        llCategories.setPadding(0, 20, 0, 0);
        mSvCategories.addView(llCategories);

        String[] subjects = Constants.Subject.getSubjects();
        for(String subject : subjects) {
            String[] categoriesInSubject = Constants.SubjectToCategory.get(subject);
            for(String category : categoriesInSubject) {
                View checkBoxView = View.inflate(this, R.layout.checkbox_category, null);
                CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
                mCategoryCheckboxToCategory.put(checkBox, category);
                if(mSelectedCategories.contains(category)) {
                    if(checkBox.isEnabled())
                        checkBox.setChecked(true);
                }
                checkBox.setOnClickListener(new CheckBox.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addOrRemoveSelectedCategory((CheckBox) v);
                        showOrHideNextButton();
                    }
                });
                checkBox.setText(subject + ": " + category);
                llCategories.addView(checkBoxView);
                mCategoryCheckboxes.add(checkBox);
            }
        }
    }

    private void displayOpponent() {
        mOpponentBaseUserId = mIntent.getStringExtra(Constants.IntentExtra.OPPONENT_BASEUSERID);
        int user1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        if (user1or2 == 1 && (mOpponentBaseUserId == null || mOpponentBaseUserId.equals(""))) { // Creating new challenge and opponent has not already been chosen
            LinearLayout llOpponentsHolder = (LinearLayout) findViewById(R.id.llOpponentsHolder);
            LinearLayout llOpponentRow = null;
            CogniRadioGroup mRgOpponents = new CogniRadioGroup();
            int numColumns = 2;

            String[] opponentTypes = Constants.OpponentType.getOpponentTypes();
            for (int i = 0; i < opponentTypes.length; i++) {
                if (i % numColumns == 0) {
                    llOpponentRow = new LinearLayout(this);
                    llOpponentRow.setOrientation(LinearLayout.HORIZONTAL);
                    llOpponentRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    llOpponentsHolder.addView(llOpponentRow);
                }

                String opponentType = opponentTypes[i];
                CogniRadioButton radioButton = new CogniRadioButton(this, getDrawableLeftResIdForOpponent(opponentType));
                radioButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                mRgOpponents.add(radioButton);
                radioButton.setOnClickListener(new CogniRadioButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedOpponent = ((CogniRadioButton) v).getText().toString();
                    }
                });

                radioButton.setText(opponentType);
                llOpponentRow.addView(radioButton);

                if (opponentType.equals(DEFAULT_OPPONENT)) {
                    mRbDefaultOpponent = radioButton;
                }
            }
        } else { // Opponent has already been chosen
            CardView cvOpponent = (CardView) findViewById(R.id.cvOpponent);
            cvOpponent.setVisibility(View.INVISIBLE);
            mSelectedOpponent = Constants.OpponentType.FRIEND;

            // Get opponent PublicUserData and create new ChallengeUserData
            PublicUserData.getPublicUserDataFromBaseUserIdInBackground(mOpponentBaseUserId)
                    .continueWith(new Continuation<PublicUserData, Void>() {
                        @Override
                        public Void then(Task<PublicUserData> task) throws Exception {
                            if (task.isFaulted()) {
                                task.getError().printStackTrace();
                            }
                            ;

                            PublicUserData opponentPublicUserData = task.getResult();
                            mUser2Data = new ChallengeUserData(opponentPublicUserData);
                            return null;
                        }
                    });

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

    private int getDrawableLeftResIdForSubject(String subject) {
        switch (subject) {
            case Constants.Subject.ENGLISH:
                return R.drawable.ic_action_icon_english_transparent;
            case Constants.Subject.MATH:
                return R.drawable.ic_action_icon_math_transparent;
            case Constants.Subject.SCIENCE:
                return R.drawable.ic_action_icon_science_transparent;
            case Constants.Subject.READING:
                return R.drawable.ic_action_icon_reading_transparent;
            default:
                return 0;
        }
    }

    private int getDrawableLeftResIdForOpponent(String opponentType) {
        switch (opponentType) {
            case Constants.OpponentType.COMPUTER:
                return R.drawable.ic_action_icon_computer_opponent;
            case Constants.OpponentType.FRIEND:
                return R.drawable.ic_action_icon_friend_opponent;
            case Constants.OpponentType.PRACTICE:
                return R.drawable.ic_action_icon_practice;
            case Constants.OpponentType.RANDOM:
                return R.drawable.ic_action_icon_random_opponent;
            default:
                return 0;
        }
    }

    private void setDefaults() {
        // In Beta, only some categories are enabled
        for (CheckBox categoryCheckBox : mCategoryCheckboxes) {
            String category = mCategoryCheckboxToCategory.get(categoryCheckBox);
            if (mEnabledCategories.contains(category)) {
                categoryCheckBox.performClick();
            } else {
                categoryCheckBox.setEnabled(false);
            }
        }
        for (CogniCheckBox subjectCheckBox : mSubjectCheckboxes) {
            if (!subjectCheckBox.isChecked()) {
                subjectCheckBox.setEnabled(false);
                subjectCheckBox.setColor(this, R.color.grey);
            }
        }

        mRbDefaultTest.performClick();
        if(mRbDefaultOpponent != null) {
            mRbDefaultOpponent.performClick();
        }
    }

    // Changing selected test resets all of the selected subjects and categories
    public void changeSelectedTest(CogniRadioButton rbTest) {
        mSelectedTest = rbTest.getText().toString();
        mSelectedSubjects.clear();
        List<String> subjectsInSelectedTest = Arrays.asList(Constants.TestToSubject.get(mSelectedTest));
        for (String subjectInSelectedTest : subjectsInSelectedTest) {
            if (mEnabledSubjects.contains(subjectInSelectedTest)) {
                mSelectedSubjects.add(subjectInSelectedTest);
            }
        }
        mSelectedCategories.clear();
        List<String> categoriesInSelectedTest = Arrays.asList(Constants.TestToCategory.get(mSelectedTest));
        for (String categoryInSelectedTest : categoriesInSelectedTest) {
            if (mEnabledCategories.contains(categoryInSelectedTest)) {
                mSelectedCategories.add(categoryInSelectedTest);
            }
        }
        for(CogniCheckBox cbSubject : mSubjectCheckboxes) {
            if(cbSubject.isEnabled())
                cbSubject.setChecked(false);
            String subject = cbSubject.getText().toString();
            if (subjectsInSelectedTest.contains(subject)) {
                if(cbSubject.isEnabled())
                    cbSubject.setChecked(true);
            }
        }
        for(CheckBox cbCategory : mCategoryCheckboxes) {
            if(cbCategory.isEnabled())
                cbCategory.setChecked(false);
            if(categoriesInSelectedTest.contains(mCategoryCheckboxToCategory.get(cbCategory))) {
                if(cbCategory.isEnabled())
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
            for(CheckBox cbCategory : mCategoryCheckboxes) {
                String category = mCategoryCheckboxToCategory.get(cbCategory);
                if(categoriesInSelectedSubject.contains(category) && !cbCategory.isChecked()) {
                    if(cbCategory.isEnabled()) {
                        cbCategory.setChecked(true);
                        mSelectedCategories.add(category);
                    }
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
                    if(cbCategory.isEnabled())
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
                    if(cbSubject.isEnabled())
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
                    if(cbSubject.isEnabled())
                        cbSubject.setChecked(false);
                    mSelectedSubjects.remove(subject);
                }
            }
        }
    }

    public void onClick_btnPlayNow(View view) {
        CogniButton btnPlayNow = (CogniButton) findViewById(R.id.btnPlayNow);
        btnPlayNow.setVisibility(View.INVISIBLE);
        ProgressBar progressBarPlayNow = (ProgressBar) findViewById(R.id.progressBarPlayNow);
        progressBarPlayNow.setVisibility(View.VISIBLE);

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
                ChallengeUserData user1Data = new ChallengeUserData(user1PublicUserData,
                        new ArrayList<String>(mSelectedSubjects),
                        new ArrayList<String>(mSelectedCategories));
                user1Data.saveInBackground();

                final String challengeType = getChallengeType();
                final Challenge challenge = new Challenge(user1Data, challengeType);
                challenge.setTimeLastPlayed(new Date());
                if (challengeType.equals(Constants.ChallengeType.ONE_PLAYER)) {
                    ChallengeUserData user2Data = new ChallengeUserData(PublicUserData.getComputerPublicUserData());
                    user2Data.saveInBackground();
                    challenge.setUser2Data(user2Data);
                } else if (mSelectedOpponent.equals(Constants.OpponentType.RANDOM)) {
                    chooseRandomOpponentAndNavigate(challenge);
                    return null;
                } else if (mOpponentBaseUserId != null && !mOpponentBaseUserId.equals("")) { // Opponent has already been chosen
                    setOpponentInChallenge(challenge);
                }

                return challenge.saveInBackground().continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        challenge.pinInBackground(challenge.getObjectId());
                        Exception e = task.getError();
                        if (e == null) {
                            if (challengeType.equals(Constants.ChallengeType.PRACTICE)) {
                                savePracticeChallenge(challenge, user1PublicUserData);
                            } else if (challengeType.equals(Constants.ChallengeType.ONE_PLAYER)) {
                                navigateToChooseBoardConfigurationActivity(challenge.getObjectId(), 1);
                            } else if (mOpponentBaseUserId != null && !mOpponentBaseUserId.equals("")) { // Opponent has already been chosen
                                navigateToChooseBoardConfigurationActivity(challenge.getObjectId(), 1);
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

    private void chooseRandomOpponentAndNavigate(final Challenge challenge) {
        final String baseUserId = PublicUserData.getPublicUserData().getBaseUserId();
        Map<String, String> params = new HashMap<String, String>();
        params.put("baseUserId", baseUserId);
        ParseCloud.callFunctionInBackground(Constants.CloudCodeFunction.GET_RANDOM_OPPONENT, params).continueWithTask(new Continuation<Object, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Object> task) throws Exception {
                if (task.isFaulted()) {
                    task.getError().printStackTrace();
                }
                PublicUserData opponentPublicUserData = (PublicUserData) task.getResult();
                ChallengeUserData opponentChallengeUserData = new ChallengeUserData(opponentPublicUserData);
                challenge.setUser2Data(opponentChallengeUserData);
                challenge.setCurTurnUserId(opponentPublicUserData.getBaseUserId());
                challenge.setOtherTurnUserId(baseUserId);
                return challenge.saveInBackground();
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                challenge.pinInBackground(challenge.getObjectId());
                navigateToChooseBoardConfigurationActivity(challenge.getObjectId(), 1);
                return null;
            }
        });
    }

    private void setOpponentInChallenge(Challenge challenge) {
        while(mUser2Data == null) {}
        mUser2Data.saveInBackground();
        challenge.setUser2Data(mUser2Data);
        challenge.setCurTurnUserId(mOpponentBaseUserId);
        challenge.setOtherTurnUserId(ParseUser.getCurrentUser().getObjectId());
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

    private void showOrHideNextButton() {
        if (mSelectedCategories.size() == 0 && mViewFlipper.getDisplayedChild() == 0) {
            mViewFlipper.setDisplayedChild(1);
        } else if (mSelectedCategories.size() > 0 && mViewFlipper.getDisplayedChild() == 1) {
            mViewFlipper.setDisplayedChild(0);
        }
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
