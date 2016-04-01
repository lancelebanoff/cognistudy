package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Fragments.PeopleFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 3/31/2016.
 */
public class NewConversationActivity extends PeopleFragmentActivity {


    @Override
    protected PeopleListOnClickHandler getPeopleListOnClickHandler() {
        return new PeopleListOnClickHandler() {
            @Override
            public void onListItemClick(PublicUserData publicUserData) {
                chooseConversant(publicUserData);
            }
        };
    }

    private void chooseConversant(final PublicUserData publicUserData) {

        getExistingConversation(publicUserData.getBaseUserId()).continueWith(new Continuation<Conversation, Object>() {
            @Override
            public Object then(Task<Conversation> task) throws Exception {
                Conversation conversation = task.getResult();
                if(conversation != null) {
                    navigateToChatActivity(conversation.getObjectId(), conversation.getOtherUserBaseUserId());
                }
                else {
                    navigateToChatActivity(null, publicUserData.getBaseUserId());
                }
                return null;
            }
        });
    }

    private Task<Conversation> getExistingConversation(final String otherUserBaseUserId) {
        return QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<Conversation>() {
            @Override
            public ParseQuery<Conversation> buildQuery() {
                return Conversation.getQueryForOtherUserConversation(otherUserBaseUserId);
            }
        });
    }

    private void navigateToChatActivity(String conversationId, String baseUserId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.IntentExtra.CONVERSATION_ID, conversationId);
        intent.putExtra(Constants.IntentExtra.BASEUSERID, baseUserId);
        startActivity(intent);
        finish();
    }
}
