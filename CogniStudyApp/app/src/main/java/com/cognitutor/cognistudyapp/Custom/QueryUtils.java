package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.util.Log;

import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionId;
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

    // <editor-fold desc="findCacheElseNetworkInBackground">
    public static <TClass extends ParseObject> Task<List<TClass>> findPinElseNetworkInBackground(ParseQueryBuilder<TClass> builder,
                                                                                                 String pinName, boolean pinResult) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doFindCacheElseNetworkInBackground(localDataQuery, networkQuery, pinName, new Capture<Boolean>(pinResult));
    }

    public static <TClass extends ParseObject> Task<List<TClass>> findCacheElseNetworkInBackground(ParseQueryBuilder<TClass> builder,
                                                                                                   boolean pinResult) {

        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doFindCacheElseNetworkInBackground(localDataQuery, networkQuery, null, new Capture<Boolean>(pinResult));
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
    public static <TClass extends ParseObject> Task<TClass> getFirstPinElseNetworkInBackground(ParseQueryBuilder<TClass> builder,
                                                                                               String pinName, boolean pinResult) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetworkInBackground(localDataQuery, networkQuery, pinName, new Capture<Boolean>(pinResult));
    }

    public static <TClass extends ParseObject> Task<TClass> getFirstCacheElseNetworkInBackground(ParseQueryBuilder<TClass> builder, boolean pinResult) {

        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetworkInBackground(localDataQuery, networkQuery, null, new Capture<Boolean>(pinResult));
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
    public static <TClass extends ParseObject> TClass getFirstCacheElseNetwork(ParseQueryBuilder<TClass> builder, boolean pinResult) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetwork(localDataQuery, networkQuery, null, pinResult);
    }

    public static <TClass extends ParseObject> TClass getFirstPinElseNetwork(ParseQueryBuilder<TClass> builder,
                                                                             String pinName, boolean pinResult) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetwork(localDataQuery, networkQuery, pinName, pinResult);
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

    //<editor-fold desc="findCacheThenNetworkInBackground">
    public static <T extends ParseObject> Task<List<T>> findCacheThenNetworkInBackground(
            ParseQueryBuilder<T> builder, final OnDataLoadedListener<T> listener,
            final String pinName, final boolean pinResult) {

        final ParseQuery<T> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<T> networkQuery = builder.buildQuery();
        return new QueryUtils().doFindCacheThenNetworkInBackground(localDataQuery, networkQuery, listener, pinName, pinResult, null);
    }

    protected <T extends ParseObject> Task<List<T>> doFindCacheThenNetworkInBackground(
            final ParseQuery<T> localDataQuery, final ParseQuery<T> networkQuery, final OnDataLoadedListener<T> listener,
            final String pinName, final boolean pinResult, final QueryUtilsCacheThenNetworkHelper helper) {

        final long startTime = System.currentTimeMillis();

        return Task.callInBackground(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                final List<T> localResults = doLocalFindQuery(localDataQuery);
                if (isCancelled(helper, startTime))
                    return null;
                runOnDataLoadedOnUIThread(listener, localResults);

                List<T> networkResults = doNetworkFindQuery(networkQuery);
                final List<T> combined = new ArrayList<T>();
                for (T fromLocal : localResults) {
                    addFromLocalOrNetwork(fromLocal, networkResults, combined);
                }
                combined.addAll(networkResults);
                if (pinName != null && pinResult) {
                    ParseObjectUtils.unpinAllInBackground(pinName);
                    ParseObjectUtils.pinAllInBackground(pinName, combined);
                } else if (pinResult) {
                    ParseObjectUtils.pinAllInBackground(combined);
                }
                if (isCancelled(helper, startTime))
                    return null;
                runOnDataLoadedOnUIThread(listener, combined);
                return combined;
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

//            AnsweredQuestionId aid1 = new AnsweredQuestionId(id1);
//            ParseObjectUtils.pin(pinName, aid1);
//
//            AnsweredQuestionId aid2 = new AnsweredQuestionId(id2);
//            ParseObjectUtils.pin(pinName, aid2);
//
//            AnsweredQuestionId aid3 = new AnsweredQuestionId(id3);
//            aid3.save();

            MainFragment.answeredQuestionIdAdapter.clear();

            QueryUtils.findCacheThenNetworkInBackground(new QueryUtils.ParseQueryBuilder<AnsweredQuestionId>() {
                @Override
                public ParseQuery<AnsweredQuestionId> buildQuery() {
                    return ParseQuery.getQuery(AnsweredQuestionId.class);
                }
            }, new QueryUtils.OnDataLoadedListener<AnsweredQuestionId>() {
                @Override
                public Activity getActivityForUIThread() {
                    return (Activity) MainFragment.answeredQuestionIdAdapter.getContext();
                }

                @Override
                public void onDataLoaded(List<AnsweredQuestionId> list) {
                    MainFragment.answeredQuestionIdAdapter.clear();
                    MainFragment.answeredQuestionIdAdapter.addAll(list);
                    MainFragment.answeredQuestionIdAdapter.notifyDataSetChanged();
                }
            }, "AnsweredQuestionId", true);
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

