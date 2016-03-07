package com.cognitutor.cognistudyapp.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.cognitutor.cognistudyapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ChooseDisplayNameActivity extends AuthenticationActivity {

    EditText txtDisplayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_display_name);

        txtDisplayName = (EditText) findViewById(R.id.txtDisplayName);
    }

    public void checkDisplayName(View view) {
        hideKeyboard();
        showProgress(true);
        final String displayName = txtDisplayName.getText().toString();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PublicUserData");
        query.whereEqualTo("displayName", displayName);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() == 0) {
                        ParseFile profilePic = getDefaultProfilePic();
                        setUpStudentObjects(ParseUser.getCurrentUser(), null, displayName, profilePic, null, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e != null) {
                                    e.printStackTrace();
                                    Log.e("ChooseDisplayName", "Error in AuthAct/saveObjects " + e.getMessage());
                                }
                                doPinCurrentUser();
                                navigateToMainActivity();
                            }
                        });
                    } else {
                        showProgress(false);
                        txtDisplayName.setError("This name is already taken");
                        txtDisplayName.requestFocus();
                    }
                } else {
                    showProgress(false);
                    handleParseError(e);
                }
            }
        });
    }

    public ParseFile getDefaultProfilePic() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.default_profile_pic);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] image = stream.toByteArray();
        return new ParseFile("default_profile_pic.png", image);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        final RelativeLayout contentLayout = (RelativeLayout) findViewById(R.id.contentLayout);
        final ProgressBar progressView = (ProgressBar) findViewById(R.id.login_progress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            contentLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
