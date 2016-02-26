package com.cognitutor.cognistudyapp.Custom;

import com.parse.ParseACL;
import com.parse.ParseUser;

/**
 * Created by Kevin on 2/25/2016.
 */
public class ACLUtils {

    public static ParseACL getPrivateReadACL() {
        return new ParseACL(ParseUser.getCurrentUser());
    }

    public static ParseACL getPublicReadPrivateWriteACL() {
        ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
        acl.setPublicReadAccess(true);
        return acl;
    }
}
