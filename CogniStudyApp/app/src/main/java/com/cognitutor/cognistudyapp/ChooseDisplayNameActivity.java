package com.cognitutor.cognistudyapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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

        final String name = txtDisplayName.getText().toString();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PublicUserData");
        query.whereEqualTo("displayName", name);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    if(objects.size() == 0) {
                        ParseObject publicUserData = UserUtils.getPublicUserData();
                        publicUserData.put("displayName", name);
                        publicUserData.saveInBackground();
                        navigateToMainActivity();
                    }
                    else {
                        txtDisplayName.setError("This name is already taken");
                        txtDisplayName.requestFocus();
                    }
                }
                else {
                    handleError(e, "findInBackground");
                }
            }
        });
    }

    private void handleError(Exception e, String tag) {

        if(tag.equals("getPublicUserData")) {
            Log.d(tag, "Error fetching publicUserData");
            Log.d(tag, e.getMessage());
        }
        else if(tag.equals("findInBackground")) {
            Log.d(tag, "Error finding displayName");
            Log.d(tag, e.getMessage());
        }
        e.printStackTrace();
    }
}
