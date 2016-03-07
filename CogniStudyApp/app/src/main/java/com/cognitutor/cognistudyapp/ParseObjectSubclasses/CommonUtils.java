package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;

import java.util.Arrays;
import java.util.List;

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

    public static String getSubjectFromCategory(String category) {
        for(String subject : Constants.getAllConstants(Constants.Subject.class)) {
            List<String> categoriesInSubject = Arrays.asList(Constants.SubjectToCategory.get(subject));
            if(categoriesInSubject.contains(category)) {
                return subject;
            }
        }
        return null;
    }
}
