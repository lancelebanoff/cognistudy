package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    public synchronized void notifyObjectIdChanged(String objectId) {
        for(int i=0; i<getItemCount(); i++) {
            if(getItem(i).getObjectId().equals(objectId)) {
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public Activity getActivityForUIThread() {
        return mActivity;
    }

    @Override
    public synchronized void onDataLoaded(List<T> list) {

        int oldNumItems = mItems.size();
        int firstChangedIdx = Integer.MAX_VALUE;
        int idx = 0;

        ConcurrentLinkedQueue<T> oldObjects = getConcurrentLinkedQueue(mItems);
        ConcurrentLinkedQueue<T> newObjects = getConcurrentLinkedQueue(list);
        Iterator<T> newIterator = newObjects.iterator();
        int oldIdx = 0;
        while(newIterator.hasNext() && oldIdx < mItems.size()) {
            T newObj = newIterator.next();
            T oldObj = mItems.get(oldIdx);
            if(!oldObj.getObjectId().equals(newObj.getObjectId())) {
                firstChangedIdx = oldIdx;
                break;
            }
            oldObjects.remove(oldObj);
            list.remove(newObj);
            oldIdx++;
        }
        mItems.removeAll(oldObjects);
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
        if(firstChangedIdx != Integer.MAX_VALUE || oldNumItems != newNumItems) {
            fireOnDataSetChanged();
        }
    }

    private ConcurrentLinkedQueue<T> getConcurrentLinkedQueue(List<T> items) {
        ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
        for(T obj : items) {
            queue.add(obj);
        }
        return queue;
    }
}
