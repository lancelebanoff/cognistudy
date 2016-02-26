package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryTridayStats")
public class StudentCategoryTridayStats extends StudentCategoryBlockStats{

    public static StudentBlockStatsSubclassInterface getInterface() {
        return new StudentBlockStatsSubclassInterface() {

            @Override
            public ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(Student student, String category) {
                return getCurrentTridayStats(getAllCurrentUserStats(student, category));
            }

            @Override
            public ParseQuery<StudentBlockStats> getAllCurrentUserStats(Student student, String category) {
                return getCurrentUserSuperQuery(getRelation(student), category);
            }
        };
    }

    private static ParseRelation getRelation(Student student) {
        return student.getStudentBlockStatsRelation(StudentCategoryTridayStats.class);
    }

    @Override
    public void setBlockNum() {
        setTridayBlockNum();
    }
}