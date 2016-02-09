package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.GameBoard;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Ship;
import com.cognitutor.cognistudyapp.R;

import java.util.ArrayList;
import java.util.List;

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
    private ArrayList<Ship> mShips;
    private ArrayList<ShipDrawableData> mShipDrawableDatas;
    private String[][] mBoardPositionStatus;
    private boolean mCanBeAttacked;

    // Used for new challenge
    public BattleshipBoardManager(Activity activity, Challenge challenge,
                                  ChallengeUserData challengeUserData, boolean canBeAttacked) {
        mActivity = activity;
        mCanBeAttacked = canBeAttacked;
        mChallenge = challenge;
        mChallengeUserData = challengeUserData;
    }

    // Used for existing challenge
    public BattleshipBoardManager(Activity activity, Challenge challenge,
                                  ChallengeUserData challengeUserData, GameBoard gameBoard,
                                  boolean canBeAttacked) {
        mActivity = activity;
        mCanBeAttacked = canBeAttacked;
        mChallenge = challenge;
        mChallengeUserData = challengeUserData;
        mGameBoard = gameBoard;
        retrieveShipDrawableDatas();
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

        mShips = new ArrayList<>();
        String[] shipTypes = Constants.getAllConstants(Constants.ShipType.class);

        for(String shipType : shipTypes) {
            placeShip(shipType);
        }
    }

    public void drawShips() {
        addPlaceholderSpaces();
        for(ShipDrawableData shipDrawableData : mShipDrawableDatas) {
            drawShip(shipDrawableData.shipRow, shipDrawableData.shipColumn, shipDrawableData.shipHeight, shipDrawableData.shipWidth,
                    shipDrawableData.shipDrawableId);
        }
    }

    public void drawDeadShips() {
        addPlaceholderSpaces();
        for(ShipDrawableData shipDrawableData : mShipDrawableDatas) {
            if(!shipDrawableData.shipIsAlive) {
                drawShip(shipDrawableData.shipRow, shipDrawableData.shipColumn, shipDrawableData.shipHeight, shipDrawableData.shipWidth,
                        shipDrawableData.shipDrawableId);
            }
        }
    }

    public void saveGameBoard() {
        mGameBoard = new GameBoard(mShips, createShipAt());
        mGameBoard.saveInBackground();
        mChallengeUserData.setGameBoard(mGameBoard);
        mChallengeUserData.saveInBackground();
    }

    public List<List<Ship>> createShipAt() {
        List<List<Ship>> shipAt = new ArrayList<>();
        for(int i = 0; i < Constants.GameBoard.NUM_ROWS; i++) {
            shipAt.add(new ArrayList<Ship>());
            for(int j = 0; j < Constants.GameBoard.NUM_COLUMNS; j++) {
                shipAt.get(i).add(null);
            }
        }
        for(Ship ship : mShips) {
            ShipDrawableData shipDrawableData = ship.getShipDrawableData();
            int minRow = shipDrawableData.shipRow;
            int maxRow = shipDrawableData.shipRow + shipDrawableData.shipHeight;
            int minCol = shipDrawableData.shipColumn;
            int maxCol = shipDrawableData.shipColumn + shipDrawableData.shipWidth;
            for (int row = minRow; row < maxRow; row++) {
                for (int col = minCol; col < maxCol; col++) {
                    shipAt.get(row).set(col, ship);
                }
            }
        }
        return shipAt;
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
                ShipDrawableData shipThatOccupiesPosition = findShipThatOccupiesPosition(row, col);
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

    private ShipDrawableData findShipThatOccupiesPosition(int row, int col) {
        for(ShipDrawableData shipDrawableData : mShipDrawableDatas) {
            if(row >= shipDrawableData.shipRow &&
                    row < shipDrawableData.shipRow + shipDrawableData.shipHeight &&
                    col >= shipDrawableData.shipColumn &&
                    col < shipDrawableData.shipColumn + shipDrawableData.shipWidth) {
                return shipDrawableData;
            }
        }
        return null;
    }

    private boolean shipIsDead(ShipDrawableData shipDrawableData) {
        for(int row = shipDrawableData.shipRow; row < shipDrawableData.shipRow + shipDrawableData.shipHeight; row++) {
            for(int col = shipDrawableData.shipColumn; col < shipDrawableData.shipColumn + shipDrawableData.shipWidth; col++) {
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
            shipOrientation = Constants.RotationType.VERTICAL;
            shipDrawableId = shipDrawableIdVertical;
        }
        else {
            shipOrientation = Constants.RotationType.HORIZONTAL;
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

        ShipDrawableData shipDrawableData = new ShipDrawableData(shipRow, shipColumn, shipHeight,
                shipWidth, shipDrawableId, true);
        drawShip(shipRow, shipColumn, shipHeight, shipWidth, shipDrawableId);

        Ship ship = new Ship(shipType, shipRow, shipColumn, shipOrientation, shipHeight * shipWidth,
                shipDrawableData);
        mShips.add(ship);

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

    private void retrieveShipDrawableDatas() {
        mShipDrawableDatas = new ArrayList<>();

        String[] shipTypes = Constants.getAllConstants(Constants.ShipType.class);
        for(String shipType : shipTypes) {
            ShipInfo shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(shipType);
            // TODO:2 get Ships from database
            ShipDrawableData shipDrawableData = null;
            mShipDrawableDatas.add(shipDrawableData);
        }
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

    public static class ShipDrawableData {
        public int shipRow;
        public int shipColumn;
        public int shipHeight;
        public int shipWidth;
        public int shipDrawableId;
        public boolean shipIsAlive;

        public ShipDrawableData(int shipRow, int shipColumn, int shipHeight, int shipWidth, int shipDrawableId,
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
