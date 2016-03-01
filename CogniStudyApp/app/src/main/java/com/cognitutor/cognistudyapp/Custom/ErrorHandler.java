package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/7/2016.
 */
public class ErrorHandler {

    public static <T> void executeAndPrintTaskFault(Task<T> task, final String tag) {
        task.continueWith(new Continuation<T, Object>() {
            @Override
            public Object then(Task<T> task) throws Exception {
                printTaskFault(task, tag);
                return null;
            }
        });
    }

    public static void printTaskFault(Task task, String tag) {
        if(task.isFaulted()) {
            Log.e(tag, task.getError().getMessage());
        }
        else {
            Log.d(tag, "All good!");
        }
    }

    public static String determineAction(ParseException e) {
        switch (e.getCode()) {
            case ErrorCode.INVALID_SESSION_TOKEN:
                return ErrorMsg.LOGIN_AGAIN;
            case ErrorCode.CONNECTION_FAILED:
                return ErrorMsg.CONNECTION_ERROR;
            case ErrorCode.OBJECT_NOT_FOUND:
                return ErrorMsg.DEFAULT; //TODO: Say something else?
            default:
                Log.e("Error Handler", "Unregistered error code: " + e.getCode());
                return ErrorMsg.DEFAULT;
        }
    }

    public static class ErrorCode {
        public static final int INVALID_SESSION_TOKEN = 209;
        public static final int CONNECTION_FAILED = 100;
        public static final int INVALID_LOGIN_PARAMS = 101;
        public static final int OBJECT_NOT_FOUND = 101;
    }

    public static class ErrorMsg {
        public static final String CONNECTION_ERROR = "Error retrieving data from server. Please check your internet connection and try again.";
        public static final String LOGIN_AGAIN = "An internal error occured. Please login again.";
        public static final String DEFAULT = "An unexpected error occured. Please check your internet connection and try again.";
    }
}
