package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionId;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
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

    public static <TClass extends ParseObject> Task<List<TClass>> tryLocalDataFindQuery(final ParseQuery<TClass> query) {

        Log.i("toString()", query.toString());
        return query.fromLocalDatastore()
                .findInBackground()
                .continueWithTask(new Continuation<List<TClass>, Task<List<TClass>>>() {
                    @Override
                    public Task<List<TClass>> then(Task<List<TClass>> task) throws Exception {
                        if (task.isFaulted()) {
                            Exception e = task.getError();
                            e.printStackTrace();
                            Log.e("QueryUtils localData error", e.getMessage());
                        }
                        List<TClass> results = task.getResult();
                        TaskCompletionSource<List<TClass>> completionSource = new TaskCompletionSource<List<TClass>>();
                        completionSource.setResult(results);
                        Task<List<TClass>> resultTask = completionSource.getTask();
                        if (results.size() == 0) {
                            Log.e("QueryUtils", "LocalDatastoreQuery returned empty");
                            query.getClassName();
                            Log.i("toString()", query.toString());
                            resultTask = query.findInBackground();
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
                            Log.e("QueryUtils network backup error", e.getMessage());
                        }
                        return task.getResult();
                    }
                });
    }
}
