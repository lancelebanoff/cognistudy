package com.cognitutor.cognistudyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

public class RegistrationActivity extends AuthenticationActivity {

    private Activity activity = this;
    private String username;
    private String displayName;

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
                ParseFacebookUtils.logInWithReadPermissionsInBackground(activity,
                        permissions, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(e != null) {
                                    //TODO: handle error
                                    Log.d("onClick", "error");
                                    e.printStackTrace();
                                }
                                else if(user == null) {
                                    Log.d("Onclick", "User cancelled the Facebook login");
                                }
                                else if(user.isNew()) {
                                    Log.d("Onclick", "New user!");
                                    getUserDetailsFromFB();
                                }
                                else {
                                    navigateToMainActivity();
                                }
                            }
                        });
            }
        });
    }

    private void getUserDetailsFromFB() {

        Bundle params = new Bundle();
        params.putString("fields", "name,picture");

        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            username = response.getJSONObject().getString("id");
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
            //TODO: mProfileImage.setImageBitmap(bitmap)
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
        user.setUsername(username);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();
        String thumbName = user.getUsername().replaceAll("\\s+", "");
        final ParseFile parseFile = new ParseFile(thumbName + "_thumb.png", data);

        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    //TODO: Handle error
                }
                ParseObject publicUserData = new ParseObject("PublicUserData");
                publicUserData.put("profilePic", parseFile);
                publicUserData.put("displayName", displayName);
                setUpStudentObjects(user, publicUserData, true, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        navigateToMainActivity();
                    }
                });
            }
        });
    }
}
