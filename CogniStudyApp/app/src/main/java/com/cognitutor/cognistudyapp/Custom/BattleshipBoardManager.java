package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.GameBoard;
import com.cognitutor.cognistudyapp.R;

import java.util.ArrayList;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Lance on 1/5/2016.
 */
public class BattleshipBoardManager {

    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private boolean[][] mSpaceIsOccupied;
    private Activity mActivity;
    private Challenge mChallenge;
    private ChallengeUserData mChallengeUserData;
    private GameBoard mGameBoard;
    private ArrayList<ShipData> mShipDatas;
    private String[][] mBoardPositionStatus;
    private boolean mCanBeAttacked;
    private GifImageView gifImageView;

    public BattleshipBoardManager(Activity activity, Challenge challenge,
                                  ChallengeUserData challengeUserData, GameBoard gameBoard,
                                  boolean canBeAttacked) {
        mActivity = activity;
        mCanBeAttacked = canBeAttacked;
        mChallenge = challenge;
        mChallengeUserData = challengeUserData;
        mGameBoard = gameBoard;
        retrieveShipDatas();
        retrieveBoardPositionStatus();
    }

    public void setShipsGridLayout(GridLayout shipsGridLayout) {
        mShipsGridLayout = shipsGridLayout;
        mShipsGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mShipsGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
    }

    public void setTargetsGridLayout(GridLayout boardSpacesGridLayout) {
        mTargetsGridLayout = boardSpacesGridLayout;
        mTargetsGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mTargetsGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
    }

    public void drawAllEmptyTargets() {
        for(int row = 0; row < mTargetsGridLayout.getRowCount(); row++) {
            for(int col = 0; col < mTargetsGridLayout.getColumnCount(); col++) {
                ImageView imgSpace = new ImageView(mActivity);
                imgSpace.setImageResource(R.drawable.target_default_unknown);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.height = mTargetsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS;
                layoutParams.width = mTargetsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS;
                layoutParams.columnSpec = GridLayout.spec(col);
                layoutParams.rowSpec = GridLayout.spec(row);
                imgSpace.setLayoutParams(layoutParams);
                mTargetsGridLayout.addView(imgSpace);
            }
        }
    }

    public void initializeTargets() {
        for(int row = 0; row < mTargetsGridLayout.getRowCount(); row++) {
            for(int col = 0; col < mTargetsGridLayout.getColumnCount(); col++) {
                // Draw target
                ImageView imgSpace = new ImageView(mActivity);
                setTargetImageResource(imgSpace, mBoardPositionStatus[row][col]);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.height = mTargetsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS;
                layoutParams.width = mTargetsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS;
                layoutParams.columnSpec = GridLayout.spec(col);
                layoutParams.rowSpec = GridLayout.spec(row);
                imgSpace.setLayoutParams(layoutParams);
                mTargetsGridLayout.addView(imgSpace);

                // Set what happens when target is clicked
                if(mCanBeAttacked) {
                    setTargetOnClickListener(imgSpace, row, col);
                }
            }
        }
    }

    public void placeShips() {
        mSpaceIsOccupied = new boolean[Constants.GameBoard.NUM_ROWS][Constants.GameBoard.NUM_COLUMNS];
        mShipsGridLayout.removeAllViews();
        addPlaceholderSpaces();

        String[] shipTypes = shipTypes = Constants.getAllConstants(Constants.ShipType.class);

        for(String shipType : shipTypes) {
            placeShip(shipType);
        }
    }

    public void drawShips() {
        addPlaceholderSpaces();
        for(ShipData shipData : mShipDatas) {
            drawShip(shipData.shipRow, shipData.shipColumn, shipData.shipHeight, shipData.shipWidth,
                    shipData.shipDrawableId);
        }

        drawGif(2, 4, 1, 1, R.drawable.target_default_attacked);
    }

    public void drawDeadShips() {
        addPlaceholderSpaces();
        for(ShipData shipData : mShipDatas) {
            if(!shipData.shipIsAlive) {
                drawShip(shipData.shipRow, shipData.shipColumn, shipData.shipHeight, shipData.shipWidth,
                        shipData.shipDrawableId);
            }
        }
    }

    public void saveGameBoard() {
        // TODO:1 save gameboard
    }

