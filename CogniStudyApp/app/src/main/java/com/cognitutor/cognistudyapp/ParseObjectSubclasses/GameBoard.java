package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Kevin on 1/12/2016.
 */
@ParseClassName("GameBoard")
public class GameBoard extends ParseObject {

    public class Columns {
        public static final String status = "status";
        public static final String ships = "ships";
        public static final String shipAt = "shipAt";
        public static final String lastAbilityUsed = "lastAbilityUsed";
        public static final String isLastMove = "isLastMove";
        public static final String lastAbilityXPos = "lastAbilityXPos";
        public static final String lastAbilityYPos = "lastAbilityYPos";
    }
}
