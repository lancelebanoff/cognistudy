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
import com.parse.ParseObject;

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
    private ChallengeUserData mOtherUserData;
    private GameBoard mGameBoard;
    private ArrayList<Ship> mShips;
    private ArrayList<ShipDrawableData> mShipDrawableDatas;
    private List<List<String>> mBoardPositionStatus;
    private boolean mCanBeAttacked;
    private int mNumShotsRemaining;

    // Used for new challenge
    public BattleshipBoardManager(Activity activity, Challenge challenge,
                                  ChallengeUserData challengeUserData, boolean canBeAttacked) {
        mActivity = activity;
        mCanBeAttacked = canBeAttacked;
        mChallenge = challenge;
        mNumShotsRemaining = challenge.getNumShotsRemaining();
        mChallengeUserData = challengeUserData;
    }

    // Used for existing challenge
    public BattleshipBoardManager(Activity activity, Challenge challenge,
                                  ChallengeUserData challengeUserData, ChallengeUserData otherUserData,
                                  GameBoard gameBoard, List<Ship> ships, boolean canBeAttacked) {
        mActivity = activity;
        mCanBeAttacked = canBeAttacked;
        mChallenge = challenge;
        mNumShotsRemaining = challenge.getNumShotsRemaining();
        mChallengeUserData = challengeUserData;
        mOtherUserData = otherUserData;
        mGameBoard = gameBoard;
        mShips = (ArrayList<Ship>) ships;
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
                setTargetImageResource(imgSpace, mBoardPositionStatus.get(row).get(col));
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
        for(Ship ship : mShips) {
            ShipDrawableData shipDrawableData = ship.getShipDrawableData();
            drawShip(shipDrawableData);
        }
    }

    public void drawDeadShips() {
        addPlaceholderSpaces();
        for(ShipDrawableData shipDrawableData : mShipDrawableDatas) {
            if(!shipDrawableData.shipIsAlive) {
                drawShip(shipDrawableData);
            }
        }
    }

    public void saveNewGameBoard() {
        mGameBoard = new GameBoard(mShips, createShipAt());
        mGameBoard.saveInBackground();
        mChallengeUserData.setGameBoard(mGameBoard);
        mChallengeUserData.saveInBackground();
        ParseObject.saveAllInBackground(mShips);
    }

    public void saveGameBoard() {
        mGameBoard.setStatus(mBoardPositionStatus);
        mGameBoard.saveInBackground();
        mChallengeUserData.saveInBackground();
        ParseObject.saveAllInBackground(mShips);
    }

    public List<List<Ship>> createShipAt() {
        // Initialize each space as null
        List<List<Ship>> shipAt = new ArrayList<>();
        for(int i = 0; i < Constants.GameBoard.NUM_ROWS; i++) {
            shipAt.add(new ArrayList<Ship>());
            for(int j = 0; j < Constants.GameBoard.NUM_COLUMNS; j++) {
                shipAt.get(i).add(null);
            }
        }
        // Each space points to a ship or null
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

    public int[] getScores() {
        return new int[] { mChallengeUserData.getScore(), mOtherUserData.getScore() };
    }

    public void clearImages() {
        mTargetsGridLayout.removeAllViews();
        mShipsGridLayout.removeAllViews();
    }

    // Build the image filename based on the skin and position status, then set image resource
    private void setTargetImageResource(ImageView imgSpace, String positionStatus) {
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
                if (mNumShotsRemaining > 0) {
                    mNumShotsRemaining--;
                    shoot(imgSpace, row, col);
                }
            }
        });
    }

    private void shoot(ImageView imgSpace, int row, int col) {

        switch(mBoardPositionStatus.get(row).get(col)) {

            // If you already attacked the space, then give the shot back and do nothing
            case Constants.GameBoardPositionStatus.HIT:
            case Constants.GameBoardPositionStatus.MISS:
                mNumShotsRemaining++;
                break;

            // If you haven't attacked the space yet, then change board position status
            case Constants.GameBoardPositionStatus.UNKNOWN:
            case Constants.GameBoardPositionStatus.DETECTION:
                if(mNumShotsRemaining == 0) {
                    setOtherPlayerTurn();
                }
                Ship shipThatOccupiesPosition = findShipThatOccupiesPosition(row, col);
                if(shipThatOccupiesPosition == null) {
                    mBoardPositionStatus.get(row).set(col, Constants.GameBoardPositionStatus.MISS);
                }
                else {
                    mBoardPositionStatus.get(row).set(col, Constants.GameBoardPositionStatus.HIT);
                    shipThatOccupiesPosition.decrementHitsRemaining();
                    if(shipThatOccupiesPosition.getHitsRemaining() == 0) {
                        shipThatOccupiesPosition.getShipDrawableData().shipIsAlive = false;
                        drawShip(shipThatOccupiesPosition.getShipDrawableData());
                        mChallengeUserData.incrementScore();
                        mChallengeUserData.saveInBackground();
                    }
                }
                setTargetImageResource(imgSpace, mBoardPositionStatus.get(row).get(col));
                break;
        }
    }

    private void setOtherPlayerTurn() {
        String curTurnUserId = mChallenge.getCurTurnUserId();
        String otherTurnUserId = mChallenge.getOtherTurnUserId();
        mChallenge.setCurTurnUserId(otherTurnUserId);
        mChallenge.setOtherTurnUserId(curTurnUserId);
        // TODO:2 set numShotsRemaining after answering questions
        mChallenge.setNumShotsRemaining(4);
        mChallenge.saveInBackground();
    }

    private Ship findShipThatOccupiesPosition(int row, int col) {
        for(ShipDrawableData shipDrawableData : mShipDrawableDatas) {
            if(row >= shipDrawableData.shipRow &&
                    row < shipDrawableData.shipRow + shipDrawableData.shipHeight &&
                    col >= shipDrawableData.shipColumn &&
                    col < shipDrawableData.shipColumn + shipDrawableData.shipWidth) {
                return shipDrawableData.ship;
            }
        }
        return null;
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

        // Keep trying to place the ship until a valid position is found
        boolean successfullyPlacedShip;
        do {
            successfullyPlacedShip = tryToPlaceShip(shipType, shipInfo.height, shipInfo.width);
        }
        while(!successfullyPlacedShip);
    }

    private boolean tryToPlaceShip(String shipType, int shipHeight, int shipWidth) {
        String shipOrientation;
        int shipRow, shipColumn, shipDrawableId;

        // Choose ship orientation
        int shipOrientationRandomizer = (int) (Math.random() * 2);
        if(shipOrientationRandomizer == 0) {
            shipOrientation = Constants.RotationType.VERTICAL;
        }
        else {
            shipOrientation = Constants.RotationType.HORIZONTAL;
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

        Ship ship = new Ship(shipType, shipRow, shipColumn, shipOrientation, shipHeight * shipWidth);
        mShips.add(ship);

        ShipDrawableData shipDrawableData = new ShipDrawableData(ship);
        ship.setShipDrawableData(shipDrawableData);
        drawShip(shipDrawableData);

        return true;
    }

    private void drawShip(ShipDrawableData shipDrawableData) {
        ImageView imgShip = new ImageView(mActivity);
        imgShip.setImageResource(shipDrawableData.shipDrawableId);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(shipDrawableData.shipRow, shipDrawableData.shipHeight);
        layoutParams.columnSpec = GridLayout.spec(shipDrawableData.shipColumn, shipDrawableData.shipWidth);
        layoutParams.height = mShipsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS * shipDrawableData.shipHeight;
        layoutParams.width = mShipsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS * shipDrawableData.shipWidth;
        imgShip.setLayoutParams(layoutParams);
        mShipsGridLayout.addView(imgShip);
    }

    private void retrieveShipDrawableDatas() {
        mShipDrawableDatas = new ArrayList<>();
        for(Ship ship : mShips) {
            ShipDrawableData shipDrawableData = new ShipDrawableData(ship);
            ship.setShipDrawableData(shipDrawableData);
            mShipDrawableDatas.add(shipDrawableData);
        }
    }

    private void retrieveBoardPositionStatus() {
        mBoardPositionStatus = mGameBoard.getStatus();
    }

    public static class ShipDrawableData {
        public Ship ship;
        public int shipRow;
        public int shipColumn;
        public int shipHeight;
        public int shipWidth;
        public int shipDrawableId;
        public boolean shipIsAlive;

        public ShipDrawableData(Ship ship) {
            this.ship = ship;
            shipRow = ship.getStartRow();
            shipColumn = ship.getStartColumn();
            shipDrawableId = retrieveDrawableId(ship.getShipType(), ship.getRotation());
            shipIsAlive = ship.getHitsRemaining() > 0;

            ShipInfo shipInfo = QS_ShipInfo.ShipTypeToShipInfo.get(ship.getShipType());
            switch(ship.getRotation()) {
                case Constants.RotationType.VERTICAL:
                    shipHeight = shipInfo.height;
                    shipWidth = shipInfo.width;
                    break;
                case Constants.RotationType.HORIZONTAL:
                    shipHeight = shipInfo.width;
                    shipWidth = shipInfo.height;
                    break;
            }
        }

        private int retrieveDrawableId(String shipType, String orientation) {

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

            switch(orientation) {
                case Constants.RotationType.HORIZONTAL:
                    return shipDrawableIdHorizontal;
                case Constants.RotationType.VERTICAL:
                    return shipDrawableIdVertical;
                default:
                    return -1;
            }
        }
    }
}
