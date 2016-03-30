package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import bolts.Task;

/**
 * Created by Kevin on 3/30/2016.
 */
@ParseClassName("Conversation")
public class Conversation extends ParseObject {

    public Conversation() {}

    public class Columns {
        public static final String messages = "messages";
        public static final String publicUserData1 = "publicUserData1";
        public static final String publicUserData2 = "publicUserData2";
        public static final String baseUserId1 = "baseUserId1";
        public static final String baseUserId2 = "baseUserId2";
    }

    public ParseRelation<Message> getMessages() { return getRelation(Columns.messages); }
    public PublicUserData getPublicUserData1() { return (PublicUserData) getParseObject(Columns.publicUserData1); }
    public PublicUserData getPublicUserData2() { return (PublicUserData) getParseObject(Columns.publicUserData2); }
    public String getBaseUserId1() { return getString(Columns.baseUserId1); }
    public String getBaseUserId2() { return getString(Columns.baseUserId2); }

    public String getOtherUserBaseUserId() {
        if(UserUtils.getCurrentUserId().equals(getBaseUserId1()))
            return getBaseUserId2();
        return getBaseUserId1();
    }
    
    public PublicUserData getOtherUserPublicUserData() {
        if(getOtherUserBaseUserId().equals(getPublicUserData1().getBaseUserId()))
            return getPublicUserData1();
        return getPublicUserData2();
    }

    public Task<Void> addMessageAndSaveEventually(Message message) {
        getMessages().add(message);
        return saveEventually();
    }

    public static ParseQuery<Conversation> getQuery() { return ParseQuery.getQuery(Conversation.class); }
}
