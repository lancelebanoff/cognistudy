package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

/**
 * Created by Kevin on 1/14/2016.
 */
public class PeopleQueryAdapter extends ParseQueryAdapter<ParseObject> {

    /*
    public PeopleQueryAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = PublicUserData.getQuery()
                        .fromLocalDatastore()
                        .whereEqualTo(PublicUserData.Columns.fbLinked, true)
                        .whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
                return query;
            }
        });
    }
    */
    public PeopleQueryAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = PublicUserData.getQuery()
                        .fromLocalDatastore()
                        .whereContainedIn(PublicUserData.Columns.objectId, PrivateStudentData.getFriendPublicUserIds())
                        .whereEqualTo(PublicUserData.Columns.fbLinked, true);
                        //.whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
                return query;
            }
        });
    }
}
