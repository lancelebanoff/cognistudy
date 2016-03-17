package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtilsCacheThenNetworkHelper;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/14/2016.
 */
public class PeopleQueryAdapter extends CogniRecyclerAdapter<PublicUserData, PeopleQueryAdapter.ViewHolder> {

    private Activity mActivity;
    private PeopleListOnClickHandler mOnClickHandler;
    private volatile String currentQuery;
    private volatile List<PublicUserData> lastSearchObjects;
    private Lock mLock;
    private QueryUtilsCacheThenNetworkHelper mCacheThenNetworkHelper;
    private List<PublicUserData> cachedPublicUserDataList;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final PublicUserData publicUserData = mItems.get(position);
        holder.imgProfile.setParseFile(publicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtName.setText(publicUserData.getDisplayName());

        holder.setOnClickListener(publicUserData);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_people, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public PeopleQueryAdapter(Activity activity, PeopleListOnClickHandler onClickHandler) {
        super(activity, new ParseQueryAdapter.QueryFactory<PublicUserData>() {
            public ParseQuery create() {
                return getDefaultQuery();
            }
        }, true); //TODO: Try true for hasStableIds
        mOnClickHandler = onClickHandler;
        mCacheThenNetworkHelper = new QueryUtilsCacheThenNetworkHelper();
        mLock = new ReentrantLock();
        getDefaultQuery().findInBackground().continueWith(new Continuation<List<PublicUserData>, Object>() {
//        getImportantCachedPublicUserDatas().continueWith(new Continuation<List<PublicUserData>, Object>() {
            @Override
            public Object then(Task<List<PublicUserData>> task) throws Exception {
                if (!task.isFaulted())
                    cachedPublicUserDataList = task.getResult();
                else
                    cachedPublicUserDataList = null;
                reset();
                return null;
            }
        });
    }

    public synchronized void resetResultsToDefault() {

        mLock.lock();
        reset();
        loadObjects(); //TODO: delete this?
        mLock.unlock();
    }

    private void reset() {
        lastSearchObjects = null;
        currentQuery = "";
    }

    private static ParseQuery<PublicUserData> getDefaultQuery() {

        return PublicUserData.getQuery()
                .fromLocalDatastore();
//                .whereContainedIn(PublicUserData.Columns.objectId, PrivateStudentData.getFriendPublicUserIds())
//                .whereEqualTo(PublicUserData.Columns.fbLinked, true);
        //TODO: Add this?
        //.whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
    }

    private static Task<ParseQuery<PublicUserData>> getImportantCachedPublicUserDataQuery() {

        final List<ParseQuery<PublicUserData>> queries = new ArrayList<>();
        queries.add(PublicUserData.getQuery().fromPin(Constants.PinNames.CurrentUser));

        return Challenge.getQuery()
                .fromLocalDatastore()
                .findInBackground().continueWith(new Continuation<List<Challenge>, ParseQuery<PublicUserData>>() {
                    @Override
                    public ParseQuery<PublicUserData> then(Task<List<Challenge>> task) throws Exception {
                        for (Challenge challenge : task.getResult()) {
                            queries.add(PublicUserData.getQuery().fromPin(challenge.getObjectId()));
                        }
                        return ParseQuery.or(queries);
                    }
                });
    }

    private static Task<List<PublicUserData>> getImportantCachedPublicUserDatas() {
        return getImportantCachedPublicUserDataQuery()
                .continueWithTask(new Continuation<ParseQuery<PublicUserData>, Task<List<PublicUserData>>>() {
                    @Override
                    public Task<List<PublicUserData>> then(Task<ParseQuery<PublicUserData>> task) throws Exception {
                        return task.getResult().findInBackground();
                    }
                });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public RoundedImageView imgProfile;
        private View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            imgProfile = (RoundedImageView) itemView.findViewById(R.id.imgProfileRounded);
        }

        public void setOnClickListener(final PublicUserData publicUserData) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickHandler.onListItemClick(publicUserData);
                }
            });
        }
    }

    @Override
    public void onDataLoaded(List<PublicUserData> list) {
        mLock.lock();
        super.onDataLoaded(list);
        lastSearchObjects = cloneLastSearch();
        mLock.unlock();
    }

    public void search(final String queryText) {

        final String q = convertToSearchable(queryText);
        final PeopleQueryAdapter thisAdapter = this;

        if(currentQuery.equals(q))
            return;
        currentQuery = q;

        mCacheThenNetworkHelper.cancelAllQueries();

        mCacheThenNetworkHelper.findCacheThenNetworkInBackgroundCancelleable(Constants.PinNames.PeopleSearch,
                false, thisAdapter, new QueryUtils.ParseQueryBuilder <PublicUserData> () {
                    @Override
                    public ParseQuery<PublicUserData> buildQuery() {
                        return PublicUserData.getQuery()
                                .whereStartsWith(PublicUserData.Columns.searchableDisplayName, q);
                    }
        })
        .continueWithTask(new Continuation<List<PublicUserData>, Task<List<PublicUserData>>>() {
            @Override
            public Task<List<PublicUserData>> then(Task<List<PublicUserData>> task) throws Exception {
                //If the user cancelled the search, the previous task will return null
                if (task.getResult() == null)
                    return null;
                return mCacheThenNetworkHelper.findCacheThenNetworkInBackgroundCancelleable(Constants.PinNames.PeopleSearch,
                        false, thisAdapter, new QueryUtils.ParseQueryBuilder<PublicUserData>() {
                            @Override
                            public ParseQuery<PublicUserData> buildQuery() {
                                return PublicUserData.getQuery()
                                        .whereContains(PublicUserData.Columns.searchableDisplayName, q);
                            }
                });
            }
        }).continueWith(new Continuation<List<PublicUserData>, Object>() {
            @Override
            public Object then(Task<List<PublicUserData>> task) throws Exception {
                ParseObjectUtils.unpinAllInBackground(Constants.PinNames.PeopleSearch);
                return null;
            }
        });
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

                mCacheThenNetworkHelper.cancelAllQueries();
                mLock.lock();
                String q = convertToSearchable(constraint.toString());
                FilterResults result = new FilterResults();
                List<PublicUserData> listToFilter;
                if(lastSearchObjects != null)
                    listToFilter = lastSearchObjects;
                else
                    listToFilter = mItems;

                List<PublicUserData> cachedPudsToAdd = new ArrayList<>();
                for(PublicUserData fromCache : cachedPublicUserDataList) {
                    boolean found = false;
                    for(ParseObject fromListParseObject : listToFilter) {
                        PublicUserData fromList = (PublicUserData) fromListParseObject;
                        if(fromCache.getObjectId().equals(fromList.getObjectId())) {
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        cachedPudsToAdd.add(fromCache);
                    }
                }
                listToFilter.addAll(cachedPudsToAdd);

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
                callSuperOnDataLoaded(list);
                mLock.unlock();
            }
        };
    }

    private void callSuperOnDataLoaded(List<PublicUserData> list) {
        super.onDataLoaded(list);
    }

    private List<PublicUserData> cloneLastSearch() {
        return (List<PublicUserData>) ((ArrayList<PublicUserData>) mItems).clone();
    }
}
