package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public class SubclassUtils {

    private AtomicInteger adds;

    public SubclassUtils() {
        skipListSet = new ConcurrentSkipListSet<>();
        adds = new AtomicInteger(0);
    }

    private ConcurrentSkipListSet<SaveItem> skipListSet;

    public void addToSaveQueue(ParseObject object) {
        adds.getAndIncrement();
        Log.d("addToSaveQueue", "after increment, adds = " + adds.get());
        try {
            SaveItem saveItem = new SaveItem(object);
            if(skipListSet.contains(saveItem)) {
                Log.e("skipListSet add", "element already existed");
                skipListSet.remove(saveItem);
            }
            skipListSet.add(saveItem);
        } catch (Exception e) {e.printStackTrace(); Log.e("skipListSet add error", e.getMessage());
        }
        adds.getAndDecrement();
        Log.d("addToSaveQueue", "after decrement, adds = " + adds.get());
    }

    public synchronized Task<Boolean> saveAllInBackground() {
        while(adds.get() > 0) {
            ;
        } //Wait until all threads have finished adding the elements to be saved
        Log.i("saveAllInBackground", "executing");
        SaveItem[] saveItemArray = new SaveItem[skipListSet.size()];
        skipListSet.toArray(saveItemArray);
        List<ParseObject> saveObjects = new ArrayList<>();
        for(SaveItem saveItem : saveItemArray) {
            skipListSet.remove(saveItem);
            saveObjects.add(saveItem.object);
        }
        return ParseObject.saveAllInBackground(saveObjects)
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

    public class SaveItem implements Comparable<SaveItem> {
        ParseObject object;
        public SaveItem(ParseObject object) {
            this.object = object;
        }

        @Override
        public boolean equals(Object other) {
            if(this == other)
                return true;
            if(!(other instanceof SaveItem))
                return false;

            ParseObject thisObject = this.object;
            ParseObject otherObject = ((SaveItem) other).object;
            if(thisObject.getObjectId() != null && otherObject.getObjectId() != null)
                return thisObject.getObjectId().equals(otherObject.getObjectId());
            if(thisObject.getObjectId() == null ^ otherObject.getObjectId() == null)
                return false;
            return thisObject.equals(otherObject);
        }

        @Override
        public int compareTo(SaveItem another) {
            if(this.equals(another))
                return 0;
            return -1;
        }
    }
}