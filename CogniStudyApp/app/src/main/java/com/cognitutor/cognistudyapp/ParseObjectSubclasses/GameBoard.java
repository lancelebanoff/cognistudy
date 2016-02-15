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
        public static final String lastAbilityUsed = "lastAbilityUsed";
        public static final String isLastMove = "isLastMove";
        public static final String lastAbilityXPos = "lastAbilityXPos";
        public static final String lastAbilityYPos = "lastAbilityYPos";
    }

    public GameBoard(List<Ship> ships, List<List<Ship>> shipAt) {
        put(Columns.ships, ships);
        put(Columns.shipAt, shipAt);
        put(Columns.status, createNewStatus());
        put(Columns.lastAbilityUsed, JSONObject.NULL);
        put(Columns.isLastMove, JSONObject.NULL);
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
}
