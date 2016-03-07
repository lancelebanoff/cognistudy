package com.cognitutor.cognistudyapp.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseUser;

public class VerifyEmailActivity extends AuthenticationActivity {

    ParseUser currentUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        if(currentUser == null) {
            finish();
            Intent intent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        setButtonColors();

        checkEmailVerified(null);
    }

    private void setButtonColors() {
        CogniButton btnContinue = (CogniButton) findViewById(R.id.btnContinue);
        btnContinue.setColor(this, R.color.green);

        CogniButton btnLogout = (CogniButton) findViewById(R.id.btnLogout);
        btnLogout.setColor(this, R.color.red);
    }

    public void checkEmailVerified(View view) {
        showProgress(true);

        try {
            currentUser = ParseUser.getCurrentUser().fetch();
        }
        catch (ParseException e) { handleParseError(e); return; }

        if (view == null) {
            showProgress(false);
        }

        boolean isVerified = currentUser.getBoolean("emailVerified");
        if (!isVerified && view != null) {
            showProgress(false);
            Toast.makeText(getApplicationContext(), "You haven't verified your email yet.", Toast.LENGTH_SHORT).show();
        }

        if(isVerified)
            navigateToNewDestination(); //Should almost always go to ChooseDisplayNameActivity
    }

    public void resendConfirmationEmail(View view) {
        Toast.makeText(getApplicationContext(), "Confirmation email has been resent.", Toast.LENGTH_SHORT).show();
        currentUser.setEmail("");
        currentUser.saveInBackground();
        currentUser.setEmail(currentUser.getString("username"));
        currentUser.saveInBackground();
    }

    public void logout(View view) {
        ParseUser.logOut();
        navigateToRegistrationActivity();
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
}
