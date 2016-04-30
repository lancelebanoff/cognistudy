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

    public <T extends ParseObject> Task<List<T>> findCacheThenNetworkInBackgroundCancelleable(final String pinName,
          final boolean deleteOldPinnedResults, final QueryUtils.OnDataLoadedListener<T> listener, QueryUtils.ParseQueryBuilder<T> builder) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromLocalDatastore();
        return doFindCacheThenNetworkInBackground(localDataQuery, builder, listener, PinNameOption.CONSTANT, pinName, deleteOldPinnedResults, this);
    }

    public <T extends ParseObject> Task<List<T>> findCacheThenNetworkInBackgroundCancelleablePinWithObjectId(
            final boolean deleteOldPinnedResults, final QueryUtils.OnDataLoadedListener<T> listener, QueryUtils.ParseQueryBuilder<T> builder) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromLocalDatastore();
        return doFindCacheThenNetworkInBackground(localDataQuery, builder, listener, PinNameOption.OBJECT_ID, null, deleteOldPinnedResults, this);
    }

    public void cancelAllQueries() {
        lastCancelled = System.currentTimeMillis();
    }
}
