package com.cognitutor.cognistudyapp.Custom;

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

    public static String getGameImage(String shopItemType) {
        return ShopItemTypeToShopItemInfo.get(shopItemType).getGameImage();
    }
}

class ShopItemInfo {
    private static String imageFolder = "images/"; //TODO: Verify this
    private String shopItemType;
    private String displayName;
    private String shopImage;
    private String gameImage;
    private int cost;

    public ShopItemInfo(String shopItemType, String displayName, int cost) {
        this.shopItemType = shopItemType;
        this.displayName = displayName;
        shopImage = imageFolder + shopItemType.toLowerCase() + "_shop.png"; //TODO: png?
        gameImage = imageFolder + shopItemType.toLowerCase() + "_game.gif";
        this.cost = cost;
    }

    public String getShopItemType() { return shopItemType; }
    public String getDisplayName() { return displayName; }
    public String getShopImage() { return shopImage; }
    public String getGameImage() { return gameImage; }
    public int getCost() { return cost; }
}

