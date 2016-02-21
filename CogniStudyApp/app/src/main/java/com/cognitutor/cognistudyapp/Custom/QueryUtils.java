package com.cognitutor.cognistudyapp.Custom;

import android.app.DownloadManager;
import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionId;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryBlockStats;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

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

    // <editor-fold desc="findCacheElseNetworkInBackground">
    public static <TClass extends ParseObject> Task<List<TClass>> findPinElseNetworkInBackground(ParseQueryBuilder<TClass> builder,
                                                                                                 String pinName, boolean pinResult) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doFindCacheElseNetworkInBackground(localDataQuery, networkQuery, pinName, pinResult);
    }

    public static <TClass extends ParseObject> Task<List<TClass>> findCacheElseNetworkInBackground(ParseQueryBuilder<TClass> builder,
                                                                                                   boolean pinResult) {

        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doFindCacheElseNetworkInBackground(localDataQuery, networkQuery, null, pinResult);
    }

    private static <TClass extends ParseObject> Task<List<TClass>> doFindCacheElseNetworkInBackground(
            final ParseQuery<TClass> localDataQuery, final ParseQuery<TClass> networkQuery, final String pinName, final boolean pinResult) {

        return localDataQuery
                .findInBackground()
                .continueWithTask(new Continuation<List<TClass>, Task<List<TClass>>>() {
                    @Override
                    public Task<List<TClass>> then(Task<List<TClass>> task) throws Exception {
                        if (task.isFaulted()) {
                            Exception e = task.getError();
                            e.printStackTrace();
                            Log.e("QueryUtil localData err", e.getMessage());
                        }
                        List<TClass> results = task.getResult();
                        TaskCompletionSource<List<TClass>> completionSource = new TaskCompletionSource<List<TClass>>();
                        completionSource.setResult(results);
                        Task<List<TClass>> resultTask = completionSource.getTask();
                        if (results.size() == 0) {
                            Log.e("QueryUtils", "LocalDatastoreQuery returned empty");
                            resultTask = networkQuery.findInBackground();
                        }
                        return resultTask;
                    }
                })
                .continueWith(new Continuation<List<TClass>, List<TClass>>() {
                    @Override
                    public List<TClass> then(Task<List<TClass>> task) throws Exception {
                        if (task.isFaulted()) {
                            Exception e = task.getError();
                            e.printStackTrace();
                            Log.e("QueryUtils network err", e.getMessage());
                        }
                        List<TClass> results = task.getResult();
                        if (pinName != null && pinResult) {
                            ParseObjectUtils.pinAllInBackground(pinName, results);
                        } else if (pinResult) {
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

    private static <T extends ParseObject> Task<T> getCompletionTask(T result) {
        TaskCompletionSource<T> completionSource = new TaskCompletionSource<T>();
        completionSource.setResult(result);
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
}