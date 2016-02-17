package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kevin on 1/12/2016.
 */
@ParseClassName("GameBoard")
public class GameBoard extends ParseObject {

    public class Columns {
        public static final String objectId = "objectId";
        public static final String status = "status";
        public static final String ships = "ships";
        public static final String shipAt = "shipAt";
        public static final String shouldDisplayLastMove = "shouldDisplayLastMove";
        public static final String lastAbilityUsed = "lastAbilityUsed";
        public static final String isLastMove = "isLastMove";
        public static final String lastAbilityXPos = "lastAbilityXPos";
        public static final String lastAbilityYPos = "lastAbilityYPos";
    }

    public GameBoard(List<Ship> ships, List<List<Ship>> shipAt) {
        put(Columns.ships, ships);
        put(Columns.shipAt, shipAt);
        put(Columns.status, createNewStatus());
        put(Columns.shouldDisplayLastMove, false);
        put(Columns.lastAbilityUsed, JSONObject.NULL);
        put(Columns.isLastMove, createNewIsLastMove());
        put(Columns.lastAbilityXPos, JSONObject.NULL);
        put(Columns.lastAbilityYPos, JSONObject.NULL);
    }

    private List<List<String>> createNewStatus() {
        List<List<String>> status = new ArrayList<>();
        for(int i = 0; i < Constants.GameBoard.NUM_ROWS; i++) {
            List<String> rowList = new ArrayList<String>(
                    Collections.nCopies(Constants.GameBoard.NUM_COLUMNS,
                            Constants.GameBoardPositionStatus.UNKNOWN)
            );
            status.add(rowList);
        }
        return status;
    }

    private List<List<Boolean>> createNewIsLastMove() {
        List<List<Boolean>> isLastMove = new ArrayList<>();
        for(int i = 0; i < Constants.GameBoard.NUM_ROWS; i++) {
            List<Boolean> rowList = new ArrayList<Boolean>(
                    Collections.nCopies(Constants.GameBoard.NUM_COLUMNS, false)
            );
            isLastMove.add(rowList);
        }
        return isLastMove;
    }

    public GameBoard() {

    }

    public List<Ship> getShips() {
        return (List<Ship>) get(Columns.ships);
    }

    public List<List<String>> getStatus() {
        return (List<List<String>>) get(Columns.status);
    }

    public void setStatus(List<List<String>> status) {
        put(Columns.status, status);
    }

    public void setShouldDisplayLastMove(boolean shouldDisplayLastMove) {
        put(Columns.shouldDisplayLastMove, shouldDisplayLastMove);
    }

    public boolean getShouldDisplayLastMove() {
        return getBoolean(Columns.shouldDisplayLastMove);
    }

    public List<List<Boolean>> getIsLastMove() {
        return (List<List<Boolean>>) get(Columns.isLastMove);
    }

    public void setPositionAttacked(int row, int col) {
        List<List<Boolean>> isLastMove = getIsLastMove();
        isLastMove.get(row).set(col, true);
        put(Columns.isLastMove, isLastMove);
    }
}
