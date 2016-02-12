package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Adapters.CogniParseQueryAdapter;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Kevin on 1/14/2016.
 */
public class PeopleQueryAdapter extends CogniParseQueryAdapter<ParseObject> {

    private Activity mActivity;
    private PeopleListOnClickHandler mOnClickHandler;
    private volatile String currentQuery;
    private volatile int prevSize;
    private static Lock mLock;

    public PeopleQueryAdapter(Context context, PeopleListOnClickHandler onClickHandler) {
        super(context, new CogniParseQueryAdapter.QueryFactory<ParseObject>() {
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
        mLock = new ReentrantLock();
        prevSize = 0;
        currentQuery = "";
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

    public void search(final String q) {

        if(currentQuery.equals(q))
            return;
        currentQuery = q;

        mLock.lock();

        ParseQuery<PublicUserData> startsWithQuery = PublicUserData.getQuery()
                .whereStartsWith(PublicUserData.Columns.searchableDisplayName, q);

        setQueryFactory(startsWithQuery);

        OnQueryLoadListener listener = new OnQueryLoadListener() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(List objects, Exception e) {
                mLock.lock();

                removeOnQueryLoadListener(this);

                if(!currentQuery.equals(q) || objects == null) {
                    mLock.unlock();
                    return;
                }
                prevSize = objects.size();
                ParseQuery<PublicUserData> containsQuery = PublicUserData.getQuery()
                        .whereContains(PublicUserData.Columns.searchableDisplayName, q);
                setQueryFactory(containsQuery);
                loadObjects(mLock, prevSize + 1);
                mLock.unlock();
            }
        };
        removeAllOnQueryLoadListeners();
        addOnQueryLoadListener(listener);

        loadObjects(mLock);
        mLock.unlock();
    }

    private void setQueryFactory(final ParseQuery query) {
        this.queryFactory = new QueryFactory<ParseObject>() {
            @Override
            public ParseQuery<ParseObject> create() {
                return query;
            }
        };
    }

}
