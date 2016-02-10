package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

/**
 * Created by Kevin on 1/14/2016.
 */
public class PeopleQueryAdapter extends ParseQueryAdapter<ParseObject> {

    private Activity mActivity;
    private PeopleListOnClickHandler mOnClickHandler;

    /*
    public PeopleQueryAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = PublicUserData.getQuery()
                        .fromLocalDatastore()
                        .whereEqualTo(PublicUserData.Columns.fbLinked, true)
                        .whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
                return query;
            }
        });
    }
    */
    public PeopleQueryAdapter(Context context, PeopleListOnClickHandler onClickHandler) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = PublicUserData.getQuery()
                        .fromLocalDatastore()
                        .whereContainedIn(PublicUserData.Columns.objectId, PrivateStudentData.getFriendPublicUserIds())
                        .whereEqualTo(PublicUserData.Columns.fbLinked, true);
                //.whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
                return query;
            }
        });
        mActivity = (Activity) context;
        mOnClickHandler = onClickHandler;
    }

    @Override
    public View getItemView(ParseObject object, View view, ViewGroup parent) {
        final ViewHolder holder;
        if(view == null) {
            view = View.inflate(getContext(), R.layout.list_item_people, null);
            holder = createViewHolder(view);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        super.getItemView(object, view, parent);

        final PublicUserData publicUserData = (PublicUserData) object;
        holder.imgProfile.setParseFile(publicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtName.setText(publicUserData.getDisplayName());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickHandler.onListItemClick(publicUserData);
            }
        });

        return view;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) v.findViewById(R.id.txtName);
        holder.imgProfile = (RoundedImageView) v.findViewById(R.id.imgProfileRounded);
        return holder;
    }

    private static class ViewHolder {
        public TextView txtName;
        public RoundedImageView imgProfile;
    }
}
