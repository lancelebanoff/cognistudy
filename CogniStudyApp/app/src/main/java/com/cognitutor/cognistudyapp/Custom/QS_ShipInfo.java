package com.cognitutor.cognistudyapp.Custom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kevin on 12/30/2015.
 */
public class QS_ShipInfo {
    public static final Map<String, ShipInfo> ShipTypeToShipInfo;
    static {
        Map<String, ShipInfo> map = new HashMap<String, ShipInfo>();
        map.put(Constants.ShipType.RULER,
                new ShipInfo(5,1,
                        "Ruler",
                        "You broke your foe's ruler!",
                        "Your ruler has been broken!"));
        map.put(Constants.ShipType.CALCULATOR,
                new ShipInfo(2,2,
                        "Calculator",
                        "You broke your foe's calculator!",
                        "Your calculator has been broken!"));
        map.put(Constants.ShipType.PEN,
                new ShipInfo(4,1,
                        "Pen",
                        "You broke your foe's pen!",
                        "Your pen has been broken!"));
        map.put(Constants.ShipType.YELLOW_PENCIL,
                new ShipInfo(3,1,
                        "Yellow Pencil",
                        "You broke your foe's yellow pencil!",
                        "Your yellow pencil has been broken!"));
        map.put(Constants.ShipType.GREEN_PENCIL,
                new ShipInfo(3,1,
                        "Green Pencil",
                        "You broke your foe's green pencil!",
                        "Your green pencil has been broken!"));
        map.put(Constants.ShipType.ERASER,
                new ShipInfo(2,1,
                        "Eraser",
                        "You shredded your foe's eraser!",
                        "Your eraser has been shredded!"));
        ShipTypeToShipInfo = Collections.unmodifiableMap(map);
    }
}
class ShipInfo {
    int height;
    int width;
    String displayName;
    String enemyDestroyedMsg;
    String ownDestroyedMsg;
    public ShipInfo(int height, int width, String displayName, String enemyDestroyedMsg, String ownDestroyedMsg) {
        this.height = height;
        this.width = width;
        this.displayName = displayName;
        this.enemyDestroyedMsg = enemyDestroyedMsg;
        this.ownDestroyedMsg = ownDestroyedMsg;
    }
}
