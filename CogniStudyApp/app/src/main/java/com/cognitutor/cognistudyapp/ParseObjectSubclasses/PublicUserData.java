package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.ACLUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.Date;

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
        public static final String searchableDisplayName = "searchableDisplayName";
    }

    public PublicUserData() {

    }

    public PublicUserData(ParseUser user, Student student, String facebookId, String displayName, ParseFile profilePic, byte[] profilePicData) {
//        //TODO: Remove this
//        if(facebookId != null && facebookId.equals("1082622075081952")) {
//            setObjectId("SJUOoOm3fi");
//        }
        setACL(ACLUtils.getPublicReadPrivateWriteACL());
        put(Columns.userType, Constants.UserType.STUDENT);
        put(Columns.baseUserId, user.getObjectId());
        put(Columns.student, student);
        put(Columns.displayName, displayName);
        if(facebookId != null)
            put(Columns.facebookId, facebookId);
        put(Columns.fbLinked, facebookId != null);

        if(profilePic != null)
            put(Columns.profilePic, profilePic);
        if(profilePicData != null)
            put(Columns.profilePicData, Arrays.asList(profilePicData));

        String searchableDisplayName = displayName;
        searchableDisplayName = searchableDisplayName.replaceAll("\\s+", "");
        searchableDisplayName = searchableDisplayName.toLowerCase();
        put(Columns.searchableDisplayName, searchableDisplayName);
    }

    public Student getStudent() throws ParseException {
        //TODO: Finish this
        return (Student) getParseObject(Columns.student).fetchIfNeeded();
    }

    public Tutor getTutor() throws ParseException {
        //TODO: Finish this
        Tutor tutor = (Tutor) getParseObject(Columns.tutor);
        tutor.fetchIfNeeded();
        return tutor;
    }

    //TODO: Add getTutor() method

    public String getUserType() { return getString(Columns.userType); }
    public String getDisplayName() { return getString(Columns.displayName); }
    public Date getLastSeen() { return getDate(Columns.lastSeen); }
    public ParseFile getProfilePic() { return getParseFile(Columns.profilePic); }
    public byte[] getProfilePicData() { return getBytes(Columns.profilePicData); }
    public String getBaseUserId() { return getString(Columns.baseUserId); }
    public String getSearchableDisplayName() { return getString(Columns.searchableDisplayName); }

    public static ParseQuery<PublicUserData> getQuery() {
        return ParseQuery.getQuery(PublicUserData.class);
    }

    public static ParseQuery<PublicUserData> getNonCurrentUserQuery(boolean ignoreTutors) {
        ParseQuery<PublicUserData> query = ParseQuery.getQuery(PublicUserData.class)
                .whereNotEqualTo(PublicUserData.Columns.baseUserId, UserUtils.getCurrentUserId())
                .whereContainedIn(PublicUserData.Columns.userType, Arrays.asList(Constants.UserType.nonComputerUserTypes));
        if(ignoreTutors)
            return query.whereEqualTo(Columns.userType, Constants.UserType.STUDENT);
        return query;
    }

    public static PublicUserData getPublicUserData() {
        return QueryUtils.getFirstCacheElseNetwork(new QueryUtils.ParseQueryBuilder<PublicUserData>() {
            @Override
            public ParseQuery<PublicUserData> buildQuery() {
                return PublicUserData.getQuery()
                        .whereEqualTo(Columns.baseUserId, UserUtils.getCurrentUserId());
            }
        });
//        return getPublicUserDataFromBaseUserId(ParseUser.getCurrentUser().getObjectId());
    }

    public static PublicUserData getPublicUserData(final String publicUserDataID) {
        return QueryUtils.getFirstCacheElseNetwork(new QueryUtils.ParseQueryBuilder<PublicUserData>() {
            @Override
            public ParseQuery<PublicUserData> buildQuery() {
                return PublicUserData.getQuery().whereEqualTo(Columns.objectId, publicUserDataID);
            }
        });
    }

    private static PublicUserData getPublicUserDataFromBaseUserId(final String baseUserId) {
        return QueryUtils.getFirstCacheElseNetwork(new QueryUtils.ParseQueryBuilder<PublicUserData>() {
            @Override
            public ParseQuery<PublicUserData> buildQuery() {
                return PublicUserData.getQuery().whereEqualTo(Columns.baseUserId, baseUserId);
            }
        });
    }

    public static Task<PublicUserData> getPublicUserDataInBackground() {
        return getPublicUserDataFromBaseUserIdInBackground(ParseUser.getCurrentUser().getObjectId());
    }

    public static Task<PublicUserData> getPublicUserDataFromBaseUserIdInBackground(final String baseUserId) {
        return QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<PublicUserData>() {
            @Override
            public ParseQuery<PublicUserData> buildQuery() {
                return PublicUserData.getQuery().whereEqualTo(Columns.baseUserId, baseUserId);
            }
        });
    }

    public static PublicUserData getComputerPublicUserData() {
        return QueryUtils.getFirstCacheElseNetwork(new QueryUtils.ParseQueryBuilder<PublicUserData>() {
            @Override
            public ParseQuery<PublicUserData> buildQuery() {
                return PublicUserData.getQuery().whereEqualTo(Columns.userType, Constants.UserType.COMPUTER);
            }
        });
    }

    private static ParseQuery<PublicUserData> getLocalDataStoreQuery(String column, String baseUserId) {

        return PublicUserData.getQuery()
                .fromLocalDatastore()
                .whereEqualTo(column, baseUserId);
    }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() + " | baseUserId: " + getBaseUserId() + " | " + getDisplayName();
    }
}
