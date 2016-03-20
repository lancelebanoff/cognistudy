package com.cognitutor.cognistudyapp.Activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.ChallengeUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class ChallengeActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;

    private Challenge mChallenge;
    private String mChallengeId;
    private int mCurrentUser1or2;
    private int mViewingUser1or2;
    private boolean mScoresHaveBeenLoaded;
    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private GridLayout mAnimationsGridLayout;
    private BroadcastReceiver mBroadcastReceiver;

    private BattleshipBoardManager mBattleshipBoardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);
        mIntent = getIntent();
        initializeBroadcastReceiver();

        mChallengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        mCurrentUser1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        mViewingUser1or2 = mCurrentUser1or2;
        mScoresHaveBeenLoaded = false;

        mChallenge = Challenge.getChallenge(mChallengeId);
        initializeBoard(mViewingUser1or2);

        showOrHideYourTurnButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        showOrHideYourTurnButton();
    }

    private void initializeBoard(final int viewingUser1or2) {

        hideSwitchViewButton();

        ChallengeUtils.initializeBattleshipBoardManager(this, mChallengeId, mCurrentUser1or2, viewingUser1or2, false)
                .continueWith(new Continuation<BattleshipBoardManager, Void>() {
                    @Override
                    public Void then(Task<BattleshipBoardManager> task) throws Exception {
                        mBattleshipBoardManager = task.getResult();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeGridLayouts(viewingUser1or2);
                                showSwitchViewButton();
                                if (!mScoresHaveBeenLoaded) {
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

    private void showSwitchViewButton() {
        ImageButton btnSwitch = (ImageButton) findViewById(R.id.btnSwitchView);
        btnSwitch.setVisibility(View.VISIBLE);
    }

    private void hideSwitchViewButton() {
        ImageButton btnSwitch = (ImageButton) findViewById(R.id.btnSwitchView);
        btnSwitch.setVisibility(View.INVISIBLE);
    }

    private void showOrHideYourTurnButton() {
        String currentUserId = ParseUser.getCurrentUser().getObjectId();
        boolean isCurrentUsersTurn = mChallenge.getCurTurnUserId().equals(currentUserId);
        Button btnYourTurn = (Button) findViewById(R.id.btnYourTurn);
        if (isCurrentUsersTurn && !mChallenge.getHasEnded()) {
            btnYourTurn.setVisibility(View.VISIBLE);
        } else {
            btnYourTurn.setVisibility(View.INVISIBLE);
        }
    }

    private void initializeGridLayouts(final int viewingUser1or2) {
        mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
        mBattleshipBoardManager.setShipsGridLayout(mShipsGridLayout);
        ViewTreeObserver observer = mShipsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(viewingUser1or2 == mCurrentUser1or2) {
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

        mAnimationsGridLayout = (GridLayout) findViewById(R.id.animationsGridLayout);
        mBattleshipBoardManager.setAnimationsGridLayout(mAnimationsGridLayout);
        observer = mAnimationsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.initializeAnimationsGridLayout();
                mBattleshipBoardManager.showPreviousTurn();
                removeOnGlobalLayoutListener(mAnimationsGridLayout, this);
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

    public void onClick_btnResign(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_quit_challenge)
                .setMessage(R.string.message_dialog_quit_challenge)
                .setNegativeButton(R.string.no_dialog_cancel_challenge, null)
                .setPositiveButton(R.string.yes_dialog_cancel_challenge, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        mBattleshipBoardManager.quitChallenge();
                        Button btnYourTurn = (Button) findViewById(R.id.btnYourTurn);
                        btnYourTurn.setVisibility(View.INVISIBLE);
                    }
                }).create().show();
    }

    public void onClick_btnYourTurn(View view) {
        int quesAnsThisTurn = mChallenge.getQuesAnsThisTurn();
        if (quesAnsThisTurn == Constants.Questions.NUM_QUESTIONS_PER_TURN) { // All questions have been answered
            navigateToBattleshipAttackActivity();
        } else {
            List<String> questionIds = mChallenge.getThisTurnQuestionIds();
            if (questionIds != null && questionIds.size() != 0) {
                navigateToQuestionActivity(questionIds.get(quesAnsThisTurn));
            } else {
                chooseThreeQuestionIdsThenNavigate(); // TODO:2 do this during onCreate?
            }
        }
    }

    private void chooseThreeQuestionIdsThenNavigate() {
        Question.chooseThreeQuestionIds(mChallenge, mCurrentUser1or2).continueWith(new Continuation<List<String>, Void>() {
            @Override
            public Void then(Task<List<String>> task) throws Exception {
                if(task.isFaulted()) {
                    task.getError().printStackTrace();
                    Log.e("chooseThreeQuestionIds", task.getError().getMessage());
                    return null;
                }
                List<String> questionIds = task.getResult();
                navigateToQuestionActivity(questionIds.get(0));
                return null;
            }
        });
    }

    private void navigateToQuestionActivity(String questionId) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        intent.putExtra(Constants.IntentExtra.QUESTION_ID, questionId);
//        fF4lsHt2iW
//        eO4TCrdBdn
        startActivity(intent);
    }

    private void navigateToBattleshipAttackActivity() {
        Intent intent = new Intent(this, BattleshipAttackActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mChallengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, mCurrentUser1or2);
        startActivity(intent);
    }

    public void navigateToQuestionHistoryActivity(View view) {
        Intent intent = new Intent(this, QuestionHistoryActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mChallengeId);
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
