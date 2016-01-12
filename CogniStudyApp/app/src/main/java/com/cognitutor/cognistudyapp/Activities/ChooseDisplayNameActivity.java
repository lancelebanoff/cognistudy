package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.cognitutor.cognistudyapp.Custom.ErrorHandler;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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

        final String displayName = txtDisplayName.getText().toString();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PublicUserData");
        query.whereEqualTo("displayName", displayName);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() == 0) {
                        setUpStudentObjects(ParseUser.getCurrentUser(), false, displayName, null, null, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                navigateToMainActivity();
                            }
                        });
                    } else {
                        txtDisplayName.setError("This name is already taken");
                        txtDisplayName.requestFocus();
                    }
                } else {
                    handleParseError(e);
                }
            }
        });
    }
}
