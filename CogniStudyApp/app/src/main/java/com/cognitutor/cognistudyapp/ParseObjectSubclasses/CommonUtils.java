package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by Kevin on 2/29/2016.
 */
public class CommonUtils {
    public static <T> Task<T> getCompletionTask(T result) {
        TaskCompletionSource<T> completionSource = new TaskCompletionSource<T>();
        completionSource.setResult(result);
        return completionSource.getTask();
    }
}
