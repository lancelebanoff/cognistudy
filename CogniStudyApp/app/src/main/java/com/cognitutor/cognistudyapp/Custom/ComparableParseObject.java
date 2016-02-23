package com.cognitutor.cognistudyapp.Custom;

import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Kevin on 2/20/2016.
 */
public class ComparableParseObject implements Comparable<ComparableParseObject> {

    ParseObject object;

    public ComparableParseObject(ParseObject object) {
        this.object = object;
    }

    public class ObjectColumns {
        public static final String updatedAt = "updatedAt";
    }

    @Override
    public boolean equals(Object other) {
        if(this == other)
            return true;
        if(!(other instanceof ComparableParseObject))
            return false;

        ParseObject thisObject = this.object;
        ParseObject otherObject = ((ComparableParseObject) other).object;
        if(thisObject.getObjectId() != null && otherObject.getObjectId() != null)
            return thisObject.getObjectId().equals(otherObject.getObjectId());
        if(thisObject.getObjectId() == null ^ otherObject.getObjectId() == null)
            return false;
        return thisObject.equals(otherObject);
    }

    @Override
    public int compareTo(ComparableParseObject another) {
        boolean thisUpdatedAtNull = this.getUpdatedAt() == null;
        boolean otherUpdatedAtNull = another.getUpdatedAt() == null;
        if(!thisUpdatedAtNull && !otherUpdatedAtNull) {
            if (this.getUpdatedAt().after(another.getUpdatedAt()))
                return -1;
            else
                return 1;
        }
        if(thisUpdatedAtNull && !otherUpdatedAtNull)
            return -1;
        if(!thisUpdatedAtNull)
            return 1;
        return 0;
    }

    public boolean wasUpdatedAfter(ComparableParseObject another) {
        return compareTo(another) < 0;
    }

    public Date getUpdatedAt() { return object.getDate(ObjectColumns.updatedAt); }
}
