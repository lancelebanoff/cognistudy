package com.cognitutor.cognistudyapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;

public class ChooseBoardConfigurationActivity extends CogniActivity {

    private GridLayout mShipsGridLayout;
    private GridLayout mBoardSpacesGridLayout;
    private BattleshipBoardManager mBattleshipBoardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_board_configuration);
        // TODO:2 Delete challenge from database when hitting back button

        mBattleshipBoardManager = new BattleshipBoardManager(this);
        initializeGridLayouts();
    }

    public void onClick_Randomize(View view) {
        mBattleshipBoardManager.placeShips();
    }

    private void initializeGridLayouts() {
        mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
        mBattleshipBoardManager.setShipsGridLayout(mShipsGridLayout);
        ViewTreeObserver observer = mShipsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.placeShips();
                removeOnGlobalLayoutListener(mShipsGridLayout, this);
            }
        });

        mBoardSpacesGridLayout = (GridLayout) findViewById(R.id.boardSpacesGridLayout);
        mBattleshipBoardManager.setBoardSpacesGridLayout(mBoardSpacesGridLayout);
        observer = mBoardSpacesGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.drawBoardSpaces();
                removeOnGlobalLayoutListener(mBoardSpacesGridLayout, this);
            }
        });
    }

    public void navigateToChallengeActivity(View view) {
        Intent intent = new Intent(this, ChallengeActivity.class);
        startActivity(intent);
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
