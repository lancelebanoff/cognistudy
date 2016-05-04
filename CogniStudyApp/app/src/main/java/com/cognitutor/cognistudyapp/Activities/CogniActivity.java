package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.ErrorHandler;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseUser;

import bolts.Continuation;
import bolts.Task;

public class CogniActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if(this instanceof ChatActivity) {
            MainActivity.setCameFromChatActivity(true);
        }
        else {
            MainActivity.setCameFromChatActivity(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cogni);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent callingIntent = getIntent();
        Bundle extrasBundle = callingIntent.getExtras();
        if(extrasBundle != null && extrasBundle.containsKey("toastMessage")) {
            Toast.makeText(getApplicationContext(), extrasBundle.getString("toastMessage"), Toast.LENGTH_LONG).show();
        }

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

    // Shows the tutorial dialog only if it hasn't been seen yet.
    // Returns true if the dialog was shown, otherwise returns false
    public boolean showTutorialDialogIfNeeded(String label, Runnable onDismiss) {
        try {
            Student student = UserUtils.getStudent();
            if (!student.tutorialProgressContains(label)) {
                showTutorialDialog(student, label, onDismiss);
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Shows the dialog, and marks that it has been seen
    public void showTutorialDialog(Student student, String label, final Runnable onDismiss) {
        String[] contents = Constants.Tutorial.dialogLabelToContents.get(label);
        new AlertDialog.Builder(this)
                .setTitle(contents[0])
                .setMessage(contents[1])
                .setPositiveButton(R.string.yes_dialog_tutorial, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (onDismiss != null) {
                            onDismiss.run();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (onDismiss != null) {
                            onDismiss.run();
                        }
                    }
                })
                .create().show();

        student.addToTutorialProgress(label);
        student.saveInBackground();
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
        UserUtils.setUserLoggedIn(false);
//        ParseObjectUtils.logPinnedObjects(true);
//        ParseObjectUtils.logPinnedObjects(false);
        ParseObjectUtils.unpinAllInBackground()
            .continueWith(new Continuation<Void, Task<Void>>() {
                @Override
                public Task<Void> then(Task<Void> task) throws Exception {
                    ParseObjectUtils.logPinnedObjects(false);
                    return ParseUser.logOutInBackground();
                }
            }).continueWith(new Continuation<Task<Void>, Void>() {
                @Override
                public Void then(Task<Task<Void>> task) throws Exception {
                    return null;
                }
        });
    }
}
