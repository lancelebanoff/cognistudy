package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentCategoryBlockStats extends StudentBlockStats {

    public static class Columns {
        public static final String category = "category";
    }

    public String getCategory() { return getString(Columns.category); }

    /**
     * Returns ParseQuery.getQuery(the class)
     *                   .whereEqualTo(Columns.category, category);
     * @param category
     * @return
     */
    protected ParseQuery<ParseObject> getClassQuery(String category) {
        return super.getClassQuery().whereEqualTo(Columns.category, category);
    }

    @Override
    public void setSubjectOrCategory(String category) {
        put(Columns.category, category);
    }

    @Override
    public String toString() {
        return super.toString() + " | " + getCategory();
    }
}
