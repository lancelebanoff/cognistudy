package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Achievement;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionIds;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Bookmark;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.GameBoard;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Message;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PinnedObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionBundle;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionContents;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionReport;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Ship;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryDayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryTridayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectDayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectTridayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTotalDayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTotalMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTotalRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTotalTridayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SuggestedQuestion;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Tutor;
import com.cognitutor.cognistudyapp.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;


/**
 * Created by Kevin on 12/30/2015.
 */
public class App extends Application {

    public static CallbackManager callbackManager;
    public static AccessTokenTracker accessTokenTracker;
    public static ProfileTracker profileTracker;
    public static boolean isLocalDatastoreEnabled;
    private static boolean initFinished = false;
    private static ConnectivityManager cMgr = null;

    @Override public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        isLocalDatastoreEnabled = true;
        registerSubclasses();
        Parse.initialize(this, Constants.Parse.APPLICATION_ID, Constants.Parse.CLIENT_KEY);
        ParseFacebookUtils.initialize(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());

        Foreground.init(this);
        initFinished = true;

        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                //TODO: I don't think we need to do this
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                //TODO: Do something here
            }

        };

        cMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isInitFinished() { return initFinished; }

    private void registerSubclasses() {
        ParseObject.registerSubclass(Achievement.class);
        ParseObject.registerSubclass(Bookmark.class);
        ParseObject.registerSubclass(PrivateStudentData.class);
        ParseObject.registerSubclass(PublicUserData.class);
        ParseObject.registerSubclass(Student.class);
        ParseObject.registerSubclass(Challenge.class);
        ParseObject.registerSubclass(ChallengeUserData.class);
        ParseObject.registerSubclass(GameBoard.class);
        ParseObject.registerSubclass(Ship.class);
        ParseObject.registerSubclass(StudentCategoryRollingStats.class);
        ParseObject.registerSubclass(StudentSubjectRollingStats.class);
        ParseObject.registerSubclass(StudentTotalRollingStats.class);
        ParseObject.registerSubclass(Question.class);
        ParseObject.registerSubclass(QuestionContents.class);
        ParseObject.registerSubclass(QuestionData.class);
        ParseObject.registerSubclass(QuestionBundle.class);
        ParseObject.registerSubclass(AnsweredQuestionIds.class);
        ParseObject.registerSubclass(Response.class);
        ParseObject.registerSubclass(StudentCategoryDayStats.class);
        ParseObject.registerSubclass(StudentCategoryTridayStats.class);
        ParseObject.registerSubclass(StudentCategoryMonthStats.class);
        ParseObject.registerSubclass(StudentSubjectDayStats.class);
        ParseObject.registerSubclass(StudentSubjectTridayStats.class);
        ParseObject.registerSubclass(StudentSubjectMonthStats.class);
        ParseObject.registerSubclass(StudentTotalDayStats.class);
        ParseObject.registerSubclass(StudentTotalTridayStats.class);
        ParseObject.registerSubclass(StudentTotalMonthStats.class);
        ParseObject.registerSubclass(PinnedObject.class);
        ParseObject.registerSubclass(SuggestedQuestion.class);
        ParseObject.registerSubclass(Tutor.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Conversation.class);
        ParseObject.registerSubclass(QuestionReport.class);
    }

    // Check for Internet connectivity
    public static boolean isNetworkConnected() {

        //Log.d(TAG, "Checking connectivity");
        if ( cMgr != null){
            NetworkInfo info = cMgr.getActiveNetworkInfo();
            if (info!= null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        //Log.d(TAG, "No internet connection");
        return false;
    }
}

class Foreground implements Application.ActivityLifecycleCallbacks {

    private static Foreground instance;
    private boolean foreground;

    public static void init(Application app){
        if (instance == null){
            instance = new Foreground();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static Foreground get(){
        return instance;
    }

    private Foreground(){}

    public boolean isBackground() {
        return !foreground;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(ParseUser.getCurrentUser() != null) {
            StudentTRollingStats.updateAllCacheElseNetworkInBackground();
        }
        foreground = true;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}

