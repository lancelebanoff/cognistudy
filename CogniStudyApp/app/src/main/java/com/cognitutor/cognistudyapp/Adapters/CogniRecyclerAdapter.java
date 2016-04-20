package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import bolts.Capture;

/**
 * Created by Kevin on 2/21/2016.
 */
public abstract class CogniRecyclerAdapter<T extends ParseObject, U extends RecyclerView.ViewHolder>
        extends ParseRecyclerQueryAdapter<T, U> implements QueryUtils.OnDataLoadedListener<T> {

    protected Activity mActivity;
    private HashMap<String, Date> mLastSeenUpdatedAtMap;

    public CogniRecyclerAdapter(Activity activity, final ParseQueryAdapter.QueryFactory<T> factory, final boolean hasStableIds) {
        super(factory, hasStableIds);
        mActivity = activity;
        mLastSeenUpdatedAtMap = new HashMap<>();
        addOnDataSetChangedListener(new OnDataSetChangedListener() {
            @Override
            public void onDataSetChanged() {
                for(T obj : mItems) {
                    setLastSeenUpdatedAt(obj);
                }
            }
        });
    }

    public synchronized void notifyObjectIdChanged(final String objectId) {
        getActivityForUIThread().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < getItemCount(); i++) {
                    if (getItem(i).getObjectId().equals(objectId)) {
                        notifyItemChanged(i);
                    }
                }
            }
        });
    }

    @Override
    public Activity getActivityForUIThread() {
        return mActivity;
    }

    private Date getLastSeenUpdatedAt(T obj) {
        return mLastSeenUpdatedAtMap.get(obj.getObjectId());
    }

    private boolean hasBeenSeen(T obj) {
        return mLastSeenUpdatedAtMap.containsKey(obj.getObjectId());
    }

    private void setLastSeenUpdatedAt(T obj) {
        mLastSeenUpdatedAtMap.put(obj.getObjectId(), obj.getUpdatedAt());
    }

    @Override
    public synchronized List<T> onDataLoaded(final List<T> list) {

        final int oldNumItems = mItems.size();
        final Capture<Integer> firstChangedIdx = new Capture<>(Integer.MAX_VALUE);
        final boolean[] changedArray = new boolean[mItems.size()];
        final Capture<Boolean> objectChanged = new Capture<>(false);
        Iterator<T> listIterator = list.iterator();
        int oldIdx = 0;
        T newObj = null;
        T oldObj;

        //Walk through each list side by side until you find an object that is not the same
        // If an object has been updated, note this
        while(listIterator.hasNext() && oldIdx < mItems.size()) {
            newObj = listIterator.next();
            oldObj = mItems.get(oldIdx);
            if(!oldObj.getObjectId().equals(newObj.getObjectId())) {
                firstChangedIdx.set(oldIdx);
                break;
            }
            //hasBeenSeen should always be true
            if(hasBeenSeen(newObj) && newObj.getUpdatedAt().after(getLastSeenUpdatedAt(newObj))) {
                changedArray[oldIdx] = true;
                objectChanged.set(true);
            }
            oldIdx++;
        }

        //Find all of the new objects that were not in the list previously so we can return them
        listIterator = list.iterator();
        List<T> newObjects = new ArrayList<>();
        while (listIterator.hasNext()) {
            newObj = listIterator.next();
            boolean found = false;
//            for (int i = firstChangedIdx.get(); i < mItems.size(); i++) { //To be used when listIterator is not re-initialized
            for (int i = 0; i < mItems.size(); i++) {
                oldObj = mItems.get(i);
                if (newObj.getObjectId().equals(oldObj.getObjectId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newObjects.add(newObj);
            }
        }
        final Capture<Boolean> isChallenge = new Capture<>(false);
        if(getClass().equals(ChallengeQueryAdapter.class)) {
            isChallenge.set(true);
        }

        getActivityForUIThread().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(isChallenge.get()) {
                    int a = 0;
                    a = 5;
                }
                //Update the dataset
                mItems.clear();
                mItems.addAll(list);
                final int newNumItems = mItems.size();

                //Notify the adapter of the dataset changes
                if(firstChangedIdx.get() < Math.min(oldNumItems, newNumItems))
                    notifyItemRangeChanged(firstChangedIdx.get(), Math.min(oldNumItems, newNumItems));
                if(newNumItems > oldNumItems) {
                    notifyItemRangeInserted(oldNumItems, newNumItems - oldNumItems);
                }
                else if(oldNumItems > newNumItems) {
                    notifyItemRangeRemoved(newNumItems, oldNumItems - newNumItems);
                }
                for(int i = 0; i < changedArray.length; i++) {
                    if(changedArray[i])
                        notifyItemChanged(i);
                }
                if(firstChangedIdx.get() != Integer.MAX_VALUE || oldNumItems != newNumItems || objectChanged.get()) {
                    fireOnDataSetChanged();
                }
            }
        });
        return newObjects;
    }

    private ConcurrentLinkedQueue<T> getConcurrentLinkedQueue(List<T> items) {
        ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
        for(T obj : items) {
            queue.add(obj);
        }
        return queue;
    }

    @Override
    public void loadObjects() {
        super.loadObjects();
        for(T obj : mItems) {
            setLastSeenUpdatedAt(obj);
        }
    }
}
