package com.cognitutor.cognistudyapp.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.cognitutor.cognistudyapp.Custom.ErrorHandler;
import com.cognitutor.cognistudyapp.Custom.FacebookUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;

import bolts.Continuation;
import bolts.Task;

public class RegistrationActivity extends AuthenticationActivity {

    private Activity activity = this;
    private String facebookId;
    private String displayName;
    private static final String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        /*
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.cognitutor.cognistudyapp", PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign= Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("MY KEY HASH:", sign);
                //  Toast.makeText(getApplicationContext(),sign,     Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        */

        final LoginButton loginButton = (LoginButton) findViewById(R.id.btnFacebookSignIn);
        final Collection<String> permissions = Arrays.asList(new String[]{"public_profile", "user_friends"});
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                ParseFacebookUtils.logInWithReadPermissionsInBackground(activity,
                        permissions, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (e != null) {
                                    //TODO: handle error
                                    Log.d("onClick", "error");
                                    showProgress(false);
                                    e.printStackTrace();
                                } else if (user == null) {
                                    Log.d("Onclick", "User cancelled the Facebook login");
                                    showProgress(false);
                                } else if (user.isNew()) {
                                    Log.d("Onclick", "New user!");
                                    UserUtils.setUserLoggedIn(true);
                                    getUserDetailsFromFB();
                                } else {
                                    UserUtils.setUserLoggedIn(true);
                                    doPinCurrentUser();
                                    FacebookUtils.getFriendsInBackground().continueWith(new Continuation<Void, Void>() {
                                        @Override
                                        public Void then(Task<Void> task) throws Exception {
                                            navigateToMainActivity();
                                            return null;
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    private void getUserDetailsFromFB() {

        Bundle params = new Bundle();
        params.putString("fields", "name,picture.height(200).width(200)");

        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            facebookId = response.getJSONObject().getString("id");
                            displayName = response.getJSONObject().getString("name");
                            JSONObject picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");

                            String pictureUrl = data.getString("url");
                            new ProfilePhotoAsync(pictureUrl).execute();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    class ProfilePhotoAsync extends AsyncTask<String, String, String> {

        public Bitmap bitmap;
        String url;

        public ProfilePhotoAsync(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(String... params) {
            bitmap = DownloadImageBitmap(url);
            return null;
        }

        @Override
        protected  void onPostExecute(String s) {
            super.onPostExecute(s);
            saveNewFBUser(bitmap);
        }
    }

    public static Bitmap DownloadImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("IMAGE", "Error getting bitmap", e);
        }
        return bm;
    }

    private void saveNewFBUser(Bitmap bitmap) {
        final ParseUser user = ParseUser.getCurrentUser();
        user.setUsername(facebookId);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        final byte[] data = stream.toByteArray();
        String thumbName = user.getUsername().replaceAll("\\s+", "");
        final ParseFile profilePic = new ParseFile(thumbName + "_thumb.png", data);

        profilePic.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    //TODO: Handle error
                }
                /*
                getFBFriends("/me/friends", fbFriendsList)
                    .onSuccess(new Continuation<Void, Void>() {
                        @Override
                        public Void then(Task<Void> task) throws Exception {
                            for (String id : fbFriendsList)
                                Log.d(TAG, id);
                            return null;
                        }
                    });
                    */
                setUpStudentObjects(user, facebookId, displayName, profilePic, data, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null) {
                            e.printStackTrace();
                            Log.e("RegistrationActSaveFB", "Error in AuthAct/saveObjects " + e.getMessage());
                        }
                        doPinCurrentUser();
                        navigateToMainActivity();
                    }
                });
            }
        });
    }


    //TODO: Remove all of this later
    public static final String autoGenUsername = "autoCreatedUsername";
    public void autoCreateUser(View view) {
        final ParseUser user = new ParseUser();
        final String email = "KJoslyn29@gmail.com";
        final String password = "password";
        final String username = autoGenUsername;
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("fbLinked", false);
        user.put("displayNameSet", true);
        UserUtils.setUserLoggedIn(true);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                loginUserAuto(username, password);
            }
        });
    }

    private void loginUserAuto(final String email, final String password) {
        ParseUser.logInInBackground(email, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    if (e.getCode() == ErrorHandler.ErrorCode.INVALID_LOGIN_PARAMS) {
                    } else {
                        handleParseError(e);
                        return;
                    }
                } else {
                    final ParseFile profilePic = getDefaultProfilePic();
                    profilePic.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            setUpStudentObjects(ParseUser.getCurrentUser(), null, "AutoGen Person", profilePic, null, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        e.printStackTrace();
                                        Log.e("RegistrationActLogin", "Error in AuthAct/saveObjects " + e.getMessage());
                                    }
                                    doPinCurrentUser();
                                    navigateToMainActivity();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private ParseFile getDefaultProfilePic() {
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
}
