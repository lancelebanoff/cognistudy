package com.cognitutor.cognistudyapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;

public class ChallengeActivity extends CogniActivity {

    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private BattleshipBoardManager mBattleshipBoardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        mBattleshipBoardManager = new BattleshipBoardManager(this);
        initializeGridLayouts();
    }

    private void initializeGridLayouts() {
        mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
        mBattleshipBoardManager.setShipsGridLayout(mShipsGridLayout);
        ViewTreeObserver observer = mShipsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.drawShips();
                removeOnGlobalLayoutListener(mShipsGridLayout, this);
            }
        });

        mTargetsGridLayout = (GridLayout) findViewById(R.id.targetsGridLayout);
        mBattleshipBoardManager.setTargetsGridLayout(mTargetsGridLayout);
        observer = mTargetsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.drawTargets();
                removeOnGlobalLayoutListener(mTargetsGridLayout, this);
            }
        });
    }

    public void navigateToQuestionActivity(View view) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Constants.Extra.ParentActivity.PARENT_ACTIVITY, Constants.Extra.ParentActivity.CHALLENGE_ACTIVITY);
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
}
