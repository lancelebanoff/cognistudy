package com.cognitutor.cognistudyapp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kevin on 12/30/2015.
 */
public class QS_ShopItemInfo {
    public static final Map<String, ShopItemInfo> ShopItemTypeToShopItemInfo;
    static {
        Map<String, ShopItemInfo> map = new HashMap<String, ShopItemInfo>();
        map.put(Constants.ShopItemType.ERASE_SHIP,
                new ShopItemInfo(Constants.ShopItemType.ERASE_SHIP,
                        "Erase Ship",
                        10));
        //TODO: Finish entering these
        ShopItemTypeToShopItemInfo = Collections.unmodifiableMap(map);
    }
}
class ShopItemInfo {
    static String imageFolder = "images/"; //TODO: Verify this
    String shopItemType;
    String displayName;
    String shopImage;
    String gameImage;
    int cost;
    public ShopItemInfo(String shopItemType, String displayName, int cost) {
        this.shopItemType = shopItemType;
        this.displayName = displayName;
        shopImage = imageFolder + shopItemType.toLowerCase() + "_shop.png"; //TODO: png?
        gameImage = imageFolder + shopItemType.toLowerCase() + "_game.gif";
        this.cost = cost;
    }
}
