package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public class SubclassUtils {
    public static HashSet<ParseObject> saveSet = new HashSet<>();

    public synchronized static void addToSaveQueue(ParseObject object) {
        saveSet.add(object);
    }

    public synchronized static Task<Boolean> saveAllInBackground() {
        Log.i("saveAllInBackground", "called");
        List<ParseObject> saveList = new ArrayList<>();
        for(ParseObject object : saveSet) {
            saveList.add(object);
        }
        return ParseObject.saveAllInBackground(saveList)
                .continueWith(new Continuation<Void, Boolean>() {
                    @Override
                    public Boolean then(Task<Void> task) throws Exception {
                        boolean ok = true;
                        if (task.isFaulted()) {
                            Exception e = task.getError();
                            e.printStackTrace();
                            Log.e("utils saveAll error", e.getMessage());
                            ok = false;
                        }
                        saveSet.clear();
                        return ok;
                    }
                });
    }
}