    // Build the image filename based on the skin and position status, then set image resource
    private void setTargetImageResource(ImageView imgSpace, String positionStatus) {
        // TODO:2 get selected target skin from database
        String targetSkin = Constants.ShopItemType.SKIN_TARGET_DEFAULT;
        String skinBaseString = targetSkin.replace("SKIN_", "").toLowerCase();
        String statusBaseString = positionStatus.toLowerCase();
        String imageResourceString = skinBaseString + "_" + statusBaseString;
        int imageResourceID = mActivity.getResources().getIdentifier(imageResourceString,
                "drawable", mActivity.getPackageName());
        imgSpace.setImageResource(imageResourceID);
    }

    private void setTargetOnClickListener(final ImageView imgSpace, final int row, final int col) {
        imgSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shoot(imgSpace, row, col);
            }
        });
    }

    private void shoot(ImageView imgSpace, int row, int col) {
        switch(mBoardPositionStatus[row][col]) {
            case Constants.GameBoardPositionStatus.UNKNOWN:
            case Constants.GameBoardPositionStatus.DETECTION:
                ShipData shipThatOccupiesPosition = findShipThatOccupiesPosition(row, col);
                if(shipThatOccupiesPosition == null) {
                    mBoardPositionStatus[row][col] = Constants.GameBoardPositionStatus.MISS;
                    // TODO:2 set in database too
                }
                else {
                    mBoardPositionStatus[row][col] = Constants.GameBoardPositionStatus.HIT;
                    if(shipIsDead(shipThatOccupiesPosition)) {
                        shipThatOccupiesPosition.shipIsAlive = false;
                        drawShip(shipThatOccupiesPosition.shipRow, shipThatOccupiesPosition.shipColumn,
                                shipThatOccupiesPosition.shipHeight, shipThatOccupiesPosition.shipWidth,
                                shipThatOccupiesPosition.shipDrawableId);
                        mChallengeUserData.incrementScore();
                        mChallengeUserData.saveInBackground();
                    }
                    // TODO:2 set in database too
                }
                setTargetImageResource(imgSpace, mBoardPositionStatus[row][col]);
        }
    }

    private ShipData findShipThatOccupiesPosition(int row, int col) {
        for(ShipData shipData : mShipDatas) {
            if(row >= shipData.shipRow &&
                    row < shipData.shipRow + shipData.shipHeight &&
                    col >= shipData.shipColumn &&
                    col < shipData.shipColumn + shipData.shipWidth) {
                return shipData;
            }
        }
        return null;
    }

    private boolean shipIsDead(ShipData shipData) {
        for(int row = shipData.shipRow; row < shipData.shipRow + shipData.shipHeight; row++) {
            for(int col = shipData.shipColumn; col < shipData.shipColumn + shipData.shipWidth; col++) {
                switch(mBoardPositionStatus[row][col]) {
                    case Constants.GameBoardPositionStatus.UNKNOWN:
                    case Constants.GameBoardPositionStatus.DETECTION:
                        return false;
                }
            }
        }
        return true;
    }

    // Fill the GridLayout with placeholder ImageViews so that the cells will be the correct size
    private void addPlaceholderSpaces() {
        for(int row = 0; row < mShipsGridLayout.getRowCount(); row++) {
            for(int col = 0; col < mShipsGridLayout.getColumnCount(); col++) {
                ImageView imgSpace = new ImageView(mActivity);
                imgSpace.setImageResource(R.drawable.target_default_unknown);
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
        ShipInfo shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(shipType);

        int shipDrawableIdVertical = 0, shipDrawableIdHorizontal = 0;

        switch(shipType) {
            case Constants.ShipType.ERASER:
                shipDrawableIdVertical = R.drawable.skin_eraser_default_vert;
                shipDrawableIdHorizontal = R.drawable.skin_eraser_default_horiz;
                break;
            case Constants.ShipType.YELLOW_PENCIL:
                shipDrawableIdVertical = R.drawable.skin_yellowpencil_default_vert;
                shipDrawableIdHorizontal = R.drawable.skin_yellowpencil_default_horiz;
                break;
            case Constants.ShipType.GREEN_PENCIL:
                shipDrawableIdVertical = R.drawable.skin_greenpencil_default_vert;
                shipDrawableIdHorizontal = R.drawable.skin_greenpencil_default_horiz;
                break;
            case Constants.ShipType.PEN:
                shipDrawableIdVertical = R.drawable.skin_pen_default_vert;
                shipDrawableIdHorizontal = R.drawable.skin_pen_default_horiz;
                break;
            case Constants.ShipType.CALCULATOR:
                shipDrawableIdVertical = R.drawable.skin_calculator_default_vert;
                shipDrawableIdHorizontal = R.drawable.skin_calculator_default_horiz;
                break;
            case Constants.ShipType.RULER:
                shipDrawableIdVertical = R.drawable.skin_ruler_default_vert;
                shipDrawableIdHorizontal = R.drawable.skin_ruler_default_horiz;
                break;
        }

        // Keep trying to place the ship until a valid position is found
        boolean successfullyPlacedShip;
        do {
            successfullyPlacedShip = tryToPlaceShip(shipType, shipInfo.height, shipInfo.width,
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

    private void drawGif(int row, int col, int height, int width, int shipDrawableId) {
        gifImageView = new GifImageView(mActivity);
        gifImageView.setImageResource(shipDrawableId);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(row, height);
        layoutParams.columnSpec = GridLayout.spec(col, width);
        layoutParams.height = mShipsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS * height;
        layoutParams.width = mShipsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS * width;
        gifImageView.setLayoutParams(layoutParams);
        mShipsGridLayout.addView(gifImageView);

        GifDrawable gifDrawable = (GifDrawable) gifImageView.getDrawable();
        gifDrawable.addAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                gifImageView.setVisibility(View.GONE);
            }
        });
    }

    private void retrieveShipDatas() {
        // TODO:2 get ShipDatas from database
        mShipDatas = new ArrayList<>();
        ShipData shipData;
        ShipInfo shipInfo;

        shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(Constants.ShipType.ERASER);
        shipData = new ShipData(0, 0, shipInfo.height, shipInfo.width,
                R.drawable.skin_eraser_default_vert, true);
        mShipDatas.add(shipData);
        shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(Constants.ShipType.YELLOW_PENCIL);
        shipData = new ShipData(0, 1, shipInfo.height, shipInfo.width,
                R.drawable.skin_yellowpencil_default_vert, true);
        mShipDatas.add(shipData);
        shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(Constants.ShipType.GREEN_PENCIL);
        shipData = new ShipData(0, 2, shipInfo.height, shipInfo.width,
                R.drawable.skin_greenpencil_default_vert, true);
        mShipDatas.add(shipData);
        shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(Constants.ShipType.PEN);
        shipData = new ShipData(0, 3, shipInfo.height, shipInfo.width,
                R.drawable.skin_pen_default_vert, true);
        mShipDatas.add(shipData);
        shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(Constants.ShipType.CALCULATOR);
        shipData = new ShipData(0, 4, shipInfo.height, shipInfo.width,
                R.drawable.skin_calculator_default_vert, true);
        mShipDatas.add(shipData);
        shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(Constants.ShipType.RULER);
        shipData = new ShipData(0, 7, shipInfo.height, shipInfo.width,
                R.drawable.skin_ruler_default_vert, true);
        mShipDatas.add(shipData);
    }

    private void retrieveBoardPositionStatus() {
        // TODO:2 get board position status from database
        mBoardPositionStatus = new String[Constants.GameBoard.NUM_ROWS][Constants.GameBoard.NUM_COLUMNS];
        for(int i = 0; i < mBoardPositionStatus.length; i++) {
            for(int j = 0; j < mBoardPositionStatus[i].length; j++) {
                mBoardPositionStatus[i][j] = Constants.GameBoardPositionStatus.UNKNOWN;
            }
        }
    }

    public static class ShipData {
        public int shipRow;
        public int shipColumn;
        public int shipHeight;
        public int shipWidth;
        public int shipDrawableId;
        public boolean shipIsAlive;

        public ShipData(int shipRow, int shipColumn, int shipHeight, int shipWidth, int shipDrawableId,
                        boolean shipIsAlive) {
            this.shipRow = shipRow;
            this.shipColumn = shipColumn;
            this.shipHeight = shipHeight;
            this.shipWidth = shipWidth;
            this.shipDrawableId = shipDrawableId;
            this.shipIsAlive = shipIsAlive;
        }
    }
}
