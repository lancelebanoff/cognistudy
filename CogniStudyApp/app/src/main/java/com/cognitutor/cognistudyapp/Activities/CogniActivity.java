package com.cognitutor.cognistudyapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.ErrorHandler;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CogniActivity extends AppCompatActivity {

    private static ConnectivityManager cMgr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cogni);

        Intent callingIntent = getIntent();
        Bundle extrasBundle = callingIntent.getExtras();
        if(extrasBundle != null && extrasBundle.containsKey("toastMessage")) {
            Toast.makeText(getApplicationContext(), extrasBundle.getString("toastMessage"), Toast.LENGTH_LONG).show();
        }

        cMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Makes the ActionBar "up" button mimic the Android "back" button
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }

    // <editor-fold desc="Error handling">

    public void handleParseError(ParseException e) {
        final String TAG = "handleParseError";
        e.printStackTrace();
        Log.e("handleParseError", "Error code: " + e.getCode());
        String errorMsg = null;
        switch (e.getCode()) {
            case ErrorCode.INVALID_SESSION_TOKEN:
                errorMsg = ErrorMsg.LOGIN_AGAIN;
                handleLoginAgain();
                break;
            case ErrorCode.CONNECTION_FAILED:
                errorMsg = ErrorMsg.CONNECTION_ERROR;
                break;
            case ErrorCode.OBJECT_NOT_FOUND:
                errorMsg = ErrorMsg.DEFAULT; //TODO: Say something else?
                break;
            case ErrorCode.CACHE_MISS:
                Log.e(TAG, "Cache miss!");
                break;
            case ErrorCode.OPERATION_FORBIDDEN:
                Log.e(TAG, "Operation forbidden!");
                break;
            default:
                Log.e(TAG, "Unregistered error code: " + e.getCode());
                errorMsg = ErrorMsg.DEFAULT;
        }
        if(errorMsg != null)
            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
    }

    public static class ErrorCode {
        public static final int INVALID_SESSION_TOKEN = 209;
        public static final int CONNECTION_FAILED = 100;
        public static final int INVALID_LOGIN_PARAMS = 101;
        public static final int OBJECT_NOT_FOUND = 101;
        public static final int CACHE_MISS = 120;
        public static final int OPERATION_FORBIDDEN = 119;
    }

    public static class ErrorMsg {
        public static final String CONNECTION_ERROR = "Error retrieving data from server. Please check your internet connection and try again.";
        public static final String LOGIN_AGAIN = "An internal error occured. Please login again.";
        public static final String DEFAULT = "An unexpected error occured. Please check your internet connection and try again.";
    }

    private void handleLoginAgain() {
        try {
            logout();
        } catch (ParseException secondException) { secondException.printStackTrace(); ParseUser.logOut(); }
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("toastMessage", ErrorHandler.ErrorMsg.LOGIN_AGAIN);
        startActivity(intent);
    }

    // </editor-fold>

    public void logout() throws ParseException {
        Log.i("CogniActivity", "Logging out");
        ParseQuery.clearAllCachedResults();
        //ParseObject.unpinAll("CurrentUser");
        ParseUser.logOut();
    }

    // Check for Internet connectivity
    private static boolean isNetworkConnected() {

        //Log.d(TAG, "Checking connectivity");
        if ( cMgr != null){
            NetworkInfo info = cMgr.getActiveNetworkInfo();
            if (info!= null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        //Log.d(TAG, "No internet connection");
        return false;
    }
}
