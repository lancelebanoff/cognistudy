package com.cognitutor.cognistudyapp.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
                        txtDisplayName.setError("This name is already taken");
                        txtDisplayName.requestFocus();
                    }
                } else {
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
}
