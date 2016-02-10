package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Lance on 2/2/2016.
 */
@ParseClassName("Ship")
public class Ship extends ParseObject {

    public class Columns {
        public static final String objectId = "objectId";
        public static final String shipType = "shipType";
        public static final String startRow = "startRow";
        public static final String startColumn = "startColumn";
        public static final String rotation = "rotation";
        public static final String hitsRemaining = "hitsRemaining";
    }

    private BattleshipBoardManager.ShipDrawableData shipDrawableData;

    public Ship(String shipType, int startRow, int startColumn, String rotation, int hitsRemaining) {
        put(Columns.shipType, shipType);
        put(Columns.startRow, startRow);
        put(Columns.startColumn, startColumn);
        put(Columns.rotation, rotation);
        put(Columns.hitsRemaining, hitsRemaining);
    }

    public Ship() {

    }

    public void setShipDrawableData(BattleshipBoardManager.ShipDrawableData shipDrawableData) {
        this.shipDrawableData = shipDrawableData;
    }

    public BattleshipBoardManager.ShipDrawableData getShipDrawableData() {
        return shipDrawableData;
    }

    public int getStartRow() {
        return getInt(Columns.startRow);
    }

    public int getStartColumn() {
        return getInt(Columns.startColumn);
    }

    public String getShipType() {
        return getString(Columns.shipType);
    }

    public String getRotation() {
        return getString(Columns.rotation);
    }

    public int getHitsRemaining() {
        return getInt(Columns.hitsRemaining);
    }
}
