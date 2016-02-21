package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView.ViewHolder.*;

import com.cognitutor.cognistudyapp.Adapters.CogniParseQueryAdapter;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/14/2016.
 */
public class PeopleQueryAdapter extends ParseRecyclerQueryAdapter<ParseObject, PeopleQueryAdapter.ViewHolder> implements QueryUtils.OnDataLoadedListener<PublicUserData> {

    private Activity mActivity;
    private PeopleListOnClickHandler mOnClickHandler;
    private volatile String currentQuery;
    private static Lock mLock;
    private volatile List<ParseObject> lastSearchObjects;
    private int lastFilteredSize;
    private QueryUtilsCacheThenNetworkHelper mCacheThenNetworkHelper;
    private List<PublicUserData> cachedPublicUserDataList;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ParseObject object = mItems.get(position);

        final PublicUserData publicUserData = (PublicUserData) object;
        holder.imgProfile.setParseFile(publicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtName.setText(publicUserData.getDisplayName());

        holder.setOnClickListener(publicUserData);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mActivity, R.layout.list_item_people, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public PeopleQueryAdapter(Context context, PeopleListOnClickHandler onClickHandler) {
        super(new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                return getDefaultQuery();
            }
        }, false);
        mActivity = (Activity) context;
        mOnClickHandler = onClickHandler;
        mLock = new ReentrantLock();
        mCacheThenNetworkHelper = new QueryUtilsCacheThenNetworkHelper();
        try {
            cachedPublicUserDataList = getDefaultQuery().find();
        } catch (ParseException e) { e.printStackTrace(); cachedPublicUserDataList = null; }
        reset();
    }

    public synchronized void resetResultsToDefault() {

        mLock.lock();
        reset();
//        setQueryFactory(getDefaultQuery());
        loadObjects();
        mLock.unlock();
    }

    private void reset() {
        lastSearchObjects = null;
        lastFilteredSize = 0;
        currentQuery = "";
    }

    private static ParseQuery<PublicUserData> getDefaultQuery() {

        return PublicUserData.getQuery()
                .fromLocalDatastore()
                .whereContainedIn(PublicUserData.Columns.objectId, PrivateStudentData.getFriendPublicUserIds())
                .whereEqualTo(PublicUserData.Columns.fbLinked, true);
        //TODO: Add this?
        //.whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
    }

