package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Adapters.CogniParseQueryAdapter;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
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
    private static Lock mLock;
    private volatile List<ParseObject> lastSearchObjects;
    private int lastFilteredSize;

    public PeopleQueryAdapter(Context context, PeopleListOnClickHandler onClickHandler) {
        super(context, new CogniParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                return getDefaultQuery();
            }
        });
        mActivity = (Activity) context;
        mOnClickHandler = onClickHandler;
        mLock = new ReentrantLock();
        reset();
    }

    public synchronized void resetResultsToDefault() {

        mLock.lock();
        reset();
        setQueryFactory(getDefaultQuery());
        loadObjects();
        mLock.unlock();
    }

    private void reset() {
        lastSearchObjects = null;
        lastFilteredSize = 0;
        currentQuery = "";
    }

    private static ParseQuery getDefaultQuery() {

        return PublicUserData.getQuery()
                .fromLocalDatastore()
                .whereContainedIn(PublicUserData.Columns.objectId, PrivateStudentData.getFriendPublicUserIds())
                .whereEqualTo(PublicUserData.Columns.fbLinked, true);
        //TODO: Add this?
        //.whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
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

    public void search(final String queryText) {

        final String q = convertToSearchable(queryText);

        if(currentQuery.equals(q))
            return;
        currentQuery = q;

        mLock.lock();

        ParseQuery<PublicUserData> startsWithQuery = PublicUserData.getQuery()
                .whereStartsWith(PublicUserData.Columns.searchableDisplayName, q);

        setQueryFactory(startsWithQuery);
        addOnQueryLoadListener(getFirstSearchListener(q));

        loadObjects(mLock);
        mLock.unlock();
    }

    private OnQueryLoadListener getFirstSearchListener(final String q) {
        return new OnQueryLoadListener() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(List objects, Exception e) {
                mLock.lock();

                removeOnQueryLoadListener(this);

                //TODO: if user is still typing, cancel this?
                if(!currentQuery.equals(q) || objects == null) {
                    mLock.unlock();
                    return;
                }
                lastSearchObjects = cloneLastSearch();
                ParseQuery<PublicUserData> containsQuery = PublicUserData.getQuery()
                        .whereContains(PublicUserData.Columns.searchableDisplayName, q);
                setQueryFactory(containsQuery);
                addOnQueryLoadListener(getSecondSearchListener());
                loadObjects(mLock, lastSearchObjects.size() + 1);
                mLock.unlock();
            }
        };
    }

    private OnQueryLoadListener getSecondSearchListener() {
        return new OnQueryLoadListener() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(List objects, Exception e) {

                mLock.lock();

                removeOnQueryLoadListener(this);
                lastSearchObjects = cloneLastSearch();

                mLock.unlock();
            }
        };
    }

    private void setQueryFactory(final ParseQuery query) {
        this.queryFactory = new QueryFactory<ParseObject>() {
            @Override
            public ParseQuery<ParseObject> create() {
                return query;
            }
        };
        removeAllOnQueryLoadListeners();
    }

    private String convertToSearchable(String q) {

        q = q.replaceAll("\\s+", "");
        q = q.toLowerCase();
        return q;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                mLock.lock();
                String q = convertToSearchable(constraint.toString());
                FilterResults result = new FilterResults();
                List<ParseObject> listToFilter;
                if(lastSearchObjects != null)
                    listToFilter = lastSearchObjects;
                else
                    listToFilter = objects;

                if(q != null && q.length() > 0) {
                    List<PublicUserData> filtered = new ArrayList<>();
                    for(ParseObject object : listToFilter) {
                        PublicUserData pud = (PublicUserData) object;
                        if(pud.getSearchableDisplayName().contains(q)) {
                            filtered.add(pud);
                        }
                    }
                    result.values = filtered;
                    result.count = filtered.size();
                }
                else {
                    result.values = listToFilter;
                    result.count = listToFilter.size();
                }
                mLock.unlock();
                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mLock.lock();

                List<PublicUserData> list = (List<PublicUserData>) results.values;

                if(list.size() == lastFilteredSize) {
                    mLock.unlock();
                    return;
                }

                clear();
                lastFilteredSize = list.size();
                for(PublicUserData pud : list) {
                    objects.add(pud);
                }
                notifyDataSetChanged();
                mLock.unlock();
            }
        };
    }

    private List<ParseObject> cloneLastSearch() {
        return (List<ParseObject>) ((ArrayList<ParseObject>) objects).clone();
    }

}
