package com.cognitutor.cognistudyapp.Activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.ChallengeUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseFile;

import bolts.Continuation;
import bolts.Task;

public class BattleshipAttackActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;

    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private BattleshipBoardManager mBattleshipBoardManager;
    private boolean mScoresHaveBeenLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battleship_attack);
        mIntent = getIntent();
        mScoresHaveBeenLoaded = false;

        // Exit ChallengeActivity
        Intent finishActivityIntent = new Intent(Constants.IntentExtra.FINISH_CHALLENGE_ACTIVITY);
        sendBroadcast(finishActivityIntent);

        initializeBoard();
    }

    private void initializeBoard() {
        String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        int currentUser1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        int opponentUser1or2 = currentUser1or2 == 1 ? 2: 1;

        ChallengeUtils.initializeBattleshipBoardManager(this, challengeId, currentUser1or2, opponentUser1or2, true)
                .continueWith(new Continuation<BattleshipBoardManager, Void>() {
                    @Override
                    public Void then(Task<BattleshipBoardManager> task) throws Exception {
                        mBattleshipBoardManager = task.getResult();

                        TextView txtNumShotsRemaining = (TextView) findViewById(R.id.txtNumShotsRemaining);
                        mBattleshipBoardManager.startShowingNumShotsRemaining(txtNumShotsRemaining);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeGridLayouts();
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

    private void initializeGridLayouts() {
        mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
        mBattleshipBoardManager.setShipsGridLayout(mShipsGridLayout);
        ViewTreeObserver observer = mShipsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.drawDeadShips();
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

    @Override
    public void onBackPressed() {
        mBattleshipBoardManager.saveGameBoard();
        super.onBackPressed();
    }

    public void onClick_btnDone(View view) {
        mBattleshipBoardManager.saveGameBoard();

        // Refresh Challenge list
        Intent refreshIntent = new Intent(Constants.IntentExtra.REFRESH_CHALLENGE_LIST);
        refreshIntent.putExtra(Constants.IntentExtra.REFRESH_CHALLENGE_LIST, true);
        sendBroadcast(refreshIntent);

        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }
}
