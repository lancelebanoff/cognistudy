package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 3/30/2016.
 */
@ParseClassName("Message")
public class Message extends ParseObject {

    public Message() {}
    public Message(Conversation conversation, String text) {
        put(Columns.senderBaseUserId, UserUtils.getCurrentUserId());
        put(Columns.receiverBaseUserId, conversation.getOtherUserBaseUserId());
        put(Columns.text, text);
    }

    public class Columns {
        public static final String receiverBaseUserId = "receiverBaseUserId";
        public static final String senderBaseUserId = "senderBaseUserId";
        public static final String text = "text";
    }

    public String getReceiverBaseUserId() { return getString(Columns.receiverBaseUserId); }
    public String getSenderBaseUserId() { return getString(Columns.senderBaseUserId); }
    public String getText() { return getString(Columns.text); }

    public boolean isCurUserSender() {
        return getSenderBaseUserId().equals(UserUtils.getCurrentUserId());
    }

    public static ParseQuery<Message> getQuery() { return ParseQuery.getQuery(Message.class); }
}
