package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PinnedObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryDayStats;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public class ParseObjectUtils {

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

    public static void pin(String pinName, ParseObject object) throws ParseException {
        pinAllWithMax(pinName, convertToList(object));
    }

    public static <T extends ParseObject> void pinAll(String pinName, List<T> objects) throws ParseException {
        pinAllWithMax(pinName, objects);
    }

    public static void pinInBackground(ParseObject object) {
        pinAllWithMax(null, convertToList(object));
    }

    public static Task<Void> pinInBackground(String pinName, ParseObject object) {
        return pinAllWithMaxInBackground(pinName, convertToList(object));
    }

    public static <T extends ParseObject> Task<Void> pinAllInBackground(List<T> objects) {
        return pinAllWithMaxInBackground(null, objects);
    }

    public static <T extends ParseObject> Task<Void> pinAllInBackground(String pinName, List<T> objects) {
        return pinAllWithMaxInBackground(pinName, objects);
    }

    private static <T extends ParseObject> void doPinAll(final String pinName, final List<T> objects) throws ParseException{
        addAllPinnedObjectsInBackground(pinName == null ? "" : pinName, objects);
        ParseObject.pinAll(objects);
    }

    private static List<ParseObject> convertToList(ParseObject object) {
        List<ParseObject> list = new ArrayList<>();
        list.add(object);
        return list;
    }

    private static <T extends ParseObject> void pinAllWithMax(final String pinName, final List<T> objects) {

        //TODO: Take this out when testing with PinnedObject is removed
        //This section is probably only necessary for PinnedObject functionality. I believe that
        //ParseObject.pin() or pinAll() will still work if some of the objects are already pinned.
        //Without this section, a new PinnedObject will be created even if the object already exists in the cache.
        List<T> objectsToPin = new ArrayList<>();
        for (T obj : objects) {
            try {
                T fromLocal = ParseQuery.getQuery((Class<T>) obj.getClass())
                        .fromLocalDatastore()
                        .get(obj.getObjectId());
            }
            catch (ParseException e) { //obj did not exist in the local datastore
                if(e.getCode() != ErrorHandler.ErrorCode.OBJECT_NOT_FOUND) {
                    e.printStackTrace();
                    Log.e("pinAllWithMax inital", e.getMessage());
                }
                objectsToPin.add(obj);
            }
        }
        if(objectsToPin.size() == 0)
            return;

        try {
            final int numWaiting = objectsToPin.size();
            final Integer max = (pinName == null) ? null : PinNamesToMaxPinned.get(pinName);
            if (max == null) {
                doPinAll(pinName, objectsToPin);
                return;
            }

            Log.d("pinning with max", "pinName: " + pinName);

            int numPinned = PinnedObject.getQueryForUserOldestFirst(pinName).count();

            int numToUnpin = numWaiting + numPinned - max;
            if (numToUnpin > 0) {
                List<PinnedObject> listToUnpin = PinnedObject.getQueryForUserOldestFirst(pinName)
                        .setLimit(numToUnpin).find();

                PinnedObject.unpinAllObjectsAndDelete(listToUnpin);
            }
            doPinAll(pinName, objectsToPin.subList(0, Math.min(max, objectsToPin.size())));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("pinAllWithMax", e.getMessage());
        }
    }

    private static <T extends ParseObject> Task<Void> pinAllWithMaxInBackground(final String pinName, final List<T> objects) {

        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                pinAllWithMax(pinName, objects);
                return null;
            }
        });
    }

    //Don't bother removing the pin name because it might be in the process of being added back by another thread
    public static void unpinAll(String pinName) throws ParseException {
        deletePinnedObjectsInBackground(pinName);
        ParseObject.unpinAll(pinName);
    }

    //Don't bother removing the pin name because it might be in the process of being added back by another thread
    public static Task<Void> unpinAllInBackground(final String pinName) {
        return deletePinnedObjectsInBackground(pinName)
            .continueWithTask(new Continuation<Void, Task<Void>>() {
                @Override
                public Task<Void> then(Task<Void> task) throws Exception {
                    return ParseObject.unpinAllInBackground(pinName);
                }
            });
    }

    public static void unpinAllInBackground(List<ParseObject> objects) {
        deletePinnedObjectsInBackground(objects);
        ParseObject.unpinAllInBackground(objects);
    }

    //This will only be used when logging out
    public static Task<Void> unpinAllInBackground() {

//        return PinnedObject.deleteAllPinnedObjectsInBackground().continueWith...
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("baseUserId", ParseUser.getCurrentUser().getObjectId());
        return ParseCloud.callFunctionInBackground("deletePinnedObjects", params)
            .continueWith(new Continuation<Object, Void>() {
                @Override
                public Void then(Task<Object> task) throws Exception {
                    if(task.isFaulted()) {
                        Log.e("deletePinnedObjects", task.getError().getMessage());
                    }
                    ParseObject.unpinAllInBackground();
                    //Extra layer of assurance that everything will be unpinned that should be
                    List<String> pinNames = Arrays.asList(Constants.getAllConstants(PinNames.class));
                    for (String pinName : pinNames) {
                        Log.d("pinName: ", pinName);
                        ParseObject.unpinAllInBackground(pinName);
                    }
                    return null;
                }
            });
    }

    private static <T extends ParseObject> void addAllPinnedObjectsInBackground(String pinName, List<T> objects) {
        for(ParseObject obj : objects) {
            PinnedObject pinnedObject = new PinnedObject(pinName, obj);
        }
    }

    private static Task<Void> deletePinnedObjectsInBackground(String pinName) {

        ParseQuery<PinnedObject> query = PinnedObject.getQueryForUserOldestFirst(pinName);
        return deleteObjectsInBackground(query);
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
                List<T> objects = task.getResult();
                for(T obj : objects) {
                    obj.deleteInBackground()
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
                }
//                ParseObject.deleteAllInBackground(task.getResult())
//                    .continueWith(new Continuation<Void, Object>() {
//                        @Override
//                        public Object then(Task<Void> task) throws Exception {
//                            if (task.isFaulted()) {
//                                Log.e("DeleteObjectsInBackg", task.getError().getMessage());
//                                //TODO: I once got an invalid session token error here. Create Cloud Code function for deleting PinnedObjects?
//                            }
//                            return null;
//                        }
//                    });
                return null;
                }
            });
    }
    // </editor-fold>
}