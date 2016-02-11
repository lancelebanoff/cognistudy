package com.cognitutor.cognistudyapp.Activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.ChallengeUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battleship_attack);
        mIntent = getIntent();

        initializeBoard();
    }

    private void initializeBoard() {
        String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        int user1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);

        ChallengeUtils.initializeBattleshipBoardManager(this, challengeId, user1or2, true)
                .continueWith(new Continuation<BattleshipBoardManager, Void>() {
                    @Override
                    public Void then(Task<BattleshipBoardManager> task) throws Exception {
                        mBattleshipBoardManager = task.getResult();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeGridLayouts();
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

    public void onClick_btnDone(View view) {
        mBattleshipBoardManager.saveGameBoard();
        setOtherPlayerTurn();

        // Exit ChallengeActivity and start a new one
        Intent finishActivityIntent = new Intent(Constants.IntentExtra.FINISH_CHALLENGE_ACTIVITY);
        sendBroadcast(finishActivityIntent);
        Intent startActivityIntent = new Intent(this, ChallengeActivity.class);
        startActivityIntent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        startActivityIntent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        startActivity(startActivityIntent);
        finish();
    }

    private void setOtherPlayerTurn() {
        String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        Challenge.getChallenge(challengeId)
                .onSuccess(new Continuation<Challenge, Void>() {
                    @Override
                    public Void then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        String curTurnUserId = challenge.getCurTurnUserId();
                        String otherTurnUserId = challenge.getOtherTurnUserId();
                        challenge.setCurTurnUserId(otherTurnUserId);
                        challenge.setOtherTurnUserId(curTurnUserId);
                        challenge.saveInBackground();
                        return null;
                    }
                });
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
