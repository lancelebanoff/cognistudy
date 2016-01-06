package com.cognitutor.cognistudyapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageView;

public class ChooseBoardConfigurationActivity extends CogniActivity {

    private boolean[][] mSpaceIsOccupied;
    private GridLayout mShipsGridLayout;
    private GridLayout mBoardSpacesGridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_board_configuration);
        // TODO:2 Delete challenge from database when hitting back button

        initializeGridLayouts();
    }

    public void onClick_Randomize(View view) {
        placeShips();
    }

    private void initializeGridLayouts() {
        mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
        mShipsGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mShipsGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
        ViewTreeObserver observer = mShipsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                placeShips();
                removeOnGlobalLayoutListener(mShipsGridLayout, this);
            }
        });

        mBoardSpacesGridLayout = (GridLayout) findViewById(R.id.boardSpacesGridLayout);
        mBoardSpacesGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mBoardSpacesGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
        observer = mBoardSpacesGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                drawBoardSpaces();
                removeOnGlobalLayoutListener(mBoardSpacesGridLayout, this);
            }
        });
    }

    private void drawBoardSpaces() {
        for(int row = 0; row < mBoardSpacesGridLayout.getRowCount(); row++) {
            for(int col = 0; col < mBoardSpacesGridLayout.getColumnCount(); col++) {
                ImageView imgSpace = new ImageView(this);
                imgSpace.setImageResource(R.drawable.gameboard_space);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.height = mBoardSpacesGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS;
                layoutParams.width = mBoardSpacesGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS;
                layoutParams.columnSpec = GridLayout.spec(col);
                layoutParams.rowSpec = GridLayout.spec(row);
                imgSpace.setLayoutParams(layoutParams);
                mBoardSpacesGridLayout.addView(imgSpace);
            }
        }
    }

    private void placeShips() {
        mSpaceIsOccupied = new boolean[Constants.GameBoard.NUM_ROWS][Constants.GameBoard.NUM_COLUMNS];
        mShipsGridLayout.removeAllViews();
        addPlaceholderSpaces();

        String[] shipTypes = new String[0];
        try {
            shipTypes = Constants.getAllConstants(Constants.ShipType.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        for(String shipType : shipTypes) {
            placeShip(shipType);
        }
    }

    // Fill the GridLayout with placeholder ImageViews so that the cells will be the correct size
    private void addPlaceholderSpaces() {
        for(int row = 0; row < mShipsGridLayout.getRowCount(); row++) {
            for(int col = 0; col < mShipsGridLayout.getColumnCount(); col++) {
                ImageView imgSpace = new ImageView(this);
                imgSpace.setImageResource(R.drawable.gameboard_space);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.height = mShipsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS;
                layoutParams.width = mShipsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS;
                layoutParams.columnSpec = GridLayout.spec(col);
                layoutParams.rowSpec = GridLayout.spec(row);
                imgSpace.setLayoutParams(layoutParams);
                mShipsGridLayout.addView(imgSpace);
            }
        }
    }

    private void placeShip(String shipType) {
        // TODO:2 get ship height and width from database
        int shipHeight = 0;
        int shipWidth = 0;
        int shipDrawableIdVertical = 0;
        int shipDrawableIdHorizontal = 0;

        switch(shipType) {
            case Constants.ShipType.ERASER:
                shipHeight = 2;
                shipWidth = 1;
                shipDrawableIdVertical = R.drawable.eraser;
                shipDrawableIdHorizontal = R.drawable.eraser_horiz;
                break;
            case Constants.ShipType.YELLOW_PENCIL:
                shipHeight = 3;
                shipWidth = 1;
                shipDrawableIdVertical = R.drawable.yellow_pencil;
                shipDrawableIdHorizontal = R.drawable.yellow_pencil_horiz;
                break;
            case Constants.ShipType.GREEN_PENCIL:
                shipHeight = 3;
                shipWidth = 1;
                shipDrawableIdVertical = R.drawable.green_pencil;
                shipDrawableIdHorizontal = R.drawable.green_pencil_horiz;
                break;
            case Constants.ShipType.PEN:
                shipHeight = 4;
                shipWidth = 1;
                shipDrawableIdVertical = R.drawable.pen;
                shipDrawableIdHorizontal = R.drawable.pen_horiz;
                break;
            case Constants.ShipType.CALCULATOR:
                shipHeight = 2;
                shipWidth = 2;
                shipDrawableIdVertical = R.drawable.calculator;
                shipDrawableIdHorizontal = R.drawable.calculator_horiz;
                break;
            case Constants.ShipType.RULER:
                shipHeight = 5;
                shipWidth = 1;
                shipDrawableIdVertical = R.drawable.ruler;
                shipDrawableIdHorizontal = R.drawable.ruler_horiz;
                break;
        }

        boolean successfullyPlacedShip;
        do {
            successfullyPlacedShip = tryToPlaceShip(shipType, shipHeight, shipWidth,
                    shipDrawableIdVertical, shipDrawableIdHorizontal);
        }
        while(!successfullyPlacedShip);
    }

    private boolean tryToPlaceShip(String shipType, int shipHeight, int shipWidth,
                                   int shipDrawableIdVertical, int shipDrawableIdHorizontal) {
        String shipOrientation;
        int shipRow, shipColumn, shipDrawableId;

        // Choose ship orientation
        int shipOrientationRandomizer = (int) (Math.random() * 2);
        if(shipOrientationRandomizer == 0) {
            shipOrientation = Constants.ShipAttribute.Orientation.VERTICAL;
            shipDrawableId = shipDrawableIdVertical;
        }
        else {
            shipOrientation = Constants.ShipAttribute.Orientation.HORIZONTAL;
            shipDrawableId = shipDrawableIdHorizontal;
            // Swap height and width
            int temp = shipHeight;
            shipHeight = shipWidth;
            shipWidth = temp;
        }

        // Choose ship position
        int maxRow = Constants.GameBoard.NUM_ROWS - shipHeight;
        int maxColumn = Constants.GameBoard.NUM_COLUMNS - shipWidth;
        shipRow = (int) (Math.random() * (maxRow + 1));
        shipColumn = (int) (Math.random() * (maxColumn + 1));

        // If the ship would be placed on an occupied space, then restart and try a new position
        for(int row = shipRow; row < shipRow + shipHeight; row++) {
            for(int col = shipColumn; col < shipColumn + shipWidth; col++) {
                if(mSpaceIsOccupied[row][col]) {
                    return false;
                }
            }
        }
        // Set all of the ship's spaces as occupied
        for(int row = shipRow; row < shipRow + shipHeight; row++) {
            for(int col = shipColumn; col < shipColumn + shipWidth; col++) {
                mSpaceIsOccupied[row][col] = true;
            }
        }

        Log.i("Placing ships", shipType + " (" + shipRow + ", " + shipColumn + ") " + shipOrientation);

        // TODO:2 put ship attributes into database

        drawShip(shipRow, shipColumn, shipHeight, shipWidth, shipDrawableId);

        return true;
    }

    private void drawShip(int shipRow, int shipColumn, int shipHeight, int shipWidth, int shipDrawableId) {
        ImageView imgShip = new ImageView(this);
        imgShip.setImageResource(shipDrawableId);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(shipRow, shipHeight);
        layoutParams.columnSpec = GridLayout.spec(shipColumn, shipWidth);
        layoutParams.height = mShipsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS * shipHeight;
        layoutParams.width = mShipsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS * shipWidth;
        imgShip.setLayoutParams(layoutParams);
        mShipsGridLayout.addView(imgShip);
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
