package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Achievement;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionIds;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PinnedObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryDayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryTridayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectDayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectTridayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTotalRollingStats;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by Kevin on 2/13/2016.
 */
public class ParseObjectUtils {

    // <editor-fold desc="Saving">
    public static HashSet<ParseObject> saveSet = new HashSet<>();
    private static HashMap<String, HashSet<ParseObject>> pinMap = new HashMap<>();

    public synchronized static void addToSaveThenPinQueue(String pinName, ParseObject object) {
        saveSet.add(object);
        HashSet<ParseObject> pinSet;
        if(pinMap.containsKey(pinName)) {
            pinSet = pinMap.get(pinName);
        }
        else {
            pinSet = new HashSet<>();
            pinMap.put(pinName, pinSet);
        }
        pinSet.add(object);
    }

    public synchronized static void addToSaveQueue(ParseObject object) {
        saveSet.add(object);
    }

    public synchronized static Task<Boolean> saveAllInBackground() {
        Log.i("saveAllInBackground", "called");
        List<ParseObject> saveList = convertSetToList(saveSet);
        saveSet.clear();
        final Map<String, HashSet<ParseObject>> clonedPinMap = (Map<String, HashSet<ParseObject>>) pinMap.clone();
        pinMap.clear();
        return ParseObject.saveAllInBackground(saveList)
            .continueWithTask(new Continuation<Void, Task<Boolean>>() {
                @Override
                public Task<Boolean> then(Task<Void> task) throws Exception {
                    if (task.isFaulted()) {
                        handleException(task.getError(), "utils saveAll error");
                        return getCompletionTask(false);
                    }
                    if(clonedPinMap.isEmpty())
                        return getCompletionTask(true);
                    return pinFromMap(clonedPinMap);
                }
            });
    }

    private static <T> Task<T> getCompletionTask(T result) {
        TaskCompletionSource<T> completionSource = new TaskCompletionSource<T>();
        completionSource.setResult(result);
        return completionSource.getTask();
    }

    private static Task<Boolean> pinFromMap(Map<String, HashSet<ParseObject>> map) {
        List<Task<Void>> tasks = new ArrayList<>();
        for(String pinName : map.keySet()) {
            List<ParseObject> list = convertSetToList(map.get(pinName));
            try {
                pinAllInBackground(pinName, list).waitForCompletion();
            } catch (Exception e) {
                handleException(e, "utils saveAll pinning error");
                return getCompletionTask(false);
            }
        }
        return getCompletionTask(true);
    }

//    private static Task<Boolean> pinFromMap(Map<String, HashSet<ParseObject>> map) {
//        List<Task<Void>> tasks = new ArrayList<>();
//        for(String pinName : map.keySet()) {
//            List<ParseObject> list = convertSetToList(map.get(pinName));
//            tasks.add(pinAllInBackground(pinName, list));
//        }
//        return Task.whenAll(tasks)
//            .continueWith(new Continuation<Void, Boolean>() {
//                @Override
//                public Boolean then(Task<Void> task) throws Exception {
//                    if (task.isFaulted()) {
//                        handleException(task.getError(), "utils saveAll pinning error");
//                        return false;
//                    }
//                    return true;
//                }
//            });
//    }

    private static void handleException(Exception e, String tag) {
        e.printStackTrace();
        Log.e(tag, e.getMessage());
    }

    //TODO: Move this somewhere else
    public static <T> List<T> convertSetToList(Set<T> set) {
        List<T> list = new ArrayList<>();
        for(T object : set) {
            list.add(object);
        }
        return list;
    }
    // </editor-fold>

