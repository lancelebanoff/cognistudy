package com.cognitutor.cognistudyapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cognitutor.cognistudyapp.Custom.FacebookUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionId;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SubclassUtils;
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
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

        try {
            ParseObject.unpinAll();
        } catch (ParseException e) {}
        final String id1 = "id1";
        final String id2 = "id2";
        final AnsweredQuestionId a = new AnsweredQuestionId(id1, false);
//        SubclassUtils.saveAllInBackground()
//                .continueWith(new Continuation<Boolean, Object>() {
//                    @Override
//                    public Object then(Task<Boolean> task) throws Exception {
//                        try {
//                            a.pin();
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        ParseQuery<AnsweredQuestionId> query1 = ParseQuery.getQuery(AnsweredQuestionId.class)
//                                .whereEqualTo(AnsweredQuestionId.Columns.questionId, id1);
//                        QueryUtils.tryLocalDataFindQuery(query1)
//                                .continueWith(new Continuation<List<AnsweredQuestionId>, Object>() {
//                                    @Override
//                                    public Object then(Task<List<AnsweredQuestionId>> task) throws Exception {
//                                        List<AnsweredQuestionId> results = task.getResult();
//                                        Log.i("TEST", "query 1 results length is " + results.size());
//                                        try {
//                                            a.unpin();
//                                        } catch (ParseException e) {
//                                            e.printStackTrace();
//                                        }
//                                        return null;
//                                    }
//                                });
//
//                        return null;
//                    }
//                });
        AnsweredQuestionId b = new AnsweredQuestionId(id2, false);
        final String objectId = b.getObjectId();
//        SubclassUtils.saveAllInBackground()
        b.saveInBackground()
                .continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        ParseQuery<AnsweredQuestionId> query2 = ParseQuery.getQuery(AnsweredQuestionId.class)
//                                .whereEqualTo(AnsweredQuestionId.Columns.questionId, id2);
                            .whereEqualTo("objectId", objectId);
                        QueryUtils.tryLocalDataFindQuery(query2)
                                .continueWith(new Continuation<List<AnsweredQuestionId>, Object>() {
                                    @Override
                                    public Object then(Task<List<AnsweredQuestionId>> task) throws Exception {
                                        List<AnsweredQuestionId> results = task.getResult();
                                        Log.i("TEST", "query 2 results length is " + results.size());
                                        return null;
                                    }
                                });
                        return null;
                    }
                });
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
                ParseFacebookUtils.logInWithReadPermissionsInBackground(activity,
                        permissions, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (e != null) {
                                    //TODO: handle error
                                    Log.d("onClick", "error");
                                    e.printStackTrace();
                                } else if (user == null) {
                                    Log.d("Onclick", "User cancelled the Facebook login");
                                } else if (user.isNew()) {
                                    Log.d("Onclick", "New user!");
                                    getUserDetailsFromFB();
                                } else {
                                    setUpLocalDataStore();
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
                            Log.e("setUpStudentObjects err", e.getMessage());
                        }
                        setUpLocalDataStore();
                        navigateToMainActivity();
                    }
                });
            }
        });
    }
}
