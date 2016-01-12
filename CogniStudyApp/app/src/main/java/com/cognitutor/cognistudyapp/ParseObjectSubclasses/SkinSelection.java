package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.QS_ShopItemInfo;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/9/2016.
 */
@ParseClassName("SkinSelection")
public class SkinSelection extends ParseObject {

    public class Columns {
        public static final String studentId = "studentId";
        public static final String customizableType = "customizableType";
        public static final String shopItemType = "shopItemType";
    }

    public SkinSelection() {}
    public SkinSelection(String studentId, String customizableType, String shopItemType) {
        put(Columns.studentId, studentId);
        put(Columns.customizableType, customizableType);
        put(Columns.shopItemType, shopItemType);
    }

    public String getShopItemType() { return getString(Columns.shopItemType); }

    public static ParseQuery<SkinSelection> getQuery() { return ParseQuery.getQuery(SkinSelection.class); }

    private static ParseQuery<SkinSelection> getLocalDataStoreQuery(String studentId, String customizableType) {
        return getQuery().fromLocalDatastore()
                .whereEqualTo(Columns.studentId, studentId)
                .whereEqualTo(Columns.customizableType, customizableType);
    }

    public static Task<SkinSelection> getSkinSelectionInBackground(String studentId, String customizableType) {
        return getLocalDataStoreQuery(studentId, customizableType).getFirstInBackground();
    }

    public static Task<String> getShopItemTypeInBackground(String studentId, String customizableType) {

        return getSkinSelectionInBackground(studentId, customizableType)
            .onSuccess(new Continuation<SkinSelection, String>() {
                @Override
                public String then(Task<SkinSelection> task) throws Exception {
                    return task.getResult().getShopItemType();
                }
            });
    }

    public static SkinSelection getSkinSelection(String studentId, String customizableType) {
        try { return getLocalDataStoreQuery(studentId, customizableType).getFirst(); }
        catch(ParseException e) { e.printStackTrace(); return null; }
    }

    public static String getShopItemType(String studentId, String customizableType) {
        SkinSelection skinSelection = getSkinSelection(studentId, customizableType);
        if (skinSelection != null) return skinSelection.getShopItemType();
        else return null;
    }

    public static void setShopItemType(String studentId, String customizableType, final String shopItemType) {
        getSkinSelectionInBackground(studentId, customizableType)
                .onSuccess(new Continuation<SkinSelection, Void>() {
                    @Override
                    public Void then(Task<SkinSelection> task) throws Exception {
                        SkinSelection object = task.getResult();
                        object.put(Columns.shopItemType, shopItemType);
                        object.saveEventually();
                        return null;
                    }
                });
    }

    public static Task<String> getGameDrawableInBackground(String studentId, String customizableType) {
        return getShopItemTypeInBackground(studentId, customizableType)
                .onSuccess(new Continuation<String, String>() {
                    @Override
                    public String then(Task<String> task) throws Exception {
                        return QS_ShopItemInfo.getGameImage(task.getResult());
                    }
                });
    }

    public static String getGameDrawable(String studentId, String customizableType) {
        String shopItemType = getShopItemType(studentId, customizableType);
        if (shopItemType != null) return QS_ShopItemInfo.getGameImage(shopItemType);
        else return null;
    }
}