//    @Override
//    public View getItemView(ParseObject object, View view, ViewGroup parent) {
//        final ViewHolder holder;
//        if(view == null) {
//            view = View.inflate(mActivity, R.layout.list_item_people, null);
//            holder = createViewHolder(view);
//            view.setTag(holder);
//        }
//        else {
//            holder = (ViewHolder) view.getTag();
//        }
//
//        super.getItemView(object, view, parent);
//
//        final PublicUserData publicUserData = (PublicUserData) object;
//        holder.imgProfile.setParseFile(publicUserData.getProfilePic());
//        holder.imgProfile.loadInBackground();
//        holder.txtName.setText(publicUserData.getDisplayName());
//
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mOnClickHandler.onListItemClick(publicUserData);
//            }
//        });
//
//        return view;
//    }

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
    public Activity getActivityForUIThread() {
        return mActivity;
    }

    @Override
    public void onDataLoaded(List<PublicUserData> list) {
        mLock.lock();
        int oldNumItems = mItems.size();
        ConcurrentLinkedQueue<ParseObject> oldObjects = new ConcurrentLinkedQueue<>();
        for(ParseObject oldObj : mItems) {
            oldObjects.add(oldObj);
        }
        int firstChangedIdx = Integer.MAX_VALUE;
        int idx = 0;
        Iterator<ParseObject> oldIterator = oldObjects.iterator();
        while(oldIterator.hasNext()) {
            ParseObject oldObj = oldIterator.next();
            if(oldObj == null)
                break;
            PublicUserData oldPud = (PublicUserData) oldObj;
            boolean found = false;
            Iterator<PublicUserData> newIterator = list.iterator();
            while(newIterator.hasNext()) {
                PublicUserData newPud = newIterator.next();
                if(oldPud.getObjectId().equals(newPud.getObjectId())) {
                    found = true;
                    list.remove(newPud);
                    firstChangedIdx = Math.min(firstChangedIdx, idx);
                    break;
                }
            }
            if(!found) {
                mItems.remove(oldObj);
            }
            idx++;
        }
        List<ParseObject> converted = new ArrayList<>();
        for(PublicUserData pud : list) {
            converted.add(pud);
        }
        mItems.addAll(converted);
//        objects = converted;
        int newNumItems = mItems.size();
        for(int i=0; i<Math.min(oldNumItems, newNumItems); i++) {
            notifyItemChanged(i);
        }
        if(newNumItems > oldNumItems) {
            for(int i=oldNumItems; i<newNumItems; i++) {
                notifyItemInserted(i);
            }
        }
        else if(oldNumItems > newNumItems) {
            for(int i=newNumItems; i<oldNumItems; i++) {
                notifyItemRemoved(i);
            }
        }

//        notifyDataSetChanged();
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

        mCacheThenNetworkHelper.findCacheThenNetworkInBackgroundCancelleable(new QueryUtils.ParseQueryBuilder<PublicUserData>() {
            @Override
            public ParseQuery<PublicUserData> buildQuery() {
                return PublicUserData.getQuery()
                        .whereStartsWith(PublicUserData.Columns.searchableDisplayName, q);
            }
        }, thisAdapter, null, false)
        .continueWithTask(new Continuation<List<PublicUserData>, Task<List<PublicUserData>>>() {
            @Override
            public Task<List<PublicUserData>> then(Task<List<PublicUserData>> task) throws Exception {

                //If the user cancelled the search, the previous task will return null
                if (task.getResult() == null)
                    return null;

                return mCacheThenNetworkHelper.findCacheThenNetworkInBackgroundCancelleable(new QueryUtils.ParseQueryBuilder<PublicUserData>() {
                    @Override
                    public ParseQuery<PublicUserData> buildQuery() {
                        return PublicUserData.getQuery()
                                .whereContains(PublicUserData.Columns.searchableDisplayName, q);
                    }
                }, thisAdapter, null, false);
            }
        });
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
//                setQueryFactory(containsQuery);
                addOnQueryLoadListener(getSecondSearchListener());
//                loadObjects(mLock, lastSearchObjects.size() + 1);
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

//    private void setQueryFactory(final ParseQuery query) {
//        this.queryFactory = new QueryFactory<ParseObject>() {
//            @Override
//            public ParseQuery<ParseObject> create() {
//                return query;
//            }
//        };
//        removeAllOnQueryLoadListeners();
//    }

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

                if(list.size() == lastFilteredSize) {
                    mLock.unlock();
                    return;
                }
                lastFilteredSize = list.size();

//                clear();
                int oldNumItems = mItems.size();
//                mItems.clear();

                ConcurrentLinkedQueue<ParseObject> oldObjects = new ConcurrentLinkedQueue<>();
                for(ParseObject oldObj : mItems) {
                    oldObjects.add(oldObj);
                }
                int firstChangedIdx = Integer.MAX_VALUE;
                int idx = 0;
                Iterator<ParseObject> oldIterator = oldObjects.iterator();
                while(oldIterator.hasNext()) {
                    ParseObject oldObj = oldIterator.next();
                    if(oldObj == null)
                        break;
                    PublicUserData oldPud = (PublicUserData) oldObj;
                    boolean found = false;
                    Iterator<PublicUserData> newIterator = list.iterator();
                    while(newIterator.hasNext()) {
                        PublicUserData newPud = newIterator.next();
                        if(oldPud.getObjectId().equals(newPud.getObjectId())) {
                            found = true;
                            list.remove(newPud);
                            firstChangedIdx = Math.min(firstChangedIdx, idx);
                            break;
                        }
                    }
                    if(!found) {
                        mItems.remove(oldObj);
                    }
                    idx++;
                }
                List<ParseObject> converted = new ArrayList<>();
                for(PublicUserData pud : list) {
                    converted.add(pud);
                }
                mItems.addAll(converted);
//        objects = converted;
                int newNumItems = mItems.size();
                for(int i=0; i<Math.min(oldNumItems, newNumItems); i++) {
                    notifyItemChanged(i);
                }
                if(newNumItems > oldNumItems) {
                    for(int i=oldNumItems; i<newNumItems; i++) {
                        notifyItemInserted(i);
                    }
                }
                else if(oldNumItems > newNumItems) {
                    for(int i=newNumItems; i<oldNumItems; i++) {
                        notifyItemRemoved(i);
                    }
                }

//                notifyDataSetChanged();
                mLock.unlock();
            }
        };
    }

    private List<ParseObject> cloneLastSearch() {
        return (List<ParseObject>) ((ArrayList<ParseObject>) mItems).clone();
    }

}
