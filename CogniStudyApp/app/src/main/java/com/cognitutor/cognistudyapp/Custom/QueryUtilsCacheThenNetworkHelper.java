package com.cognitutor.cognistudyapp.Custom;

import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import bolts.Task;

/**
 * Created by Kevin on 2/21/2016.
 */
public class QueryUtilsCacheThenNetworkHelper extends QueryUtils {

    volatile long lastCancelled;

    public <T extends ParseObject> Task<List<T>> findCacheThenNetworkInBackgroundCancelleable(
            QueryUtils.ParseQueryBuilder<T> builder, final QueryUtils.OnDataLoadedListener<T> listener,
            final String pinName, final boolean pinResult) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<T> networkQuery = builder.buildQuery();
        return doFindCacheThenNetworkInBackground(localDataQuery, networkQuery, listener, pinName, pinResult, this);
    }

    public void cancelAllQueries() {
        lastCancelled = System.currentTimeMillis();
    }
}
