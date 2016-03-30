package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 3/30/2016.
 */
public class ConversationAdapter extends CogniRecyclerAdapter<Conversation, ConversationAdapter.ViewHolder> {

    public ConversationAdapter(Activity activity) {
        super(activity, new ParseQueryAdapter.QueryFactory<Conversation>() {
            @Override
            public ParseQuery<Conversation> create() {
                return getDefaultQuery();
            }
        }, true);
    }

    private static ParseQuery<Conversation> getDefaultQuery() {
        List<ParseQuery<Conversation>> orQueries = new ArrayList<>();
        ParseQuery<Conversation> query1 = Conversation.getQuery()
                .whereEqualTo(Conversation.Columns.baseUserId1, UserUtils.getCurrentUserId());
        ParseQuery<Conversation> query2 = Conversation.getQuery()
                .whereEqualTo(Conversation.Columns.baseUserId2, UserUtils.getCurrentUserId());
        orQueries.add(query1);
        orQueries.add(query2);
        return ParseQuery.or(orQueries);
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
        holder.imgConversationProfile.setParseFile(pud.getProfilePic());
        holder.imgConversationProfile.loadInBackground();
        holder.txtConversationUserName.setText(pud.getDisplayName());
        holder.txtLastMessageSubstring.setText("blah"); //TODO
        holder.txtConversationDate.setText(conversation.getUpdatedAt().toString()); //TODO
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

        public void setOnClickListener() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO
                }
            });
        }
    }
}