    // <editor-fold desc="Save and Pin">
    public static Task<Void> saveThenPinInBackground(final String pinName, final ParseObject object) {
        return object.saveInBackground()
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        return pinInBackground(pinName, object);
                    }
                });
    }

    /**
     *
     * Pins the object in the background with the given pinName and saves it eventually.
     * <br />
     * This DOES NOT create a PinnedObject for the object that is pinned
     *
     * @param pinName The pin name to assign to the object
     * @param object The object to be pinned
     *
     */
    public static void pinThenSaveEventually(String pinName, ParseObject object) {
        object.pinInBackground(pinName);
        object.saveEventually()
            .continueWith(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) throws Exception {
                    if(task.isFaulted()) {
                        Log.e("saveResponse", task.getError().getMessage());
                    }
                    else {
                        Log.d("saveResponse", "Fine!");
                    }
                    return null;
                }
            });
    }

    public static Task<Void> pinThenSaveInBackground(String pinName, ParseObject object) {
        object.pinInBackground(pinName);
        return object.saveInBackground();
    }
    // </editor-fold>

    // <editor-fold desc="Pinning">
    public static final Map<String, Integer> PinNamesToMaxPinned;
    static {
        Map<String, Integer> map = new HashMap<>();

        map.put(Constants.PinNames.PeopleSearch, 20);

        PinNamesToMaxPinned = Collections.unmodifiableMap(map);
    }

    public static void pin(String pinName, ParseObject object) throws ParseException {
        pinAllWithMax(pinName, convertToList(object));
    }

    public static <T extends ParseObject> void pinAll(String pinName, List<T> objects) throws ParseException {
        pinAllWithMax(pinName, objects);
    }

    public static Task<Void> pinInBackground(ParseObject object) {
//        object.pinInBackground();
        return pinAllWithMaxInBackground(null, convertToList(object));
    }

    public static Task<Void> pinInBackground(String pinName, ParseObject object) {
        return pinAllWithMaxInBackground(pinName, convertToList(object));
    }

    public static <T extends ParseObject> Task<Void> pinAllInBackground(List<T> objects) {
//        return ParseObject.pinAllInBackground(objects);
        return pinAllWithMaxInBackground(null, objects);
    }

    public static <T extends ParseObject> Task<Void> pinAllInBackground(String pinName, List<T> objects) {
        return pinAllWithMaxInBackground(pinName, objects);
    }

    private static <T extends ParseObject> void doNewPinAll(final String pinName, final List<T> objects) throws ParseException{
        addAllPinnedObjectsInBackground(pinName == null ? "" : pinName, objects);
        if(pinName == null) {
            ParseObject.pinAll(objects);
            Log.e("doPinAll", "Pin name is null");
        }
        else {
            ParseObject.pinAll(pinName, objects);
        }
    }

    private static List<ParseObject> convertToList(ParseObject object) {
        List<ParseObject> list = new ArrayList<>();
        list.add(object);
        return list;
    }

    private static <T extends ParseObject> void pinAllWithMax(final String pinName, final List<T> objects) {

        if(pinName == null)
            Log.e("pinAllWithMax", "pinName is null");

        if(objects.size() == 0)
            return;

        List<T> newObjectsToPin = new ArrayList<>();
        List<T> objectsAlreadyPinned = new ArrayList<>();
        //TODO: Take this out when testing with PinnedObject is removed
        //This section is probably only necessary for PinnedObject functionality. I believe that
        //ParseObject.pin() or pinAll() will still work if some of the objects are already pinned.
        //Without this section, a new PinnedObject will be created even if the object already exists in the cache.
        for (T obj : objects) {
            try {
                T fromLocal = ParseQuery.getQuery((Class<T>) obj.getClass())
                        .fromLocalDatastore()
                        .get(obj.getObjectId()); //TODO: What happens when obj.getObjectId is null?
                objectsAlreadyPinned.add(fromLocal);
            }
            catch (ParseException e) { //obj did not exist in the local datastore
                if(e.getCode() != ErrorHandler.ErrorCode.OBJECT_NOT_FOUND) {
                    e.printStackTrace();
                    Log.e("pinAllWithMax inital", e.getMessage());
                }
                newObjectsToPin.add(obj);
            }
        }

        try {
//            ParseObject.pinAll(objectsAlreadyPinned); //????????? Idk what I did here. I left out the pin name too.

            final Integer max = (pinName == null) ? null : PinNamesToMaxPinned.get(pinName);
            final int numWaiting = newObjectsToPin.size();
            if (max == null) {
                doNewPinAll(pinName, newObjectsToPin);
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
            doNewPinAll(pinName, newObjectsToPin.subList(0, Math.min(max, newObjectsToPin.size())));
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

    public static Task<Void> unpinAllInBackground(ParseQuery<ParseObject> query) {
        return query
                .fromLocalDatastore()
                .findInBackground()
                .continueWithTask(new Continuation<List<ParseObject>, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<List<ParseObject>> task) throws Exception {
                        if (task.isFaulted()) {
                            handleException(task.getError(), "unpinAll from Query");
                        }
                        List<ParseObject> list = task.getResult();
                        if (list.size() == 0)
                            return getCompletionTask(null);
                        return unpinAllInBackground(list);
                    }
                });
    }

    public static Task<Void> unpinAllInBackground(List<ParseObject> objects) {
        deletePinnedObjectsInBackground(objects);
        return ParseObject.unpinAllInBackground(objects);
    }

    //This will only be used when logging out
    public static Task<Void> unpinAllInBackground() {

//        return PinnedObject.deleteAllPinnedObjectsInBackground().continueWith...
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("baseUserId", ParseUser.getCurrentUser().getObjectId());
        try {
            int numFromLocal = ParseQuery.getQuery(PinnedObject.class)
                    .fromLocalDatastore()
                    .count();
            Log.d("unpinAllinBg", "PinnedObjects before unpinAll = " + numFromLocal);
        } catch (ParseException e) { e.printStackTrace(); }
        return ParseCloud.callFunctionInBackground("deletePinnedObjects", params)
            .continueWith(new Continuation<Object, Void>() {
                @Override
                public Void then(Task<Object> task) throws Exception {
                    if(task.isFaulted()) {
                        Log.e("deletePinnedObjects", task.getError().getMessage());
                    }
                    ParseObject.unpinAllInBackground().waitForCompletion();
                    //Extra layer of assurance that everything will be unpinned that should be
                    List<String> pinNames = Arrays.asList(Constants.getAllConstants(Constants.PinNames.class));
                    for (String pinName : pinNames) {
                        Log.d("pinName: ", pinName);
//                        ParseObject.unpinAllInBackground(pinName);
                        ParseObject.unpinAllInBackground(pinName).waitForCompletion();
                    }
                    try {
                        int numFromLocal = ParseQuery.getQuery(PinnedObject.class)
                                .fromLocalDatastore()
                                .count();
                        Log.d("unpinAllinBg", "PinnedObjects after unpinAll = " + numFromLocal);
                    } catch (ParseException e) { e.printStackTrace(); }
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
//                            }
//                            return null;
//                        }
//                    });
                return null;
                }
            });
    }

    public static void testPins(boolean pin) {

        String id1 = "id1";
        String id2 = "id2";
        String id3 = "id3";

        String pinName = "AnsweredQuestionId";

        try {
            if(pin) {
                AnsweredQuestionIds aid1 = new AnsweredQuestionIds(id1);
                ParseObjectUtils.addToSaveThenPinQueue(pinName, aid1);
//                pin(pinName, aid1);

                AnsweredQuestionIds aid2 = new AnsweredQuestionIds(id2);
                ParseObjectUtils.addToSaveThenPinQueue(pinName, aid2);
//                pin(pinName, aid2);

                ParseObjectUtils.saveAllInBackground();
            }
            else {
                unpinAllInBackground(pinName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("testPins", e.getMessage());
        }
    }

    public static void logPinnedObjects() {

        try {
            List<Class> classes = new ArrayList<Class>();
            classes.add(PublicUserData.class);
            classes.add(Student.class);
            classes.add(PrivateStudentData.class);
            classes.add(StudentCategoryRollingStats.class);
            classes.add(StudentSubjectRollingStats.class);
            classes.add(StudentTotalRollingStats.class);
            classes.add(Response.class);
            classes.add(StudentCategoryDayStats.class);
            classes.add(StudentCategoryTridayStats.class);
            classes.add(StudentCategoryMonthStats.class);
            classes.add(StudentSubjectDayStats.class);
            classes.add(StudentSubjectTridayStats.class);
            classes.add(StudentSubjectMonthStats.class);
            classes.add(AnsweredQuestionIds.class);
            classes.add(Achievement.class);
            classes.add(PinnedObject.class);

            List<PinnedObject> pinnedObjects = ParseQuery.getQuery(PinnedObject.class)
                    .fromLocalDatastore()
                    .find();
            Log.d("Num Pinned", PinnedObject.class.getSimpleName() + " = " + pinnedObjects.size());
            List<String> pinnedObjectIds = new ArrayList<>();
            for (PinnedObject pinnedObject : pinnedObjects) {
                pinnedObjectIds.add(pinnedObject.getPinObjectId());
            }

            int actualNumPinned = 0;
            for (Class clazz : classes) {
                List<ParseObject> objects = ParseQuery.getQuery(clazz.getSimpleName())
                        .fromLocalDatastore()
                        .find();
                int numPinned = objects.size();
                Log.d("Line Break", " ");
                Log.d("Num Pinned", clazz.getSimpleName() + " = " + numPinned);
                if (clazz == PinnedObject.class)
                    continue;
                actualNumPinned += numPinned;
                for (ParseObject obj : objects) {
                    Log.d("Obj data  ", "    " + obj.toString());
//                    if (!pinnedObjectIds.contains(obj.getObjectId())) {
//                        Log.d("Obj data  ", "    ============ " + obj.getObjectId() + " not represented by PinnedObject");
//                    }
                }
            }
            Log.d("Actual Num Pinned", String.valueOf(actualNumPinned));
        } catch (ParseException e) { e.printStackTrace(); }
    }
    // </editor-fold>
}