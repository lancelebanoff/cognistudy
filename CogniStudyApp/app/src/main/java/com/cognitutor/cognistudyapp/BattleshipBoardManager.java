package com.cognitutor.cognistudyapp;

import android.app.Activity;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;

/**
 * Created by Lance on 1/5/2016.
 */
public class BattleshipBoardManager {

    private GridLayout mShipsGridLayout;
    private GridLayout mBoardSpacesGridLayout;
    private boolean[][] mSpaceIsOccupied;
    private Activity mActivity;

    public BattleshipBoardManager(Activity activity) {
        mActivity = activity;
    }

    public void setShipsGridLayout(GridLayout shipsGridLayout) {
        mShipsGridLayout = shipsGridLayout;
        mShipsGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mShipsGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
    }

    public void setBoardSpacesGridLayout(GridLayout boardSpacesGridLayout) {
        mBoardSpacesGridLayout = boardSpacesGridLayout;
        mBoardSpacesGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mBoardSpacesGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
    }

    public void drawBoardSpaces() {
        for(int row = 0; row < mBoardSpacesGridLayout.getRowCount(); row++) {
            for(int col = 0; col < mBoardSpacesGridLayout.getColumnCount(); col++) {
                ImageView imgSpace = new ImageView(mActivity);
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

    public void placeShips() {
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
                ImageView imgSpace = new ImageView(mActivity);
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
        ImageView imgShip = new ImageView(mActivity);
        imgShip.setImageResource(shipDrawableId);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(shipRow, shipHeight);
        layoutParams.columnSpec = GridLayout.spec(shipColumn, shipWidth);
        layoutParams.height = mShipsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS * shipHeight;
        layoutParams.width = mShipsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS * shipWidth;
        imgShip.setLayoutParams(layoutParams);
        mShipsGridLayout.addView(imgShip);
    }
}
