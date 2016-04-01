package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

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
        public static final String lastMessage = "lastMessage";
    }

    public ParseRelation<Message> getMessages() { return getRelation(Columns.messages); }
    public PublicUserData getPublicUserData1() { return (PublicUserData) getParseObject(Columns.publicUserData1); }
    public PublicUserData getPublicUserData2() { return (PublicUserData) getParseObject(Columns.publicUserData2); }
    public String getBaseUserId1() { return getString(Columns.baseUserId1); }
    public String getBaseUserId2() { return getString(Columns.baseUserId2); }
//    public Message getLastMessage() { return (Message) getParseObject(Columns.lastMessage) };

    public Message getLastMessage() {
        try {
            Message message = getMessages().getQuery()
                    .orderByDescending(Constants.ParseObjectColumns.createdAt)
                    .getFirst();
            message.fetchIfNeeded();
            return message;
        } catch (ParseException e) { e.printStackTrace(); return null; }
    }

    public String getOtherUserBaseUserId() {
        if(UserUtils.getCurrentUserId().equals(getBaseUserId1()))
            return getBaseUserId2();
        return getBaseUserId1();
    }
    
    public PublicUserData getOtherUserPublicUserData() {
        if(getOtherUserBaseUserId().equals(getBaseUserId1()))
            return getPublicUserData1();
        return getPublicUserData2();
    }

    public Task<Void> addMessageAndSaveEventually(Message message) {
        getMessages().add(message);
        return saveEventually();
    }

    public static ParseQuery<Conversation> getQuery() { return ParseQuery.getQuery(Conversation.class); }

    public static ParseQuery<Conversation> getQueryForCurrentUserConversations() {
        List<ParseQuery<Conversation>> orQueries = new ArrayList<>();
        ParseQuery<Conversation> query1 = Conversation.getQuery()
                .whereEqualTo(Conversation.Columns.baseUserId1, UserUtils.getCurrentUserId());
        ParseQuery<Conversation> query2 = Conversation.getQuery()
                .whereEqualTo(Conversation.Columns.baseUserId2, UserUtils.getCurrentUserId());
        orQueries.add(query1);
        orQueries.add(query2);
        return ParseQuery.or(orQueries)
                .include(Conversation.Columns.publicUserData1)
                .include(Conversation.Columns.publicUserData2)
                .include(Conversation.Columns.lastMessage)
                .orderByDescending(Constants.ParseObjectColumns.updatedAt);
    }

    public static ParseQuery<Conversation> getQueryForOtherUserConversation(String otherUserBaseUserId) {
        List<ParseQuery<Conversation>> orQueries = new ArrayList<>();
        ParseQuery<Conversation> query1 = Conversation.getQuery()
                .whereEqualTo(Conversation.Columns.baseUserId1, UserUtils.getCurrentUserId())
                .whereEqualTo(Conversation.Columns.baseUserId2, otherUserBaseUserId);
        ParseQuery<Conversation> query2 = Conversation.getQuery()
                .whereEqualTo(Conversation.Columns.baseUserId2, UserUtils.getCurrentUserId())
                .whereEqualTo(Conversation.Columns.baseUserId1, otherUserBaseUserId);
        orQueries.add(query1);
        orQueries.add(query2);
        return ParseQuery.or(orQueries);
    }
}
