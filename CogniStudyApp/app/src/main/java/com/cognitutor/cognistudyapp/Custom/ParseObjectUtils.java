package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public class ParseObjectUtils {

    // <editor-fold desc="Pinning">
    private static ConcurrentSkipListSet<String> pinNames = new ConcurrentSkipListSet<>();

    public static void pin(String pinName, ParseObject object) throws ParseException {
        addPinName(pinName);
        object.pin();
    }

    public static <T extends ParseObject> void pinAll(String pinName, List<T> objects) throws ParseException {
        addPinName(pinName);
        ParseObject.pinAll(objects);
    }

    public static void pinInBackground(ParseObject object) {
        object.pinInBackground();
    }

    public static void pinInBackground(String pinName, ParseObject object) {
        addPinName(pinName);
        object.pinInBackground(pinName);
    }

    public static void pinInBackground(String pinName, ParseObject object, SaveCallback callback) {
        addPinName(pinName);
        object.pinInBackground(pinName, callback);
    }

    public static <T extends ParseObject> void pinAllInBackground(List<T> objects) {
        ParseObject.pinAllInBackground(objects);
    }

    public static <T extends ParseObject> void pinAllInBackground(String pinName, List<T> objects) {
        addPinName(pinName);
        ParseObject.pinAllInBackground(objects);
    }

    private static void addPinName(String pinName) {
        pinNames.add(pinName);
    }

    //Don't bother removing the pin name because it might be in the process of being added back by another thread
    public static void unpinAll(String pinName) throws ParseException {
        ParseObject.unpinAll(pinName);
    }

    //Don't bother removing the pin name because it might be in the process of being added back by another thread
    public static void unpinAllInBackground(String pinName) {
        ParseObject.unpinAllInBackground(pinName);
    }

    //This will only be used when logging out
    public static void unpinAllInBackground() {
        ParseObject.unpinAllInBackground();
        //Extra layer of assurance that everything will be unpinned that should be
        pinNames.addAll(Arrays.asList(Constants.getAllConstants(Constants.PinNames.class)));
        for(String pinName : pinNames) {
            Log.d("pinName: ", pinName);
            ParseObject.unpinAllInBackground(pinName);
        }
        pinNames.clear();
    }
    // </editor-fold>

    // <editor-fold desc="Saving">
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
        saveSet.clear();
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
                        return ok;
                    }
                });
    }
    // </editor-fold>
}