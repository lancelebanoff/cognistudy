package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.EditText;

import com.cognitutor.cognistudyapp.Adapters.ChatAdapter;
import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.CogniRecyclerView;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.CommonUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

import bolts.Continuation;
import bolts.Task;

public class ChatActivity extends CogniActivity {

    private static PublicUserData mConversantPud;

    private Intent mIntent;
    private Conversation mConversation;
    private ChatAdapter mChatAdapter;
    public CogniRecyclerView mRecyclerView;
    private boolean initFinished;
    private EditText mEditText;
    private CogniButton mBtnSendMsg;

    public static void setConversantPud(PublicUserData pud) {
        mConversantPud = pud;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mIntent = getIntent();
        setActivityTitle();

        mEditText = (EditText) findViewById(R.id.editText);
        mBtnSendMsg = (CogniButton) findViewById(R.id.btnSendMsg);

        mRecyclerView = (CogniRecyclerView) findViewById(R.id.rvChatMessages);
        mChatAdapter = new ChatAdapter(this, mConversantPud, getConversantBaseUserId());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mChatAdapter);

        initFinished = false;
        loadConversationAndConversant()
            .continueWithTask(fetchConversation())
            .continueWithTask(loadMessages());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(initFinished)
            loadMessagesIfNecessary();
    }

    private void setActivityTitle() {
        String conversantDisplayName = mIntent.getStringExtra(Constants.IntentExtra.CONVERSANT_DISPLAY_NAME);
        setTitle(conversantDisplayName);
    }

    private Continuation<Boolean, Task<Void>> loadMessages() {
        return new Continuation<Boolean, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Boolean> task) throws Exception {
                mChatAdapter.loadFromNetwork(mConversation);
                return null;
            }
        };
    }

    private void loadMessagesIfNecessary() {
        doFetchConversation().continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if(task.getResult()) {
                    loadMessages();
                }
                return null;
            }
        });
    }

    private Continuation<Void, Task<Boolean>> fetchConversation() {
        return new Continuation<Void, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Void> task) throws Exception {
                return doFetchConversation();
            }
        };
    }

    private Task<Boolean> doFetchConversation() {
        final Date lastUpdated = mConversation.getUpdatedAt();
        if(lastUpdated != null) {
            return mConversation.fetchInBackground().continueWith(new Continuation<ParseObject, Boolean>() {
                @Override
                public Boolean then(Task<ParseObject> task) throws Exception {
                    if(mConversation.getUpdatedAt().after(lastUpdated))
                        return true;
                    return false;
                }
            });
        }
        else {
            return CommonUtils.getCompletionTask(false);
        }
    }

    private Task<Void> loadConversationAndConversant() {

        return loadConversationCacheElseNetwork()
                .continueWithTask(loadConversant())
                .continueWith(createConversationIfNecessary());
    }

    //We will always fetch the conversation after loading from cache. Loading from cache ensures that if the network is down
    // the user can still see the cached messages
    private Task<Conversation> loadConversationCacheElseNetwork() {
        return QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<Conversation>() {
            @Override
            public ParseQuery<Conversation> buildQuery() {
                return Conversation.getQueryForConversant(getConversantBaseUserId());
            }
        });
    }

    private Continuation<Conversation, Task<PublicUserData>> loadConversant() {
        return new Continuation<Conversation, Task<PublicUserData>>() {
            @Override
            public Task<PublicUserData> then(Task<Conversation> task) throws Exception {
                mConversation = task.getResult();
                if (mConversation != null) {
                    return loadConversantFromConversation();
                }
                else {
                    return loadConversantFromBaseUserId();
                }
            }
        };
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

    private Continuation<PublicUserData, Void> createConversationIfNecessary() {
        return new Continuation<PublicUserData, Void>() {
            @Override
            public Void then(Task<PublicUserData> task) throws Exception {
                if(mConversation == null) {
                    mConversation = new Conversation(UserUtils.getCurrentUserId(), getConversantBaseUserId(),
                            PublicUserData.getPublicUserData(), mConversantPud);
                }
                return null;
            }
        };
    }

    private String getConversantBaseUserId() {
        return mIntent.getStringExtra(Constants.IntentExtra.BASEUSERID);
    }

    private Bitmap getConversantProfilePicBitmap() {
        return decodeByteArray(mIntent.getByteArrayExtra(Constants.IntentExtra.CONVERSANT_PROFILE_PIC_DATA));
    }

    private Bitmap decodeByteArray(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length, new BitmapFactory.Options());
    }
}
