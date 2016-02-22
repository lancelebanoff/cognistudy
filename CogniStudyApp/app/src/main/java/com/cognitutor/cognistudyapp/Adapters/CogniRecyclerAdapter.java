package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.cognitutor.cognistudyapp.Custom.ParseRecyclerQueryAdapter;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Kevin on 2/21/2016.
 */
public abstract class CogniRecyclerAdapter<T extends ParseObject, U extends RecyclerView.ViewHolder>
        extends ParseRecyclerQueryAdapter<T, U> implements QueryUtils.OnDataLoadedListener<T> {

    protected Activity mActivity;

    public CogniRecyclerAdapter(Activity activity, final ParseQueryAdapter.QueryFactory<T> factory, final boolean hasStableIds) {
        super(factory, hasStableIds);
        mActivity = activity;
    }

    @Override
    public Activity getActivityForUIThread() {
        return mActivity;
    }

    @Override
    public void onDataLoaded(List<T> list) {

        int oldNumItems = mItems.size();
        ConcurrentLinkedQueue<T> oldObjects = new ConcurrentLinkedQueue<>();
        for(T oldObj : mItems) {
            oldObjects.add(oldObj);
        }
        int firstChangedIdx = Integer.MAX_VALUE;
        int idx = 0;
        Iterator<T> oldIterator = oldObjects.iterator();
        while(oldIterator.hasNext()) {
            T oldObj = oldIterator.next();
            if(oldObj == null)
                break;
            boolean found = false;
            Iterator<T> newIterator = list.iterator();
            while(newIterator.hasNext()) {
                T newObj = newIterator.next();
                if(oldObj.getObjectId().equals(newObj.getObjectId())) {
                    found = true;
                    list.remove(newObj);
                    break;
                }
            }
            if(!found) {
                firstChangedIdx = Math.min(firstChangedIdx, idx);
                mItems.remove(oldObj);
            }
            idx++;
        }
        mItems.addAll(list);
        int newNumItems = mItems.size();
        if(firstChangedIdx < Math.min(oldNumItems, newNumItems))
            notifyItemRangeChanged(firstChangedIdx, Math.min(oldNumItems, newNumItems));
        if(newNumItems > oldNumItems) {
            notifyItemRangeInserted(oldNumItems, newNumItems - oldNumItems);
        }
        else if(oldNumItems > newNumItems) {
            notifyItemRangeRemoved(newNumItems, oldNumItems - newNumItems);
        }
    }
}
