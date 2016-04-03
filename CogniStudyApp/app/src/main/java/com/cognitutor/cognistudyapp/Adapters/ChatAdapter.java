package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.ChatActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Message;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 4/2/2016.
 */
public class ChatAdapter extends CogniRecyclerAdapter<Message, ChatAdapter.MessageViewHolder> {

    private PublicUserData mConversantPud;
    private ChatActivity mChatActivity;

    public ChatAdapter(Activity activity, PublicUserData conversantPud, final String conversantBaseUserId) {
        super(activity, new ParseQueryAdapter.QueryFactory<Message>() {
            @Override
            public ParseQuery<Message> create() {
                return getLocalOrQuery(conversantBaseUserId);
            }
        }, true);
        mChatActivity = (ChatActivity) activity;
        mConversantPud = conversantPud;
        addOnDataSetChangedListener(new OnDataSetChangedListener() {
            @Override
            public void onDataSetChanged() {
                mChatActivity.scrollToBottom();
            }
        });
    }

    public void loadFromNetwork(final Conversation conversation) {
        getRelationQuery(conversation).findInBackground().continueWith(new Continuation<List<Message>, Object>() {
            @Override
            public Object then(Task<List<Message>> task) throws Exception {
                final List<Message> list = task.getResult();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onDataLoaded(list);
                    }
                });
                ParseObject.pinAllInBackground(conversation.getObjectId(), list);
                return null;
            }
        });
    }

    private static ParseQuery<Message> getLocalOrQuery(String conversantBaseUserId) {
        List<ParseQuery<Message>> queries = new ArrayList<>();
        ParseQuery<Message> orQuery1 = Message.getQuery().whereEqualTo(Message.Columns.receiverBaseUserId, conversantBaseUserId);
        ParseQuery<Message> orQuery2 = Message.getQuery().whereEqualTo(Message.Columns.senderBaseUserId, conversantBaseUserId);
        queries.add(orQuery1);
        queries.add(orQuery2);
        return ParseQuery.or(queries).fromLocalDatastore().orderByAscending(Message.Columns.sentAt);
    }

    private static ParseQuery<Message> getRelationQuery(Conversation conversation) {
        return conversation.getMessages().getQuery().orderByAscending(Message.Columns.sentAt);
    }

    static final int CUR_USER = 0;
    static final int CONVERSANT = 1;

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case CUR_USER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message_cur_user, null);
                return new ViewHolderCurUserSender(view);
            default: //CONVERSANT
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message_conversant, null);
                return new ViewHolderConversantSender(view);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = getItem(position);
        holder.txtMessage.setText(message.getText());
        holder.txtTime.setText(DateUtils.getTimeOrDate(message.getSentAt()));

        if(getItemViewType(position) == CONVERSANT) {
            ViewHolderConversantSender convHolder = (ViewHolderConversantSender) holder;
            if(!convHolder.isProfilePicSet) {
                RoundedImageView imgProfileConversant = convHolder.imgProfileConversant;
                imgProfileConversant.setParseFile(mConversantPud.getProfilePic());
                convHolder.isProfilePicSet = true;
                imgProfileConversant.loadInBackground();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isCurUserSender() ? CUR_USER : CONVERSANT;
    }

    public class ViewHolderCurUserSender extends MessageViewHolder {

        public ViewHolderCurUserSender(View itemView) {
            super(itemView);
        }
    }

    public class ViewHolderConversantSender extends MessageViewHolder {

        public boolean isProfilePicSet;
        public RoundedImageView imgProfileConversant;

        public ViewHolderConversantSender(View itemView) {
            super(itemView);
            imgProfileConversant = (RoundedImageView) itemView.findViewById(R.id.imgProfileConversant);
            isProfilePicSet = false;
        }
    }

    public abstract class MessageViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        public RelativeLayout rlContent;
        public LinearLayout llChatBubble;
        public TextView txtMessage;
        public TextView txtTime;

        public MessageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            rlContent = (RelativeLayout) itemView.findViewById(R.id.rlContent);
            llChatBubble = (LinearLayout) itemView.findViewById(R.id.llChatBubble);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
            txtTime = (TextView) itemView.findViewById(R.id.txtTime);
        }
    }
}
