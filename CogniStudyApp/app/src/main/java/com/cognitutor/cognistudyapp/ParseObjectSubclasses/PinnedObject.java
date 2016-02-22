package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bolts.Task;

/**
 * Created by Kevin on 2/21/2016.
 */
@ParseClassName("PinnedObject")
public class PinnedObject extends ParseObject{

    public class Columns {
        public static final String baseUserID = "baseUserID";
        public static final String pinTime = "pinTime";
        public static final String pinName = "pinName";
        public static final String pinObjectId = "pinObjectId";
        public static final String pinObjectClass = "pinObjectClass";
    }

    public PinnedObject() {}

    public PinnedObject(String pinName, ParseObject object) {
        put(Columns.baseUserID, UserUtils.getCurrentUserId());
        put(Columns.pinTime, new Date());
        put(Columns.pinName, pinName);
        if(object.getObjectId() != null)
            put(Columns.pinObjectId, object.getObjectId());
        put(Columns.pinObjectClass, object.getClassName());
        ParseACL acl = new ParseACL(ParseUser.getCurrentUser()); //TODO: Verify this
        acl.setPublicReadAccess(false);
        setACL(acl);
        //DO NOT use ParseObjectUtils's method for pinning, it will cause recursion!
        pinInBackground(ParseObjectUtils.PinNames.PinData);
        this.saveEventually();
    }

    public static ParseQuery<PinnedObject> getQuery() { return ParseQuery.getQuery(PinnedObject.class); }
    public static ParseQuery<PinnedObject> getQueryForUserOldestFirst(String pinName) {
        return getQuery()
                .fromLocalDatastore()
                .whereEqualTo(Columns.pinName, pinName)
                .orderByAscending(Columns.pinTime);
    }

    public static ParseQuery<PinnedObject> getQueryForListOfObjects(List<ParseObject> objects) {
        List<String> objectIds = new ArrayList<>();
        for(ParseObject obj : objects) {
            objectIds.add(obj.getObjectId());
        }
        return getQuery()
                .fromLocalDatastore()
                .whereContainedIn(Columns.pinObjectId, objectIds);
    }

    public static void unpinAllObjectsAndDelete(List<PinnedObject> pinnedObjects) {
        for(PinnedObject pinnedObject : pinnedObjects) {
            pinnedObject.unpinObjectAndDelete();
        }
    }

    public void unpinObjectAndDelete() {
        ParseObject object = null;

        try {
            object = ParseQuery.getQuery(getPinObjectClass())
                    .fromLocalDatastore()
                    .get(getPinObjectId());
        } catch (ParseException e) { e.printStackTrace(); }

        if(object != null) {
            try {
                object.unpin();
                this.deleteEventually();
            } catch (ParseException e) { e.printStackTrace(); }
        }
    }

    public static Task<Void> deleteAllPinnedObjectsInBackground() {
        ParseQuery<PinnedObject> query = getQuery()
                .fromLocalDatastore();
        return ParseObjectUtils.deleteObjectsInBackground(query);
    }

    public String getBaseUserId() { return getString(Columns.baseUserID); }
    public Date getPinTime() { return getDate(Columns.pinTime); }
    public String getPinName() { return getString(Columns.pinName); }
    public String getPinObjectId() { return getString(Columns.pinObjectId); }
    public String getPinObjectClass() { return getString(Columns.pinObjectClass); }
}
