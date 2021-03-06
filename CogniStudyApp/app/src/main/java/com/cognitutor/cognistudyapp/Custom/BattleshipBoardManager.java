package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.BattleshipAttackActivity;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.CommonUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.GameBoard;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Ship;
import com.cognitutor.cognistudyapp.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import bolts.Continuation;
import bolts.Task;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Lance on 1/5/2016.
 */
public class BattleshipBoardManager {

    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private GridLayout mAnimationsGridLayout;
    private boolean[][] mSpaceIsOccupied;
    private Activity mActivity;
    private Challenge mChallenge;
    private ChallengeUserData mViewedChallengeUserData; // The player who owns the gamebaord that is being viewed
    private ChallengeUserData mCurrentUserData; // The player who is looking at the board
    private ChallengeUserData mOpponentUserData; // The player who is not looking at the board
    private GameBoard mGameBoard; // The gamebaord currently being viewed
    private ArrayList<Ship> mShips;
    private ArrayList<ShipDrawableData> mShipDrawableDatas;
    private List<List<String>> mBoardPositionStatus;
    private boolean mCanBeAttacked;
    private int mNumShotsRemaining;
    private TextView mTxtNumShotsRemaining;
    private ImageView[][] mTargetImageViews;
    private ArrayList<int[]> mGifPositions;
    private int mPencilGifIndex;
    private boolean mComputerWon;

    // Used for new challenge
    public BattleshipBoardManager(Activity activity, Challenge challenge,
                                  ChallengeUserData challengeUserData, boolean canBeAttacked) {
        mActivity = activity;
        mCanBeAttacked = canBeAttacked;
        mChallenge = challenge;
        mViewedChallengeUserData = challengeUserData;
        mTargetImageViews = new ImageView[Constants.GameBoard.NUM_ROWS][Constants.GameBoard.NUM_COLUMNS];
        mComputerWon = false;
    }

    // Used for existing challenge
    public BattleshipBoardManager(Activity activity, Challenge challenge,
                                  ChallengeUserData viewedChallengeUserData, ChallengeUserData currentUserData,
                                  ChallengeUserData opponentUserData,
                                  GameBoard gameBoard, List<Ship> ships, boolean canBeAttacked) {
        mActivity = activity;
        mCanBeAttacked = canBeAttacked;
        mChallenge = challenge;
        mViewedChallengeUserData = viewedChallengeUserData;
        mCurrentUserData = currentUserData;
        mOpponentUserData = opponentUserData;
        mGameBoard = gameBoard;
        mShips = (ArrayList<Ship>) ships;
        mTargetImageViews = new ImageView[Constants.GameBoard.NUM_ROWS][Constants.GameBoard.NUM_COLUMNS];
        mComputerWon = false;
        retrieveShipDrawableDatas();
        retrieveBoardPositionStatus();
    }

    public void startShowingNumShotsRemaining(TextView txtNumShotsRemaining) {
        mTxtNumShotsRemaining = txtNumShotsRemaining;
        mNumShotsRemaining = mChallenge.initializeAndGetNumShotsRemaining();
        mChallenge.saveInBackground();
        showNumShotsRemaining();
    }

