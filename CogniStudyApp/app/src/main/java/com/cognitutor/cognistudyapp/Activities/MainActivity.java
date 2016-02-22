package com.cognitutor.cognistudyapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.cognitutor.cognistudyapp.Custom.CogniViewPager;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.Fragments.AnalyticsFragment;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.Fragments.MenuFragment;
import com.cognitutor.cognistudyapp.Fragments.MessagesFragment;
import com.cognitutor.cognistudyapp.Fragments.PeopleFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionId;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryBlockStats;
import com.cognitutor.cognistudyapp.R;
import com.facebook.AccessToken;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import bolts.Capture;

public class MainActivity extends AuthenticationActivity {

    private final String TAG = "MainActivity";
    private Activity mActivity = this;
    private CogniViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            UserUtils.pinTest();
        }
        catch (ParseException e) { handleParseError(e); }


        // Sliding tabs
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (CogniViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setActivityRef(this);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        try {
            UserUtils.getPinTest();
        }
        catch (ParseException e) { handleParseError(e); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            UserUtils.getPinTest();
        }
        catch (ParseException e) { handleParseError(e); }
        test();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() == Fragments.Main.ordinal()) {
            super.onBackPressed();
        }
        else {
            mViewPager.setCurrentItem(Fragments.Main.ordinal());
        }
    }

    public enum Fragments {
        Main, People, Messages, Analytics, Menu
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(position == Fragments.Main.ordinal())
                return MainFragment.newInstance();
            if(position == Fragments.People.ordinal()) {
                return PeopleFragment.newInstance(new PeopleListOnClickHandler() {
                    @Override
                    public void onListItemClick(PublicUserData publicUserData) {
                        Intent intent = new Intent(mActivity, StudentProfileActivity.class);
                        intent.putExtra("publicUserDataId", publicUserData.getObjectId());
                        mActivity.startActivity(intent);
                    }
                });
            }
            if(position == Fragments.Messages.ordinal())
                return MessagesFragment.newInstance();
            if(position == Fragments.Analytics.ordinal())
                return AnalyticsFragment.newInstance();
            if(position == Fragments.Menu.ordinal())
                return MenuFragment.newInstance();
            return null;
        }

        @Override
        public int getCount() {
            return Fragments.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // TODO:3 make these images
            if(position == Fragments.Main.ordinal())
                return "Home";
            if(position == Fragments.People.ordinal())
                return "People";
            if(position == Fragments.Messages.ordinal())
                return "Messages";
            if(position == Fragments.Analytics.ordinal())
                return "Statistics";
            if(position == Fragments.Menu.ordinal())
                return "Menu";
            return null;
        }
    }
    private void test() {
//        DateUtils.test();
        Log.d("ClassName", Constants.ClassName.StudentCategoryDayStats);
        QueryUtils.testCacheThenNetwork();
    }
}
