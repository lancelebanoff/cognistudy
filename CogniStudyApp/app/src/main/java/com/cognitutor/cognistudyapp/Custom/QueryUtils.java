package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionId;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryBlockStats;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

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

    public static <TClass extends ParseObject> Task<TClass> getFirstPinElseNetworkInBackground(ParseQueryBuilder<TClass> builder,
                                                                                               String pinName, boolean pinResult) {
        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromPin(pinName);
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetworkInBackground(localDataQuery, networkQuery, pinName, pinResult);
    }

    public static <TClass extends ParseObject> Task<TClass> getFirstCacheElseNetworkInBackground(ParseQueryBuilder<TClass> builder, boolean pinResult) {

        final ParseQuery<TClass> localDataQuery = builder.buildQuery().fromLocalDatastore();
        final ParseQuery<TClass> networkQuery = builder.buildQuery();
        return doGetFirstCacheElseNetworkInBackground(localDataQuery, networkQuery, null, pinResult);
    }

    class QueryType {
        public static final String local = "local";
        public static final String network = "network";
    }

    private static void handleFault(Task<?> task, String localOrNetwork) {
        if(task.isFaulted()) {
            Exception e = task.getError();
            if(e instanceof ParseException) {
                ParseException pe = (ParseException) e;
                if(pe.getCode() != ErrorHandler.ErrorCode.OBJECT_NOT_FOUND) {
                    e.printStackTrace();
                }
            }
            Log.e("QueryUtil " + localOrNetwork, e.getMessage());
        }
    }

    private static <TClass extends ParseObject> Task<TClass> doGetFirstCacheElseNetworkInBackground(
            final ParseQuery<TClass> localDataQuery, final ParseQuery<TClass> networkQuery, final String pinName, final boolean pinResult) {

        return localDataQuery
                .getFirstInBackground()
                .continueWithTask(new Continuation<TClass, Task<TClass>>() {
                    @Override
                    public Task<TClass> then(Task<TClass> task) throws Exception {
                        handleFault(task, QueryType.local);
                        TClass result = task.getResult();
                        TaskCompletionSource<TClass> completionSource = new TaskCompletionSource<TClass>();
                        completionSource.setResult(result);
                        Task<TClass> resultTask = completionSource.getTask();
                        if (result == null) {
                            resultTask = networkQuery.getFirstInBackground();
                        }
                        return resultTask;
                    }
                })
                .continueWith(new Continuation<TClass, TClass>() {
                    @Override
                    public TClass then(Task<TClass> task) throws Exception {
                        handleFault(task, QueryType.network);
                        TClass result = task.getResult();
                        if (pinName != null && pinResult) {
                            ParseObjectUtils.pinInBackground(pinName, result);
                        } else if(pinResult) {
                            ParseObjectUtils.pinInBackground(result);
                        }
                        return result;
                    }
                });

    }

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

        TClass result = null;
        try {
            result = localDataQuery.getFirst();
        } catch (ParseException e) {
            try {
                if(e.getCode() != ErrorHandler.ErrorCode.OBJECT_NOT_FOUND) {
                    e.printStackTrace();
                    Log.e("doGetFirstCache", e.getMessage());
                    return null;
                }
                result = networkQuery.getFirst();
                result.fetchIfNeeded(); //TODO: Add this to all methods?
                if(pinName != null && pinResult) {
                    ParseObjectUtils.pinInBackground(pinName, result);
                }
                else if(pinResult) {
                    ParseObjectUtils.pinInBackground(result);
                }
            } catch (ParseException e2) { e2.printStackTrace(); Log.e("doGetFirstNetwork", e2.getMessage()); }
        }
        return result;
    }
}