package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.util.Log;

import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionIds;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by Kevin on 2/13/2016.
 */
public class QueryUtils {

    public interface ParseQueryBuilder<TClass extends ParseObject> {
        ParseQuery<TClass> buildQuery();
    }

    public interface OnDataLoadedListener<T extends ParseObject> {
        Activity getActivityForUIThread();
        void onDataLoaded(List<T> list);
    }

    public interface OnDataLoadedSingleObjectListener<T extends ParseObject> {
        Activity getActivityForUIThread();
        void onDataLoaded(T result);
    }

    // <editor-fold desc="findCacheElseNetworkInBackground">

    /**
     * Does a fromPin(pinName) query on the localDatastore followed by a network query
     * If no cached results are found, the network results will be pinned with the given pinName
     * @param builder The query to be executed, excluding fromPin()
     * @param pinName The pin name to be used
     * @param <TClass> extends ParseObject
     * @return
     */
    public static <TClass extends ParseObject> Task<List<TClass>> findPinElseNetworkInBackground(String pinName, ParseQueryBuilder<TClass> builder) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doFindCacheElseNetworkInBackground(localDataQuery, networkQuery, pinName, new Capture<Boolean>(true));
    }

    /**
     * Does a fromLocalDatastore() query on the localDatastore followed by a network query
     * If no cached results are found, the network results will be pinned with no pin name
     * @param builder The query to be executed, excluding fromPin()
     * @param <TClass> extends ParseObject
     * @return
     */
    public static <TClass extends ParseObject> Task<List<TClass>> findCacheElseNetworkInBackground(ParseQueryBuilder<TClass> builder) {

        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doFindCacheElseNetworkInBackground(localDataQuery, networkQuery, null, new Capture<Boolean>(true));
    }

    private static <TClass extends ParseObject> Task<List<TClass>> doFindCacheElseNetworkInBackground(
            final ParseQuery<TClass> localDataQuery, final ParseQuery<TClass> networkQuery, final String pinName, final Capture<Boolean> pinResult) {

        return localDataQuery
                .findInBackground()
                .continueWithTask(new Continuation<List<TClass>, Task<List<TClass>>>() {
                    @Override
                    public Task<List<TClass>> then(Task<List<TClass>> task) throws Exception {
                        handleFault(task, QueryType.local);
                        List<TClass> results = task.getResult();
                        if (results.size() != 0) {
                            pinResult.set(false);
                            return getCompletionTask(results);
                        }
                        else
                            return networkQuery.findInBackground();
                    }
                })
                .continueWith(new Continuation<List<TClass>, List<TClass>>() {
                    @Override
                    public List<TClass> then(Task<List<TClass>> task) throws Exception {
                        handleFault(task, QueryType.network);
                        List<TClass> results = task.getResult();
                        if (pinName != null && pinResult.get()) {
                            ParseObjectUtils.pinAllInBackground(pinName, results);
                        } else if (pinResult.get()) {
                            ParseObjectUtils.pinAllInBackground(results);
                        }
                        return results;
                    }
                });
    }
    // </editor-fold>

    // <editor-fold desc="getFirstCacheElseNetworkInBackground">
    public static <TClass extends ParseObject> Task<TClass> getFirstPinElseNetworkInBackground(String pinName, ParseQueryBuilder<TClass> builder) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetworkInBackground(localDataQuery, networkQuery, pinName, new Capture<Boolean>(true));
    }

    public static <TClass extends ParseObject> Task<TClass> getFirstCacheElseNetworkInBackground(ParseQueryBuilder<TClass> builder) {

        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetworkInBackground(localDataQuery, networkQuery, null, new Capture<Boolean>(true));
    }

    private static <TClass extends ParseObject> Task<TClass> doGetFirstCacheElseNetworkInBackground(
            final ParseQuery<TClass> localDataQuery, final ParseQuery<TClass> networkQuery, final String pinName, final Capture<Boolean> pinResult) {

        return localDataQuery
                .getFirstInBackground()
                .continueWithTask(new Continuation<TClass, Task<TClass>>() {
                    @Override
                    public Task<TClass> then(Task<TClass> task) throws Exception {
                        handleFault(task, QueryType.local);
                        TClass result = task.getResult();
                        if (result != null) {
                            pinResult.set(false);
                            return getCompletionTask(result);
                        }
                        else
                            return networkQuery.getFirstInBackground();
                    }
                })
                .continueWith(new Continuation<TClass, TClass>() {
                    @Override
                    public TClass then(Task<TClass> task) throws Exception {
                        handleFault(task, QueryType.network);
                        TClass result = task.getResult();
                        if (pinName != null && pinResult.get())
                            ParseObjectUtils.pinInBackground(pinName, result);
                        else if (pinResult.get())
                            ParseObjectUtils.pinInBackground(result);
                        return result;
                    }
                });
    }

