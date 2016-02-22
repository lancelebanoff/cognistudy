package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PinnedObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryDayStats;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public class ParseObjectUtils {

    // <editor-fold desc="Pinning">
    public static class PinNames {

        public static final String PinData = "PinData";

        public static final String PeopleSearch = "PeopleSearch";
        public static final String PublicUserData = "PublicUserData";
        public static final String Challenge = "Challenge";
        public static final String CurrentUser = "CurrentUser";
        //TODO: Possibly remove these later
        public static final String StudentCategoryDayStats = Constants.ClassName.StudentCategoryDayStats;
        public static final String StudentCategoryTridayStats = Constants.ClassName.StudentCategoryTridayStats;
        public static final String StudentCategoryMonthStats = Constants.ClassName.StudentCategoryMonthStats;
        public static final String StudentSubjectDayStats = Constants.ClassName.StudentSubjectDayStats;
        public static final String StudentSubjectTridayStats = Constants.ClassName.StudentSubjectTridayStats;
        public static final String StudentSubjectMonthStats = Constants.ClassName.StudentSubjectMonthStats;
    }

    public static final Map<String, Integer> PinNamesToMaxPinned;
    static {
        Map<String, Integer> map = new HashMap<>();

        map.put(PinNames.PeopleSearch, 20);

        PinNamesToMaxPinned = Collections.unmodifiableMap(map);
    }

    private static ConcurrentSkipListSet<String> pinNames = new ConcurrentSkipListSet<>();

    public static void pin(String pinName, ParseObject object) throws ParseException {
        addPinName(pinName);
        addPinnedObjectInBackground(pinName, object);
        object.pin();
    }

    public static <T extends ParseObject> void pinAll(String pinName, List<T> objects) throws ParseException {
        addPinName(pinName);
        addAllPinnedObjectsInBackground(pinName, objects);
        ParseObject.pinAll(objects);
    }

    public static void pinInBackground(ParseObject object) {
        addPinnedObjectInBackground("", object);
        object.pinInBackground();
    }

    public static void pinInBackground(String pinName, ParseObject object) {
        addPinName(pinName);
        addPinnedObjectInBackground(pinName, object);
        object.pinInBackground(pinName);
    }

    public static void pinInBackground(String pinName, ParseObject object, SaveCallback callback) {
        addPinName(pinName);
        addPinnedObjectInBackground(pinName, object);
        object.pinInBackground(pinName, callback);
    }

    public static <T extends ParseObject> void pinAllWithMax(final String pinName, final List<T> objects) {

        try {
            final int numWaiting = objects.size();
            final Integer max = PinNamesToMaxPinned.get(pinName);
            if (max == null) {
                pinAll(pinName, objects);
                return;
            }

            Log.d("pinning with max", "pinName: " + pinName);

            int numPinned = PinnedObject.getQueryForUserOldestFirst(pinName).count();

            int numToUnpin = numWaiting + numPinned - max;
            if (numToUnpin > 0) {
                List<PinnedObject> listToUnpin = PinnedObject.getQueryForUserOldestFirst(pinName)
                        .setLimit(numPinned).find();

                PinnedObject.unpinAllObjectsAndDelete(listToUnpin);
            }
            pinAll(pinName, objects.subList(0, Math.min(max, objects.size())));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("pinAllWithMax", e.getMessage());
        }
        return;
    }

    public static <T extends ParseObject> Task<Void> pinAllWithMaxInBackground(final String pinName, final List<T> objects) {

        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                pinAllWithMax(pinName, objects);
                return null;
            }
        });
    }

    public static <T extends ParseObject> void pinAllInBackground(List<T> objects) {
        addAllPinnedObjectsInBackground("", objects);
        ParseObject.pinAllInBackground(objects);
    }

    public static <T extends ParseObject> Task<Void> pinAllInBackground(String pinName, List<T> objects) {
        addPinName(pinName);
        addAllPinnedObjectsInBackground(pinName, objects);
        return ParseObject.pinAllInBackground(objects);
    }

    private static void addPinName(String pinName) {
        pinNames.add(pinName);
    }

    //Don't bother removing the pin name because it might be in the process of being added back by another thread
    public static void unpinAll(String pinName) throws ParseException {
        deletePinnedObjectsInBackground(pinName);
        ParseObject.unpinAll(pinName);
    }

    //Don't bother removing the pin name because it might be in the process of being added back by another thread
    public static void unpinAllInBackground(String pinName) {
        deletePinnedObjectsInBackground(pinName);
        ParseObject.unpinAllInBackground(pinName);
    }

    public static void unpinAllInBackground(List<ParseObject> objects) {
        deletePinnedObjectsInBackground(objects);
        ParseObject.unpinAllInBackground(objects);
    }

    //This will only be used when logging out
    public static Task<Void> unpinAllInBackground() {
        return PinnedObject.deleteAllPinnedObjectsInBackground()
            .continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    ParseObject.unpinAllInBackground();
                    //Extra layer of assurance that everything will be unpinned that should be
                    pinNames.addAll(Arrays.asList(Constants.getAllConstants(PinNames.class)));
                    for (String pinName : pinNames) {
                        Log.d("pinName: ", pinName);
                        ParseObject.unpinAllInBackground(pinName);
                    }
                    pinNames.clear();
                    return null;
                }
            });
    }

    private static void addPinnedObjectInBackground(String pinName, ParseObject object) {
        PinnedObject pinnedObject = new PinnedObject(pinName, object);
    }

    private static <T extends ParseObject> void addAllPinnedObjectsInBackground(String pinName, List<T> objects) {
        for(ParseObject obj : objects) {
            PinnedObject pinnedObject = new PinnedObject(pinName, obj);
        }
    }

    private static void deletePinnedObjectsInBackground(String pinName) {

        ParseQuery<PinnedObject> query = PinnedObject.getQueryForUserOldestFirst(pinName);
        deleteObjectsInBackground(query);
    }

    private static void deletePinnedObjectsInBackground(List<ParseObject> objects) {

        ParseQuery<PinnedObject> query = PinnedObject.getQueryForListOfObjects(objects);
        deleteObjectsInBackground(query);
    }

    public static <T extends ParseObject> Task<Void> deleteObjectsInBackground(ParseQuery<T> query) {
        return query.findInBackground()
                .continueWithTask(new Continuation<List<T>, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<List<T>> task) throws Exception {
                        ParseObject.deleteAllInBackground(task.getResult())
                            .continueWith(new Continuation<Void, Object>() {
                                @Override
                                public Object then(Task<Void> task) throws Exception {
                                    if (task.isFaulted()) {
                                        Log.e("DeleteObjectsInBackg", task.getError().getMessage());
                                        //TODO: I once got an invalid session token error here. Create Cloud Code function for deleting PinnedObjects?
                                    }
                                    return null;
                                }
                            });
                        return null;
                    }
                });
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