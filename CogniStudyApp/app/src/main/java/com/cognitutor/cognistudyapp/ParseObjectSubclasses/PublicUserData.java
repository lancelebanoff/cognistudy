package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.support.v4.content.res.TypedArrayUtils;
import android.text.format.DateUtils;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bolts.Task;

/**
 * Created by Kevin on 1/7/2016.
 */
@ParseClassName("PublicUserData")
public class PublicUserData extends ParseObject{

    public class Columns {
        public static final String objectId = "objectId";
        public static final String userType = "userType";
        public static final String displayName = "displayName";
        public static final String lastSeen = "lastSeen";
        public static final String profilePic = "profilePic";
        public static final String profilePicData = "profilePicData";
        public static final String baseUserId = "baseUserId";
        public static final String student = "student";
        public static final String tutor = "tutor";
        public static final String facebookId = "facebookId";
        public static final String fbLinked = "fbLinked";
    }

    public PublicUserData() {

    }

    public PublicUserData(ParseUser user, Student student, String facebookId, String displayName, ParseFile profilePic, byte[] profilePicData) {
        ParseACL publicDataACL = new ParseACL(user);
        publicDataACL.setPublicReadAccess(true);
        setACL(publicDataACL);
        put(Columns.userType, Constants.UserType.STUDENT);
        put(Columns.baseUserId, user.getObjectId());
        put(Columns.student, student);
        put(Columns.displayName, displayName);
        put(Columns.facebookId, facebookId);
        put(Columns.fbLinked, facebookId != null);

        if(profilePic != null)
            put(Columns.profilePic, profilePic);
        if(profilePicData != null)
            put(Columns.profilePicData, Arrays.asList(profilePicData));
    }

    public Student getStudent() throws ParseException {
        //TODO: Finish this
        return (Student) getParseObject(Columns.student).fetchIfNeeded();
    }

    //TODO: Add getTutor() method

    public String getUserType() { return getString(Columns.userType); }
    public String getDisplayName() { return getString(Columns.displayName); }
    public Date getLastSeen() { return getDate(Columns.lastSeen); }
    public ParseFile getProfilePic() { return getParseFile(Columns.profilePic); }
    public byte[] getProfilePicData() { return getBytes(Columns.profilePicData); }
    public String getBaseUserId() { return getString(Columns.baseUserId); }

    public static ParseQuery<PublicUserData> getQuery() {
        return ParseQuery.getQuery(PublicUserData.class);
    }

    public static PublicUserData getPublicUserData() {
        return getPublicUserDataFromBaseUserId(ParseUser.getCurrentUser().getObjectId());
    }

    public static PublicUserData getPublicUserData(String publicUserDataID) {
        try { return getLocalDataStoreQuery(Columns.objectId, publicUserDataID).getFirst(); }
        catch (ParseException e) { e.printStackTrace(); return null; }
    }

    private static PublicUserData getPublicUserDataFromBaseUserId(String baseUserId) {

        try { return getLocalDataStoreQuery(Columns.baseUserId, baseUserId).getFirst(); }
        catch (ParseException e) { e.printStackTrace(); return null; }
    }

    public static Task<PublicUserData> getPublicUserDataInBackground() {
        return getPublicUserDataInBackground(ParseUser.getCurrentUser().getObjectId());
    }

    public static Task<PublicUserData> getPublicUserDataInBackground(String baseUserId) {

        return getLocalDataStoreQuery(Columns.baseUserId, baseUserId).getFirstInBackground();
    }

    private static ParseQuery<PublicUserData> getLocalDataStoreQuery(String column, String baseUserId) {

        return PublicUserData.getQuery()
                .fromLocalDatastore()
                .whereEqualTo(column, baseUserId);
    }
}
