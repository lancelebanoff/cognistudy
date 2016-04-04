package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

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
        put(Columns.sentAt, new Date());
    }

    public class Columns {
        public static final String receiverBaseUserId = "receiverBaseUserId";
        public static final String senderBaseUserId = "senderBaseUserId";
        public static final String text = "text";
        public static final String sentAt = "sentAt";
    }

    public String getReceiverBaseUserId() { return getString(Columns.receiverBaseUserId); }
    public String getSenderBaseUserId() { return getString(Columns.senderBaseUserId); }
    public String getText() { return getString(Columns.text); }
    public Date getSentAt() { return getDate(Columns.sentAt); }

    public boolean isCurUserSender() {
        return getSenderBaseUserId().equals(UserUtils.getCurrentUserId());
    }

    public static ParseQuery<Message> getQuery() { return ParseQuery.getQuery(Message.class); }

    @Override
    public String toString() {
        return "objectId: " + (getObjectId() == null ? "_UNDEFINED" : getObjectId()) + " | " +
                "sender: " + getSenderBaseUserId() + " | " +
                "receiver: " + getReceiverBaseUserId() + " | " +
                "message: " + getText().substring(0, Math.min(getText().length(),30));
    }
}