    private void showNumShotsRemaining() {
        final String text = mActivity.getResources().getString(R.string.num_shots_remaining)
                + " " + mNumShotsRemaining;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxtNumShotsRemaining.setText(text);
            }
        });
    }

    public void setShipsGridLayout(GridLayout shipsGridLayout) {
        mShipsGridLayout = shipsGridLayout;
        mShipsGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mShipsGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
        ViewGroup.LayoutParams layoutParams = mShipsGridLayout.getLayoutParams();
        layoutParams.width = mShipsGridLayout.getHeight() * Constants.GameBoard.NUM_COLUMNS
                / Constants.GameBoard.NUM_ROWS;
        mShipsGridLayout.setLayoutParams(layoutParams);
    }

    public void setTargetsGridLayout(GridLayout boardSpacesGridLayout) {
        mTargetsGridLayout = boardSpacesGridLayout;
        mTargetsGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mTargetsGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
        ViewGroup.LayoutParams layoutParams = mTargetsGridLayout.getLayoutParams();
        layoutParams.width = mTargetsGridLayout.getHeight() * Constants.GameBoard.NUM_COLUMNS
                / Constants.GameBoard.NUM_ROWS;
        mTargetsGridLayout.setLayoutParams(layoutParams);
    }

    public void setAnimationsGridLayout(GridLayout animationsGridLayout) {
        mAnimationsGridLayout = animationsGridLayout;
        mAnimationsGridLayout.setColumnCount(Constants.GameBoard.NUM_COLUMNS);
        mAnimationsGridLayout.setRowCount(Constants.GameBoard.NUM_ROWS);
        ViewGroup.LayoutParams layoutParams = mAnimationsGridLayout.getLayoutParams();
        layoutParams.width = mAnimationsGridLayout.getHeight() * Constants.GameBoard.NUM_COLUMNS
                / Constants.GameBoard.NUM_ROWS;
        mAnimationsGridLayout.setLayoutParams(layoutParams);
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
        if(mBoardPositionStatus == null) {
            return;
        }
        List<List<Boolean>> isLastMove = mGameBoard.getIsLastMove();
        for(int row = 0; row < mTargetsGridLayout.getRowCount(); row++) {
            for(int col = 0; col < mTargetsGridLayout.getColumnCount(); col++) {
                // Draw target
                ImageView imgSpace = new ImageView(mActivity);
                if (isLastMove.get(row).get(col) && !mCanBeAttacked) {
                    setTargetImageResource(imgSpace, Constants.GameBoardPositionStatus.UNKNOWN);
                } else {
                    setTargetImageResource(imgSpace, mBoardPositionStatus.get(row).get(col));
                }
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.height = mTargetsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS;
                layoutParams.width = mTargetsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS;
                layoutParams.columnSpec = GridLayout.spec(col);
                layoutParams.rowSpec = GridLayout.spec(row);
                imgSpace.setLayoutParams(layoutParams);
                mTargetsGridLayout.addView(imgSpace);
                mTargetImageViews[row][col] = imgSpace;

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
        addPlaceholderSpaces(mShipsGridLayout);

        mShips = new ArrayList<>();
        String[] shipTypes = Constants.getAllConstants(Constants.ShipType.class);

        for(String shipType : shipTypes) {
            placeShip(shipType);
        }
    }

    public void drawShips() {
        addPlaceholderSpaces(mShipsGridLayout);
        if(mShips == null) {
            return;
        }
        for(Ship ship : mShips) {
            ShipDrawableData shipDrawableData = ship.getShipDrawableData();
            drawShip(shipDrawableData);
        }
    }

    public void drawDeadShips() {
        addPlaceholderSpaces(mShipsGridLayout);
        if(mShips == null) {
            return;
        }
        for(ShipDrawableData shipDrawableData : mShipDrawableDatas) {
            if(!shipDrawableData.shipIsAlive) {
                drawShip(shipDrawableData);
            }
        }
    }

    public void initializeAnimationsGridLayout() {
        addPlaceholderSpaces(mAnimationsGridLayout);
    }

    public void showPreviousTurn() {
        if(mGameBoard == null) {
            return;
        }
        if(mGameBoard.getShouldDisplayLastMove()) {
            List<List<Boolean>> isLastMove = mGameBoard.getIsLastMove();
            mGifPositions = new ArrayList<>();
            for(int i = 0; i < isLastMove.size(); i++) {
                for(int j = 0; j < isLastMove.get(i).size(); j++) {
                    if(isLastMove.get(i).get(j)) {
                        mGifPositions.add(new int[]{i, j});
                    }
                }
            }
            if (!mGifPositions.isEmpty()) {
                drawPastTurnGif(mGifPositions.get(0)[0], mGifPositions.get(0)[1], 5, 5, R.drawable.animation_pencil_fill_in);
            }
        }
    }

    public void saveNewGameBoard() {
        mGameBoard = new GameBoard(mShips, createShipAt());
        mGameBoard.saveInBackground();
        mViewedChallengeUserData.setGameBoard(mGameBoard);
        mViewedChallengeUserData.saveInBackground();
        ParseObject.saveAllInBackground(mShips);
    }

    public void saveGameBoard() {
        mGameBoard.setStatus(mBoardPositionStatus);
        mGameBoard.saveInBackground();
        ParseObject.saveAllInBackground(mShips);
        mViewedChallengeUserData.saveInBackground();
        mChallenge.setNumShotsRemaining(mNumShotsRemaining);
        mChallenge.saveInBackground();
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
        return new int[] { mCurrentUserData.getScore(), mOpponentUserData.getScore() };
    }

    public ParseFile[] getProfilePictures() {
        return new ParseFile[] {
                mCurrentUserData.getPublicUserData().getProfilePic(),
                mOpponentUserData.getPublicUserData().getProfilePic()
        };
    }

    public void clearImages() {
        if (mTargetsGridLayout != null) {
            mTargetsGridLayout.removeAllViews();
        }
        if (mShipsGridLayout != null) {
            mShipsGridLayout.removeAllViews();
        }
        if (mAnimationsGridLayout != null) {
            mAnimationsGridLayout.removeAllViews();
        }
        if (mGifPositions != null) {
            mGifPositions.clear();
        }
    }

    public void quitChallenge() {
        mChallenge.setHasEnded(true);
        mChallenge.setEndDate(new Date());
        mChallenge.setTimeLastPlayed(new Date());
        mChallenge.setWinner(mOpponentUserData.getPublicUserData().getBaseUserId());
        mChallenge.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                // Refresh Challenge list
                Intent refreshIntent = new Intent(Constants.IntentExtra.REFRESH_CHALLENGE_LIST);
                refreshIntent.putExtra(Constants.IntentExtra.REFRESH_CHALLENGE_LIST, true);
                if (mActivity != null) {
                    mActivity.sendBroadcast(refreshIntent);
                }
                return null;
            }
        });
        alertLostChallenge();
    }

    public void createComputerBoard() {
        placeShips();
        GameBoard gameBoard = new GameBoard(mShips, createShipAt());
        gameBoard.saveInBackground();
        ChallengeUserData computerUserData = mChallenge.getChallengeUserData(2);
        computerUserData.setGameBoard(gameBoard);
        computerUserData.saveInBackground();
    }

    public void takeComputerTurn() {
        final int NUM_SHOTS = 3;
        for (int i = 0; i < NUM_SHOTS; i++) {
            findAndTakeComputerShot();
        }
        if (!mComputerWon) {
            setOtherPlayerTurn(true);
            GameBoard computerGameBoard = mOpponentUserData.getGameBoard();
            computerGameBoard.setShouldDisplayLastMove(false);
            computerGameBoard.resetIsLastMove();
            computerGameBoard.saveInBackground();
        }
    }

    private void findAndTakeComputerShot() {
        Location previousHit = findPreviousHit();
        if (previousHit != null) {
            fireAroundPosition(previousHit.row, previousHit.col);
        } else {
            fireRandomly();
        }
    }

    private class Location {
        int row;
        int col;
        public Location(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private Location findPreviousHit() {
        for (int row = 0; row < Constants.GameBoard.NUM_ROWS; row++) {
            for (int col = 0; col < Constants.GameBoard.NUM_COLUMNS; col++) {
                Ship shipThatOccupiesPosition = findShipThatOccupiesPosition(row, col);
                boolean hitAtPosition = mBoardPositionStatus.get(row).get(col).equals(Constants.GameBoardPositionStatus.HIT);
                boolean hasShotAllAroundPosition = hasShotAllAroundPosition(row, col);
                boolean isOnDeadShip = shipThatOccupiesPosition != null && shipThatOccupiesPosition.getHitsRemaining() == 0;
                if (hitAtPosition && !hasShotAllAroundPosition && !isOnDeadShip) {
                    return new Location(row, col);
                }
            }
        }
        return null;
    }

    private boolean hasShotAllAroundPosition(int row, int col) {
        boolean shotAbove = row == 0 || positionHasBeenShot(row - 1, col);
        boolean shotBelow = row == Constants.GameBoard.NUM_ROWS - 1 || positionHasBeenShot(row + 1, col);
        boolean shotToLeft = col == 0 || positionHasBeenShot(row, col - 1);
        boolean shotToRight = col == Constants.GameBoard.NUM_COLUMNS - 1 || positionHasBeenShot(row, col + 1);
        return shotAbove && shotBelow && shotToLeft && shotToRight;
    }

    private void fireRandomly() {
        int row, col;
        do {
            row = new Random().nextInt(Constants.GameBoard.NUM_ROWS);
            col = new Random().nextInt(Constants.GameBoard.NUM_COLUMNS);
        } while (positionHasBeenShot(row, col));
        takeComputerShot(row, col);
    }

    private boolean positionHasBeenShot(int row, int col) {
        switch (mBoardPositionStatus.get(row).get(col)) {
            case Constants.GameBoardPositionStatus.HIT:
            case Constants.GameBoardPositionStatus.MISS:
                return true;
        }
        return false;
    }

    private void fireAroundPosition(int row, int col) {
        boolean shotAbove = row == 0 || positionHasBeenShot(row - 1, col);
        boolean shotBelow = row == Constants.GameBoard.NUM_ROWS - 1 || positionHasBeenShot(row + 1, col);
        boolean shotToLeft = col == 0 || positionHasBeenShot(row, col - 1);
        boolean shotToRight = col == Constants.GameBoard.NUM_COLUMNS - 1 || positionHasBeenShot(row, col + 1);
        if (!shotAbove) {
            takeComputerShot(row - 1, col);
            return;
        }
        if (!shotBelow) {
            takeComputerShot(row + 1, col);
            return;
        }
        if (!shotToLeft) {
            takeComputerShot(row, col - 1);
            return;
        }
        if (!shotToRight) {
            takeComputerShot(row, col + 1);
            return;
        }
    }

    private void takeComputerShot(int row, int col) {
        Ship shipThatOccupiesPosition = findShipThatOccupiesPosition(row, col);
        if(shipThatOccupiesPosition == null) {
            mBoardPositionStatus.get(row).set(col, Constants.GameBoardPositionStatus.MISS);
        }
        else {
            mBoardPositionStatus.get(row).set(col, Constants.GameBoardPositionStatus.HIT);
            shipThatOccupiesPosition.decrementHitsRemaining();
            if(shipThatOccupiesPosition.getHitsRemaining() == 0) {
                mOpponentUserData.incrementScore();
                mOpponentUserData.saveInBackground();

                if(mOpponentUserData.getScore() == Constants.GameBoard.NUM_SHIPS) {
                    computerWins();
                }
            }
        }
        mGameBoard.setIsLastMoveAtPosition(row, col);
        saveGameBoard();
    }

    private void computerWins() {
        String curTurnUserId = mChallenge.getCurTurnUserId();
        String otherTurnUserId = mChallenge.getOtherTurnUserId();
        mChallenge.setCurTurnUserId(otherTurnUserId);
        mChallenge.setOtherTurnUserId(curTurnUserId);
        mChallenge.setQuesAnsThisTurn(0);
        mChallenge.setCorrectAnsThisTurn(0);
        mChallenge.setThisTurnQuestionIds(new ArrayList<String>());
        mChallenge.incrementNumTurns();
        mChallenge.setTimeLastPlayed(new Date());
        mChallenge.saveInBackground();

        mGameBoard.setShouldDisplayLastMove(true);
        mGameBoard.saveInBackground();

        setOtherPlayerTurn(true);

        mChallenge.setHasEnded(true);
        mChallenge.setEndDate(new Date());
        mChallenge.setWinner(mOpponentUserData.getPublicUserData().getBaseUserId());
        mChallenge.saveInBackground();
        mComputerWon = true;
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
                showNumShotsRemaining();
                if(mNumShotsRemaining == 0) {
                    setOtherPlayerTurn(false);
                    ((BattleshipAttackActivity) mActivity).showBtnDone();
                } else {
                    mChallenge.setNumShotsRemaining(mNumShotsRemaining);
                    mChallenge.saveInBackground();
                }
                boolean destroyedShip = false;
                Ship shipThatOccupiesPosition = findShipThatOccupiesPosition(row, col);
                if(shipThatOccupiesPosition == null) {
                    mBoardPositionStatus.get(row).set(col, Constants.GameBoardPositionStatus.MISS);
                }
                else {
                    mBoardPositionStatus.get(row).set(col, Constants.GameBoardPositionStatus.HIT);
                    shipThatOccupiesPosition.decrementHitsRemaining();
                    if(shipThatOccupiesPosition.getHitsRemaining() == 0) {
                        destroyedShip = true;
                        shipThatOccupiesPosition.getShipDrawableData().shipIsAlive = false;
                        drawShip(shipThatOccupiesPosition.getShipDrawableData());
                        mCurrentUserData.incrementScore();
                        mCurrentUserData.saveInBackground();
                    }
                }
                drawAttackGif(row, col, 5, 5, R.drawable.animation_pencil_fill_in, destroyedShip, imgSpace,
                        mBoardPositionStatus.get(row).get(col));

                mGameBoard.setIsLastMoveAtPosition(row, col);
                saveGameBoard();
                break;
        }
    }

    private void setOtherPlayerTurn(boolean isComputerTurn) {
        String curTurnUserId = mChallenge.getCurTurnUserId();
        String otherTurnUserId = mChallenge.getOtherTurnUserId();
        mChallenge.setCurTurnUserId(otherTurnUserId);
        mChallenge.setOtherTurnUserId(curTurnUserId);
        mChallenge.setQuesAnsThisTurn(0);
        mChallenge.setCorrectAnsThisTurn(0);
        mChallenge.setThisTurnQuestionIds(new ArrayList<String>());
        mChallenge.setThisTurnQsAreBundle(false);
        mChallenge.incrementNumTurns();
        mChallenge.setTimeLastPlayed(new Date());
        mChallenge.saveInBackground();

        mGameBoard.setShouldDisplayLastMove(true);
        mGameBoard.saveInBackground();

        if (!isComputerTurn) {
            mCurrentUserData.getGameBoard().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    GameBoard opponentGameBoard = (GameBoard) object;
                    opponentGameBoard.setShouldDisplayLastMove(false);
                    opponentGameBoard.resetIsLastMove();
                    opponentGameBoard.saveInBackground();
                    if(!mChallenge.getHasEnded()) {
                        sendChallengeNotification(Constants.CloudCodeFunction.SendChallengeNotification.NotificationType.YOUR_TURN);
                    }
                }
            });
        }
    }

    private void sendChallengeNotification(String type) {
        String challengeId = mChallenge.getObjectId();
        String senderBaseUserId = UserUtils.getCurrentUserId();
        String receiverBaseUserId = mChallenge.getOpponentBaseUserId();
        int user1Or2 = mChallenge.getOpponentUser1Or2();
        CommonUtils.sendChallengeNotification(type, challengeId, senderBaseUserId, receiverBaseUserId, user1Or2);
    }

    private void endChallenge() {
        setOtherPlayerTurn(false);

        mChallenge.setHasEnded(true);
        mChallenge.setEndDate(new Date());
        mChallenge.setWinner(mCurrentUserData.getPublicUserData().getBaseUserId());
        mChallenge.saveInBackground();
        sendChallengeNotification(Constants.CloudCodeFunction.SendChallengeNotification.NotificationType.GAME_OVER);

        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.title_dialog_won_challenge)
                .setMessage(R.string.message_dialog_won_challenge)
                .setPositiveButton(R.string.yes_dialog_won_challenge, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveGameBoard();
                    }
                })
                .create().show();
    }

    public void alertLostChallenge() {
        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.title_dialog_lost_challenge)
                .setMessage(R.string.message_dialog_lost_challenge)
                .setPositiveButton(R.string.yes_dialog_lost_challenge, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
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
    private void addPlaceholderSpaces(GridLayout gridLayout) {
        for(int row = 0; row < gridLayout.getRowCount(); row++) {
            for(int col = 0; col < gridLayout.getColumnCount(); col++) {
                ImageView imgSpace = new ImageView(mActivity);
                imgSpace.setImageResource(R.drawable.target_default_unknown);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.height = gridLayout.getHeight() / Constants.GameBoard.NUM_ROWS;
                layoutParams.width = gridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS;
                layoutParams.columnSpec = GridLayout.spec(col);
                layoutParams.rowSpec = GridLayout.spec(row);
                imgSpace.setLayoutParams(layoutParams);
                gridLayout.addView(imgSpace);
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
        if(mShips == null) {
            return;
        }
        mShipDrawableDatas = new ArrayList<>();
        for (Ship ship : mShips) {
            ShipDrawableData shipDrawableData = new ShipDrawableData(ship);
            ship.setShipDrawableData(shipDrawableData);
            mShipDrawableDatas.add(shipDrawableData);
        }
    }

    private void drawAttackGif(final int row, final int col, final int height, final int width, int gifDrawableId, final boolean destroyedShip, final ImageView imgSpace, final String boardPositionStatus) {
        final GifImageView gifImageView = new GifImageView(mActivity);
        gifImageView.setImageResource(gifDrawableId);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(row, 1);
        layoutParams.columnSpec = GridLayout.spec(col, 1);
        layoutParams.height = mAnimationsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS * height;
        layoutParams.width = mAnimationsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS * width;
        gifImageView.setLayoutParams(layoutParams);
        mAnimationsGridLayout.addView(gifImageView);

        GifDrawable gifDrawable = (GifDrawable) gifImageView.getDrawable();
        gifDrawable.addAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                gifImageView.setVisibility(View.GONE);
                setTargetImageResource(imgSpace, boardPositionStatus);
                int textRow = row == 0 ? 1 : row - 1;
                int textCol = col == 0 ? 1 : col - 1;

                if (destroyedShip) {
                    drawFadingOutText(textRow, textCol, 2, 5, R.drawable.text_ship_destroyed);
                } else if (boardPositionStatus.equals(Constants.GameBoardPositionStatus.HIT)) {
                    drawFadingOutText(textRow, textCol, 2, 2, R.drawable.text_hit);
                } else {
                    drawFadingOutText(textRow, textCol, 2, 2, R.drawable.text_miss);
                }
            }
        });
    }

    private void drawPastTurnGif(int row, int col, final int height, final int width, final int gifDrawableId) {
        final GifImageView gifImageView = new GifImageView(mActivity);
        gifImageView.setImageResource(gifDrawableId);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(row, 1);
        layoutParams.columnSpec = GridLayout.spec(col, 1);
        layoutParams.height = mAnimationsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS * height;
        layoutParams.width = mAnimationsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS * width;
        gifImageView.setLayoutParams(layoutParams);
        mAnimationsGridLayout.addView(gifImageView);

        GifDrawable gifDrawable = (GifDrawable) gifImageView.getDrawable();
        gifDrawable.addAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                if (mGifPositions == null || mGifPositions.isEmpty()) return;

                int row = mGifPositions.get(mPencilGifIndex)[0];
                int col = mGifPositions.get(mPencilGifIndex)[1];
                ImageView imgTarget = mTargetImageViews[row][col];
                setTargetImageResource(imgTarget, mBoardPositionStatus.get(row).get(col)); // Change target image

                gifImageView.setVisibility(View.GONE);
                mPencilGifIndex++;
                if (mPencilGifIndex < mGifPositions.size()) {
                    row = mGifPositions.get(mPencilGifIndex)[0];
                    col = mGifPositions.get(mPencilGifIndex)[1];
                    drawPastTurnGif(row, col, height, width, gifDrawableId); // Draw next gif
                }
            }
        });
    }

    private void drawFadingOutText(final int row, final int col, final int height, final int width, final int drawableId) {
        final ImageView ivHitText = new ImageView(mActivity);
        ivHitText.setImageResource(drawableId);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(row, 1);
        layoutParams.columnSpec = GridLayout.spec(col, 1);
        if (row + height >= Constants.GameBoard.NUM_ROWS) {
            layoutParams.rowSpec = GridLayout.spec(Constants.GameBoard.NUM_ROWS - height);
        }
        if (col + width >= Constants.GameBoard.NUM_COLUMNS) {
            layoutParams.columnSpec = GridLayout.spec(Constants.GameBoard.NUM_COLUMNS - width);
        }
        layoutParams.height = mAnimationsGridLayout.getHeight() / Constants.GameBoard.NUM_ROWS * height;
        layoutParams.width = mAnimationsGridLayout.getWidth() / Constants.GameBoard.NUM_COLUMNS * width;
        ivHitText.setLayoutParams(layoutParams);
        mAnimationsGridLayout.addView(ivHitText);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                ivHitText.setVisibility(View.GONE);
                mAnimationsGridLayout.removeView(ivHitText);

                if(mCurrentUserData.getScore() == Constants.GameBoard.NUM_SHIPS) {
                    endChallenge();
                    setOtherPlayerTurn(false);
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        ivHitText.startAnimation(fadeOut);
    }

    private void retrieveBoardPositionStatus() {
        if(mGameBoard != null) {
            mBoardPositionStatus = mGameBoard.getStatus();
        }
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
