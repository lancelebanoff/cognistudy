package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;

/**
 * Created by Kevin on 1/7/2016.
 */
public class ErrorHandler {

    public static String determineAction(ParseException e) {
        switch (e.getCode()) {
            case ErrorCode.INVALID_SESSION_TOKEN:
                return ErrorMsg.LOGIN_AGAIN;
            case ErrorCode.CONNECTION_FAILED:
                return ErrorMsg.CONNECTION_ERROR;
            default:
                Log.e("Error Handler", "Unregistered error code: " + e.getCode());
                return ErrorMsg.DEFAULT;
        }
    }

    private static class ErrorCode {
        private static final int INVALID_SESSION_TOKEN = 209;
        private static final int CONNECTION_FAILED = 100;
    }

    public static class ErrorMsg {
        public static final String CONNECTION_ERROR = "Error retrieving data from server. Please check your internet connection and try again.";
        public static final String LOGIN_AGAIN = "An internal error occured. Please login again.";
        public static final String DEFAULT = "An unexpected error occured. Please check your internet connection and try again.";
    }
}