//    //This works, but only when less than 5 threads are being run simultaneously that do this
//    private static <TClass extends ParseObject> Task<TClass> doGetFirstCacheElseNetworkInBackground(
//            final ParseQuery<TClass> localDataQuery, final ParseQuery<TClass> networkQuery, final String pinName, final boolean pinResult) {
//
//        return Task.callInBackground(new Callable<TClass>() {
//            @Override
//            public TClass call() throws Exception {
//                TClass result = doLocalGetQuery(localDataQuery);
//                if (result != null)
//                    return result;
//                result = doNetworkGetQuery(networkQuery);
//                if (pinName != null && pinResult) {
//                    ParseObjectUtils.pinInBackground(pinName, result);
//                } else if (pinResult) {
//                    ParseObjectUtils.pinInBackground(result);
//                }
//                return result;
//            }
//        });
//    }
    // </editor-fold>

    // <editor-fold desc="getFirstCacheElseNetwork">
    public static <TClass extends ParseObject> TClass getFirstCacheElseNetwork(ParseQueryBuilder<TClass> builder) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetwork(localDataQuery, networkQuery, null, true);
    }

    public static <TClass extends ParseObject> TClass getFirstPinElseNetwork(String pinName, ParseQueryBuilder<TClass> builder) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetwork(localDataQuery, networkQuery, pinName, true);
    }

    private static <TClass extends ParseObject> TClass doGetFirstCacheElseNetwork(
            final ParseQuery<TClass> localDataQuery, final ParseQuery<TClass> networkQuery, final String pinName, final boolean pinResult) {

        TClass result = doLocalGetQuery(localDataQuery);
        if (result != null)
            return result;
        result = doNetworkGetQuery(networkQuery);
        if (pinName != null && pinResult) {
            ParseObjectUtils.pinInBackground(pinName, result);
        } else if (pinResult) {
            ParseObjectUtils.pinInBackground(result);
        }
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="(find/getFirst)CacheThenNetworkInBackground">

    /**
     * Finds objects with the given query using fromLocalDatastore(), then updates the results with the network results. This DOES NOT
     * use fromPin().
     * <br />
     * The OnDataLoadedListener's onLoaded() method will be called when results are returned from the cache, and again when results
     * are returned from the network.
     * @param pinName The pinName with which to pin the network results.
     * @param deleteOldPinnedResults True if all objects previously pinned with this pinName should be unpinned
     * @param builder
     * @param listener
     * @param <T> extends ParseObject
     * @return
     */
    public static <T extends ParseObject> Task<List<T>> findCacheThenNetworkInBackground(final String pinName, final boolean deleteOldPinnedResults,
                     final OnDataLoadedListener<T> listener, ParseQueryBuilder<T> builder) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<T> networkQuery = builder.buildQuery();
        return new QueryUtils().doFindCacheThenNetworkInBackground(localDataQuery, networkQuery, listener, pinName, deleteOldPinnedResults, null);
    }

    /**
     * Finds objects with the given query using fromPin(pinName), then updates the results with the network results.
     * <br />
     * The OnDataLoadedListener's onLoaded() method will be called when results are returned from the cache, and again when results
     * are returned from the network.
     * @param pinName The pinName with which to execute the cache query and with which to pin the network results.
     * @param deleteOldPinnedResults True if all objects previously pinned with this pinName should be unpinned
     * @param builder
     * @param listener
     * @param <T> extends ParseObject
     * @return
     */
    public static <T extends ParseObject> Task<List<T>> findFromPinThenNetworkInBackground(final String pinName,
                       final boolean deleteOldPinnedResults, final OnDataLoadedListener<T> listener, ParseQueryBuilder<T> builder) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<T> networkQuery = builder.buildQuery();
        return new QueryUtils().doFindCacheThenNetworkInBackground(localDataQuery, networkQuery, listener, pinName, deleteOldPinnedResults, null);
    }

    /**
     * Finds objects with the given query using fromLocalDatastore(), then updates the results with the network results. This does not
     * use fromPin().
     * <br />
     * The network results will be pinned without a pinName.
     * <br />
     * The OnDataLoadedListener's onLoaded() method will be called when results are returned from the cache, and again when results
     * are returned from the network.
     * @param builder
     * @param listener
     * @param <T> extends ParseObject
     * @return
     */
    public static <T extends ParseObject> Task<List<T>> findCacheThenNetworkInBackground(final OnDataLoadedListener<T> listener,
                                                                                         ParseQueryBuilder<T> builder) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<T> networkQuery = builder.buildQuery();
        return new QueryUtils().doFindCacheThenNetworkInBackground(localDataQuery, networkQuery, listener, null, false, null);
    }

    protected <T extends ParseObject> Task<List<T>> doFindCacheThenNetworkInBackground(
            final ParseQuery<T> localDataQuery, final ParseQuery<T> networkQuery, final OnDataLoadedListener<T> listener,
            final String pinName, final boolean deleteOldPinnedResults, final QueryUtilsCacheThenNetworkHelper helper) {

        final long startTime = System.currentTimeMillis();

        return Task.callInBackground(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                List<T> localResults = doLocalFindQuery(localDataQuery);
                if (isCancelled(helper, startTime))
                    return null;
                if (listener != null)
                    runOnDataLoadedOnUIThread(listener, localResults);

                List<T> networkResults = doNetworkFindQuery(networkQuery);
                final List<T> combined = new ArrayList<T>();
                for (T fromLocal : localResults) {
                    addFromLocalOrNetwork(fromLocal, networkResults, combined);
                }
                combined.addAll(networkResults);
                if (pinName != null) {
                    if (deleteOldPinnedResults) {
                        ParseObjectUtils.unpinAllInBackground(pinName).waitForCompletion();
                    }
                    ParseObjectUtils.pinAllInBackground(pinName, combined).waitForCompletion();
                } else { //if (deleteOldPinnedResults) {
                    ParseObjectUtils.pinAllInBackground(combined).waitForCompletion();
                }
                if (isCancelled(helper, startTime))
                    return null;
                if (listener != null)
                    runOnDataLoadedOnUIThread(listener, combined);
                return combined;
            }
        });
    }

    /**
     * Gets the first object that matches the given query using fromLocalDatastore(), then updates the results with the network results. This DOES NOT
     * use fromPin().
     * <br />
     * The OnDataLoadedListener's onLoaded() method will be called when result is returned from the cache, and again when result
     * is returned from the network.
     * @param pinName The pinName with which to pin the network result.
     * @param listener
     * @param builder
     * @param <T> extends ParseObject
     * @return
     */
    public static <T extends ParseObject> Task<T> getFirstCacheThenNetworkInBackground(final String pinName,
                                   final OnDataLoadedSingleObjectListener<T> listener, final ParseQueryBuilder<T> builder) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<T> networkQuery = builder.buildQuery();
        return doGetFirstCacheThenNetworkInBackground(localDataQuery, networkQuery, listener, pinName);
    }

    /**
     * Finds the first object with the given query using fromPin(pinName), then updates the result with the network result.
     * <br />
     * The OnDataLoadedListener's onLoaded() method will be called when result is returned from the cache, and again when result
     * is returned from the network.
     * @param pinName The pinName with which to execute the cache query and with which to pin the network result.
     * @param builder
     * @param listener
     * @param <T> extends ParseObject
     * @return
     */
    public static <T extends ParseObject> Task<T> findFromPinThenNetworkInBackground(final String pinName,
                       ParseQueryBuilder<T> builder, final OnDataLoadedSingleObjectListener<T> listener) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<T> networkQuery = builder.buildQuery();
        return doGetFirstCacheThenNetworkInBackground(localDataQuery, networkQuery, listener, pinName);
    }

    /**
     * Finds the first object with the given query using fromLocalDatastore(), then updates the result with the network result. This does not
     * use fromPin().
     * <br />
     * The network result will be pinned without a pinName.
     * <br />
     * The OnDataLoadedListener's onLoaded() method will be called when the result is returned from the cache, and again when the result
     * is returned from the network.
     * @param builder
     * @param listener
     * @param <T> extends ParseObject
     * @return
     */
    public static <T extends ParseObject> Task<T> getFirstCacheThenNetworkInBackground(ParseQueryBuilder<T> builder,
                                                                         final OnDataLoadedSingleObjectListener<T> listener) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<T> networkQuery = builder.buildQuery();
        return doGetFirstCacheThenNetworkInBackground(localDataQuery, networkQuery, listener, null);
    }

    private static <T extends ParseObject> Task<T> doGetFirstCacheThenNetworkInBackground(
            final ParseQuery<T> localDataQuery, final ParseQuery<T> networkQuery, final OnDataLoadedSingleObjectListener<T> listener,
            final String pinName) {

        return Task.callInBackground(new Callable<T>() {
            @Override
            public T call() throws Exception {
                T localResults = doLocalGetQuery(localDataQuery);
                if (listener != null)
                    runOnDataLoadedOnUIThread(listener, localResults);

                T networkResults = doNetworkGetQuery(networkQuery);
                if (pinName != null) {
                    ParseObjectUtils.pinInBackground(pinName, networkResults).waitForCompletion();
                } else {
                    ParseObjectUtils.pinInBackground(networkResults).waitForCompletion();
                }
                if (listener != null)
                    runOnDataLoadedOnUIThread(listener, networkResults);
                return networkResults;
            }
        });
    }

    private static boolean isCancelled(QueryUtilsCacheThenNetworkHelper helper, long startTime) {
        return helper != null && helper.lastCancelled > startTime;
    }

    private static <T extends ParseObject> void runOnDataLoadedOnUIThread(final OnDataLoadedListener<T> listener, final List<T> list) {
        listener.getActivityForUIThread().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onDataLoaded(list);
            }
        });
    }

    private static <T extends ParseObject> void runOnDataLoadedOnUIThread(final OnDataLoadedSingleObjectListener<T> listener,
                                                                          final T result) {
        listener.getActivityForUIThread().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onDataLoaded(result);
            }
        });
    }

    private static <T extends ParseObject> void addFromLocalOrNetwork(T fromLocal, List<T> networkResults, List<T> combined) {

        String localId = fromLocal.getObjectId();
        Date localUpdatedAt = fromLocal.getUpdatedAt();

        Iterator<T> iterator = networkResults.iterator();
        while(iterator.hasNext()) {
            T fromNetwork = iterator.next();
            if(localId != null && localId.equals(fromNetwork.getObjectId())) {
                if(localUpdatedAt == null || fromNetwork.getUpdatedAt().after(localUpdatedAt))
                    combined.add(fromNetwork);
                else
                    combined.add(fromLocal);
                networkResults.remove(fromNetwork);
                return;
            }
        }
        combined.add(fromLocal);
    }

    public static void testCacheThenNetwork() {

        try {
            String id1 = "id1";
            String id2 = "id2";
            String id3 = "id3";

            String pinName = "AnsweredQuestionId";

            final Capture<AnsweredQuestionIds> aid1 = new Capture<>();
            getFirstCacheThenNetworkInBackground("AnsweredQuestionId",
                    new OnDataLoadedSingleObjectListener<AnsweredQuestionIds>() {
                        @Override
                        public Activity getActivityForUIThread() {
                            return (Activity) MainFragment.answeredQuestionIdAdapter.getContext();
                        }

                        @Override
                        public void onDataLoaded(AnsweredQuestionIds result) {
                            if(result != null)
                                Log.d("testCacheThenNetwork", "aid1 questionIds = " + result.getString("questionIds"));
                            else
                                Log.d("testCacheThenNetwork", "aid1 is null");
                        }
                    }, new ParseQueryBuilder<AnsweredQuestionIds>() {
                        @Override
                        public ParseQuery<AnsweredQuestionIds> buildQuery() {
                            return ParseQuery.getQuery(AnsweredQuestionIds.class)
                                    .whereEqualTo("objectId", "zpWum1VCfT");
                        }
                    }).continueWith(new Continuation<AnsweredQuestionIds, Object>() {
                @Override
                public Object then(Task<AnsweredQuestionIds> task) throws Exception {
                    if (task.getResult() == null)
                        return null;
                    aid1.set(task.getResult());
                    return null;
                }
            }).waitForCompletion();

            if(aid1.get() != null)
                Log.d("testCacheThenNetwork", "aid1 questionIds = " + aid1.get().getString("questionIds"));
            else
                Log.d("testCacheThenNetwork", "aid1 is null");
//            AnsweredQuestionId aid1 = new AnsweredQuestionId(id1);
//            ParseObjectUtils.pin(pinName, aid1);
//
//            AnsweredQuestionId aid2 = new AnsweredQuestionId(id2);
//            ParseObjectUtils.pin(pinName, aid2);
//
//            AnsweredQuestionId aid3 = new AnsweredQuestionId(id3);
//            aid3.save();

//            MainFragment.answeredQuestionIdAdapter.clear();
//
//            QueryUtils.findCacheThenNetworkInBackground("AnsweredQuestionId", false, new QueryUtils.OnDataLoadedListener<AnsweredQuestionId>() {
//                @Override
//                public Activity getActivityForUIThread() {
//                    return (Activity) MainFragment.answeredQuestionIdAdapter.getContext();
//                }
//
//                @Override
//                public void onDataLoaded(List<AnsweredQuestionId> list) {
//                    MainFragment.answeredQuestionIdAdapter.clear();
//                    MainFragment.answeredQuestionIdAdapter.addAll(list);
//                    MainFragment.answeredQuestionIdAdapter.notifyDataSetChanged();
//                }
//            }, new QueryUtils.ParseQueryBuilder<AnsweredQuestionId>() {
//                @Override
//                public ParseQuery<AnsweredQuestionId> buildQuery() {
//                    return ParseQuery.getQuery(AnsweredQuestionId.class);
//                }
//            })
//            .continueWith(new Continuation<List<AnsweredQuestionId>, Object>() {
//                @Override
//                public Object then(Task<List<AnsweredQuestionId>> task) throws Exception {
//                    List<AnsweredQuestionId> listFromLocal = ParseQuery.getQuery(AnsweredQuestionId.class)
//                            .fromLocalDatastore()
//                            .find();
//                    for(AnsweredQuestionId obj : listFromLocal) {
//                        Log.d("testCacheThenNetwork", "objectId: " + obj.getObjectId() + ", questionIds: " + obj.getString("questionIds"));
//                    }
//                    return null;
//                }
//            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold desc="helper functions">
    private static <T extends ParseObject> Task<T> getCompletionTask(T result) {
        TaskCompletionSource<T> completionSource = new TaskCompletionSource<T>();
        completionSource.setResult(result);
        return completionSource.getTask();
    }

    private static <T extends ParseObject> Task<List<T>> getCompletionTask(List<T> results) {
        TaskCompletionSource<List<T>> completionSource = new TaskCompletionSource<List<T>>();
        completionSource.setResult(results);
        return completionSource.getTask();
    }

    class QueryType {
        public static final String local = "local";
        public static final String network = "network";
    }

    private static void handleFault(Task<?> task, String localOrNetwork) {
        if(task.isFaulted()) {
            handleException(task.getError(), localOrNetwork);
        }
    }

    private static void handleException(Exception e, String localOrNetwork) {
        if(e instanceof ParseException) {
            ParseException pe = (ParseException) e;
            if(pe.getCode() != ErrorHandler.ErrorCode.OBJECT_NOT_FOUND) {
                e.printStackTrace();
            }
        }
        Log.e("QueryUtil " + localOrNetwork, e.getMessage());
    }

    private static <T extends ParseObject> T doLocalGetQuery(ParseQuery<T> localDataQuery) {
        return doGetQuery(localDataQuery, QueryType.local);
    }

    private static <T extends ParseObject> T doNetworkGetQuery(ParseQuery<T> networkQuery) {
        return doGetQuery(networkQuery, QueryType.network);
    }

    private static <T extends ParseObject> List<T> doLocalFindQuery(ParseQuery<T> localDataQuery) {
        return doFindQuery(localDataQuery, QueryType.local);
    }

    private static <T extends ParseObject> List<T> doNetworkFindQuery(ParseQuery<T> networkQuery) {
        return doFindQuery(networkQuery, QueryType.network);
    }

    private static <T extends ParseObject> List<T> doFindQuery(ParseQuery<T> query, String localOrNetwork) {
        try {
            return query.find();
        } catch (ParseException e) {
            handleException(e, localOrNetwork);
            return new ArrayList<>();
        }
    }

    private static <T extends ParseObject> T doGetQuery(ParseQuery<T> query, String localOrNetwork) {
        try {
            return query.getFirst();
        } catch (ParseException e) {
            handleException(e, localOrNetwork);
            return null;
        }
    }
    //</editor-fold>
}

