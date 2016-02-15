package com.cognitutor.cognistudyapp.Activities;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.ChallengeUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseFile;

import bolts.Continuation;
import bolts.Task;

public class ChallengeActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;

    private int mUser1or2;
    private int mViewingUser1or2;
    private boolean mScoresHaveBeenLoaded;
    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private BroadcastReceiver mBroadcastReceiver;

    private BattleshipBoardManager mBattleshipBoardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);
        mIntent = getIntent();
        initializeBroadcastReceiver();

        mUser1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        mViewingUser1or2 = mUser1or2;
        mScoresHaveBeenLoaded = false;

        initializeBoard(mViewingUser1or2);
        showOrHideYourTurnButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        showOrHideYourTurnButton();
    }

    private void initializeBoard(final int viewingUser1or2) {
        String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);

        ChallengeUtils.initializeBattleshipBoardManager(this, challengeId, viewingUser1or2, false)
                .continueWith(new Continuation<BattleshipBoardManager, Void>() {
                    @Override
                    public Void then(Task<BattleshipBoardManager> task) throws Exception {
                        mBattleshipBoardManager = task.getResult();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeGridLayouts(viewingUser1or2);
                                if(!mScoresHaveBeenLoaded) {
                                    showScores();
                                    showProfilePictures();
                                    mScoresHaveBeenLoaded = true;
                                }
                            }
                        });

                        return null;
                    }
                });
    }

    private void showOrHideYourTurnButton() {
        String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);

        Challenge.getChallenge(challengeId)
                .onSuccess(new Continuation<Challenge, Void>() {
                    @Override
                    public Void then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        String currentUserId = PublicUserData.getPublicUserData().getBaseUserId();
                        boolean isCurrentUsersTurn = challenge.getCurTurnUserId().equals(currentUserId);
                        Button btnYourTurn = (Button) findViewById(R.id.btnYourTurn);
                        if (isCurrentUsersTurn) {
                            btnYourTurn.setVisibility(View.VISIBLE);
                        } else {
                            btnYourTurn.setVisibility(View.INVISIBLE);
                        }
                        return null;
                    }
                });
    }

    private void initializeGridLayouts(final int viewingUser1or2) {
        mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
        mBattleshipBoardManager.setShipsGridLayout(mShipsGridLayout);
        ViewTreeObserver observer = mShipsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(viewingUser1or2 == mUser1or2) {
                    mBattleshipBoardManager.drawShips();
                }
                else {
                    mBattleshipBoardManager.drawDeadShips();
                }
                removeOnGlobalLayoutListener(mShipsGridLayout, this);
            }
        });

        mTargetsGridLayout = (GridLayout) findViewById(R.id.targetsGridLayout);
        mBattleshipBoardManager.setTargetsGridLayout(mTargetsGridLayout);
        observer = mTargetsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.initializeTargets();
                removeOnGlobalLayoutListener(mTargetsGridLayout, this);
            }
        });
    }

    private void showScores() {
        TextView txtScore = (TextView) findViewById(R.id.txtScore);
        int[] scores = mBattleshipBoardManager.getScores();
        txtScore.setText(scores[0] + " - " + scores[1]);
    }

    private void showProfilePictures() {
        ParseFile[] parseFiles = mBattleshipBoardManager.getProfilePictures();
        RoundedImageView img1 = (RoundedImageView) findViewById(R.id.imgProfileRounded1);
        RoundedImageView img2 = (RoundedImageView) findViewById(R.id.imgProfileRounded2);
        img1.setParseFile(parseFiles[0]);
        img1.loadInBackground();
        img2.setParseFile(parseFiles[1]);
        img2.loadInBackground();
    }

    public void onClick_btnSwitchView(View view) {
        mBattleshipBoardManager.clearImages();

        mViewingUser1or2 = mViewingUser1or2 == 1 ? 2 : 1;
        initializeBoard(mViewingUser1or2);
    }

    public void navigateToQuestionActivity(View view) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        intent.putExtra(Constants.IntentExtra.QUESTION_ID, "aSVEaMqEfB"); //TODO: Replace with desired questionId
//        fF4lsHt2iW
//        eO4TCrdBdn
        startActivity(intent);
    }

    public void navigateToQuestionHistoryActivity(View view) {
        Intent intent = new Intent(this, QuestionHistoryActivity.class);
        startActivity(intent);
    }

    public void navigateToChallengeAnalyticsActivity(View view) {
        Intent intent = new Intent(this, ChallengeAnalyticsActivity.class);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    // Exits this activity when finishing BattleshipAttackActivity
    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Constants.IntentExtra.FINISH_CHALLENGE_ACTIVITY)) {
                    finish();
                }
            }
        };
        registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.IntentExtra.FINISH_CHALLENGE_ACTIVITY));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
