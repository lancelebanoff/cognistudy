package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.Collections;

public class ChooseBoardConfigurationActivity extends CogniActivity {

    private boolean[][] mBoardSpaceIsOccupied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_board_configuration);
        // TODO:2 Delete challenge from database when hitting back button
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        randomizeBoard();
    }

    private void randomizeBoard() {
        mBoardSpaceIsOccupied = new boolean[Constants.GameBoard.NUM_ROWS][Constants.GameBoard.NUM_COLUMNS];
        String[] shipTypes = new String[0];
        try {
            shipTypes = Constants.getAllConstants(Constants.ShipType.class);
            Collections.reverse(Arrays.asList(shipTypes));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        for(String shipType : shipTypes) {
            placeShip(shipType);
        }
    }

    private void placeShip(String shipType) {
        // TODO:2 get ship height and width from database
        int shipHeight = 0;
        int shipWidth = 0;
        int shipDrawableId = 0;
        switch(shipType) {
            case Constants.ShipType.ERASER:
                shipHeight = 2;
                shipWidth = 1;
                shipDrawableId = R.drawable.eraser;
                break;
            case Constants.ShipType.YELLOW_PENCIL:
                shipHeight = 3;
                shipWidth = 1;
                shipDrawableId = R.drawable.yellow_pencil;
                break;
            case Constants.ShipType.GREEN_PENCIL:
                shipHeight = 3;
                shipWidth = 1;
                shipDrawableId = R.drawable.green_pencil;
                break;
            case Constants.ShipType.PEN:
                shipHeight = 4;
                shipWidth = 1;
                shipDrawableId = R.drawable.pen;
                break;
            case Constants.ShipType.CALCULATOR:
                shipHeight = 2;
                shipWidth = 2;
                shipDrawableId = R.drawable.calculator;
                break;
            case Constants.ShipType.RULER:
                shipHeight = 5;
                shipWidth = 1;
                shipDrawableId = R.drawable.ruler;
                break;
        }

        boolean successfullyPlacedShip;
        do {
            successfullyPlacedShip = tryToPlaceShip(shipType, shipHeight, shipWidth, shipDrawableId);
        }
        while(!successfullyPlacedShip);
    }

    private boolean tryToPlaceShip(String shipType, int shipHeight, int shipWidth, int shipDrawableId) {
        String shipOrientation = "";
        int shipRow = -1;
        int shipColumn = -1;

        // Choose ship orientation and position
        int shipOrientationRandomizer = (int) (Math.random() * 2);
        if(shipOrientationRandomizer == 0) {
            shipOrientation = Constants.ShipAttribute.Orientation.VERTICAL;
            int maxRow = Constants.GameBoard.NUM_ROWS - shipHeight;
            int maxColumn = Constants.GameBoard.NUM_COLUMNS - shipWidth;
            shipRow = (int) (Math.random() * (maxRow + 1));
            shipColumn = (int) (Math.random() * (maxColumn + 1));

            // If the ship would be placed on an occupied space, then try a new position
            for(int row = shipRow; row < shipRow + shipHeight; row++) {
                for(int col = shipColumn; col < shipColumn + shipWidth; col++) {
                    if(mBoardSpaceIsOccupied[row][col]) {
                        return false;
                    }
                }
            }
            // Set all of the ship's spaces as occupied
            for(int row = shipRow; row < shipRow + shipHeight; row++) {
                for(int col = shipColumn; col < shipColumn + shipWidth; col++) {
                    mBoardSpaceIsOccupied[row][col] = true;
                }
            }
        }
        else {
            shipOrientation = Constants.ShipAttribute.Orientation.HORIZONTAL;
            int maxRow = Constants.GameBoard.NUM_ROWS - shipWidth;
            int maxColumn = Constants.GameBoard.NUM_COLUMNS - shipHeight;
            shipRow = (int) (Math.random() * (maxRow + 1));
            shipColumn = (int) (Math.random() * (maxColumn + 1));

            // If the ship would be placed on an occupied space, then try a new position
            for(int row = shipRow; row < shipRow + shipWidth; shipRow++) {
                for(int col = shipColumn; col < shipColumn + shipHeight; col++) {
                    if(mBoardSpaceIsOccupied[row][col]) {
                        return false;
                    }
                }
            }
            // Set all of the ship's spaces as occupied
            for(int row = shipRow; row < shipRow + shipWidth; shipRow++) {
                for (int col = shipColumn; col < shipColumn + shipHeight; col++) {
                    mBoardSpaceIsOccupied[row][col] = true;
                }
            }
        }

        // TODO:2 put ship attributes into database

        // Calculate size of spaces
        ImageView imgBoard = (ImageView) findViewById(R.id.imgGameBoard);
        int spaceSize = imgBoard.getDrawable().getMinimumHeight() / Constants.GameBoard.NUM_ROWS;

        // TODO:1 Draw ships

        return true;
    }

    public void navigateToChallengeActivity(View view) {
        Intent intent = new Intent(this, ChallengeActivity.class);
        startActivity(intent);
        finish();
    }
}
