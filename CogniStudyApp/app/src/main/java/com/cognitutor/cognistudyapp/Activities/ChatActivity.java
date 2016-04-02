package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.CommonUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseQuery;

import bolts.Continuation;
import bolts.Task;

public class ChatActivity extends CogniActivity {

    private Intent mIntent;
    private PublicUserData mConversantPud;
    private Conversation mConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mIntent = getIntent();
        setActivityTitle();
        loadExistingConversationAndConversant();
    }

    private void setActivityTitle() {
        String conversantDisplayName = mIntent.getStringExtra(Constants.IntentExtra.CONVERSANT_DISPLAY_NAME);
        setTitle(conversantDisplayName);
    }

    private Task<PublicUserData> loadExistingConversationAndConversant() {
        final String baseUserId = getConversantBaseUserId();
        return QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<Conversation>() {
            @Override
            public ParseQuery<Conversation> buildQuery() {
                return Conversation.getQueryForConversant(baseUserId);
            }
        }).continueWithTask(new Continuation<Conversation, Task<PublicUserData>>() {
            @Override
            public Task<PublicUserData> then(Task<Conversation> task) throws Exception {
                mConversation = task.getResult();
                if (mConversation != null)
                    return loadConversantFromConversation();
                else
                    return loadConversantFromBaseUserId();
            }
        });
    }

    private Task<PublicUserData> loadConversantFromConversation() {
        mConversantPud = mConversation.getOtherUserPublicUserData();
        try {
            mConversantPud.fetchFromLocalDatastore();
        } catch (ParseException e) { e.printStackTrace(); }
        if(!mConversantPud.isDataAvailable()) {
            return mConversantPud.fetchIfNeededInBackground();
        }
        return CommonUtils.getCompletionTask(mConversantPud);
    }

    private Task<PublicUserData> loadConversantFromBaseUserId() {
        final String baseUserId = getConversantBaseUserId();
        return PublicUserData.getPublicUserDataFromBaseUserIdInBackground(baseUserId).continueWith(new Continuation<PublicUserData, PublicUserData>() {
            @Override
            public PublicUserData then(Task<PublicUserData> task) throws Exception {
                mConversantPud = task.getResult();
                return mConversantPud;
            }
        });
    }

    private String getConversantBaseUserId() {
        return mIntent.getStringExtra(Constants.IntentExtra.BASEUSERID);
    }
}
