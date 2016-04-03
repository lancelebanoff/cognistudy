package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.ChatActivity;
import com.cognitutor.cognistudyapp.Activities.MainActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.Fragments.ConversationsFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Message;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

/**
 * Created by Kevin on 3/30/2016.
 */
public class ConversationAdapter extends CogniRecyclerAdapter<Conversation, ConversationAdapter.ViewHolder> {

    private ConversationsFragment mConversationsFragment;

    public ConversationAdapter(Activity activity, ConversationsFragment conversationsFragment) {
        super(activity, new ParseQueryAdapter.QueryFactory<Conversation>() {
            @Override
            public ParseQuery<Conversation> create() {
                return Conversation.getQueryForCurrentUserConversations();
            }
        }, true);
        mConversationsFragment = conversationsFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_conversation, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Conversation conversation = getItem(position);
        PublicUserData pud = conversation.getOtherUserPublicUserData();
        Message lastMessage = conversation.getLastMessage();
        holder.imgConversationProfile.setParseFile(pud.getProfilePic());
        holder.imgConversationProfile.loadInBackground();
        holder.txtConversationUserName.setText(pud.getDisplayName());
        holder.txtConversationUserName.setTypeface(null, Typeface.BOLD);
        holder.txtLastMessageSubstring.setText(lastMessage.getText());
        holder.txtConversationDate.setText(DateUtils.getTimeOrDate(conversation.getUpdatedAt()));

        holder.setOnClickListener(pud);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public RoundedImageView imgConversationProfile;
        public TextView txtConversationUserName;
        public TextView txtLastMessageSubstring;
        public TextView txtConversationDate;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imgConversationProfile = (RoundedImageView) itemView.findViewById(R.id.imgConversationProfile);
            txtConversationUserName = (TextView) itemView.findViewById(R.id.txtConversationUserName);
            txtLastMessageSubstring = (TextView) itemView.findViewById(R.id.txtLastMessageSubstring);
            txtConversationDate = (TextView) itemView.findViewById(R.id.txtConversationDate);
        }

        public void setOnClickListener(final PublicUserData pud) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToChatActivity(pud);
                }
            });
        }
    }

    public void navigateToChatActivity(PublicUserData publicUserData) {
        Intent intent = new Intent(mActivity, ChatActivity.class);
        intent.putExtra(Constants.IntentExtra.BASEUSERID, publicUserData.getBaseUserId());
        intent.putExtra(Constants.IntentExtra.CONVERSANT_DISPLAY_NAME, publicUserData.getDisplayName());
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.MAIN_ACTIVITY);
        ChatActivity.setConversantPud(publicUserData);
        mConversationsFragment.startActivityForResult(intent, ConversationsFragment.REQUEST_CODE);
    }
}
