package com.cognitutor.cognistudyapp.Activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.R;

public class BattleshipAttackActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
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

        String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        // TODO:2 user1or2
        mBattleshipBoardManager = new BattleshipBoardManager(this, null, null, true);
        // TODO:2 wait until data is retrieved
        initializeGridLayouts();
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
        // Exit ChallengeActivity and start a new one
        Intent finishActivityIntent = new Intent(Constants.IntentExtra.FINISH_CHALLENGE_ACTIVITY);
        sendBroadcast(finishActivityIntent);
        // TODO:2 send intent extras to specify which challenge it is
        Intent startActivityIntent = new Intent(this, ChallengeActivity.class);
        startActivity(startActivityIntent);
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
