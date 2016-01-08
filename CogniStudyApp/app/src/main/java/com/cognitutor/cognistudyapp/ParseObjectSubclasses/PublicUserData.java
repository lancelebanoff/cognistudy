package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by Kevin on 1/7/2016.
 */
@ParseClassName("PublicUserData")
public class PublicUserData extends ParseObject{

    public PublicUserData() {

    }

    public PublicUserData(ParseUser user, Student student, String displayName, ParseFile profilePic) {
        ParseACL publicDataACL = new ParseACL(user);
        publicDataACL.setPublicReadAccess(true);
        setACL(publicDataACL);
        put("userType", Constants.UserType.STUDENT);
        put("baseUserId", user.getObjectId());
        put("student", student);
        put("displayName", displayName);
        if(profilePic != null)
            put("profilePic", profilePic);
    }

    public Student getStudent() throws ParseException {
        return (Student) getParseObject("student").fetchIfNeeded();
    }
}
