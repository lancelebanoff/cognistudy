package com.cognitutor.cognistudyapp.Activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;

import com.cognitutor.cognistudyapp.Custom.CogniViewPager;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.AnalyticsFragment;
import com.cognitutor.cognistudyapp.Fragments.ConversationsFragment;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.Fragments.MenuFragment;
import com.cognitutor.cognistudyapp.Fragments.PeopleFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTRollingStats;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;

public class MainActivity extends AuthenticationActivity {

    public boolean challengesFinishedLoading = false;

    private final String TAG = "MainActivity";
    private Activity mActivity = this;
    private CogniViewPager mViewPager;
    private PeopleFragment mPeopleFragment;
    private AnalyticsFragment mAnalyticsFragment;

    private static boolean cameFromChatActivity = false;
    public static void setCameFromChatActivity(boolean val) {
        cameFromChatActivity = val;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StudentTRollingStats.updateAllCacheElseNetworkInBackground();

        setContentView(R.layout.activity_main);

        // Sliding tabs
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (CogniViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setActivityRef(this);
        mViewPager.setOffscreenPageLimit(5);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));
        setTabLayoutIconsAndColors(tabLayout);

        // Set shared preferences based on database values
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        try {
            Student student = PublicUserData.getPublicUserData().getStudent();
            prefsEditor.putBoolean(Constants.Settings.RANDOM_MATCHING, student.getRandomEnabled());
            prefsEditor.putBoolean(Constants.Settings.SKIP_BUNDLES, student.getSkipBundles());
            prefsEditor.putBoolean(Constants.Settings.ENABLE_NOTIFICATIONS, student.getNotificationsEnabled());
            prefsEditor.apply();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setTabLayoutIconsAndColors(TabLayout tabLayout) {
        // Initialize tab icons and colors
        for(int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            switch (i) {
                case 0:
                    tab.setIcon(R.drawable.icon_home); // TODO:1 google material icons
                    break;
                case 1:
                    tab.setIcon(R.drawable.icon_people);
                    break;
                case 2:
                    tab.setIcon(R.drawable.icon_messages);
                    break;
                case 3:
                    tab.setIcon(R.drawable.icon_analytics);
                    break;
                case 4:
                    tab.setIcon(R.drawable.icon_menu);
                    break;
            }
            int color;
            if(i == 0) {
                color = ContextCompat.getColor(mActivity, R.color.white);
            } else {
                color = ContextCompat.getColor(mActivity, R.color.colorPrimaryLight);
            }
            tab.getIcon().mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }

        // Change colors when tabs are selected
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                tab.getIcon().setColorFilter(
                        ContextCompat.getColor(mActivity, R.color.white),
                        PorterDuff.Mode.MULTIPLY
                );
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
                tab.getIcon().mutate().setColorFilter(
                        ContextCompat.getColor(mActivity, R.color.colorPrimaryLight),
                        PorterDuff.Mode.MULTIPLY
                );
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);
                tab.getIcon().setColorFilter(
                        ContextCompat.getColor(mActivity, R.color.white),
                        PorterDuff.Mode.MULTIPLY
                );
            }
        });
    }

    //TODO: Remove testing later
    @Override
    protected void onResume() {
        super.onResume();
        setFragment();
        if (mAnalyticsFragment != null) {
            mAnalyticsFragment.getAndDisplayAnalytics();
        }
    }

    private void setFragment() {
        if(cameFromChatActivity) {
            cameFromChatActivity = false;
            mViewPager.setCurrentItem(Fragments.Messages.ordinal());
        }
    }

    //TODO: Remove testing later
    @Override
    protected void onPause() {
        super.onPause();
        onPauseTest();
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
                mPeopleFragment = PeopleFragment.newInstance(PeopleFragment.getNavigateToProfileHandler(mActivity), false);
                return mPeopleFragment;
            }
            if(position == Fragments.Messages.ordinal())
                return ConversationsFragment.newInstance();
            if(position == Fragments.Analytics.ordinal()) {
                mAnalyticsFragment = AnalyticsFragment.newInstance();
                return mAnalyticsFragment;
            }
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
//            if(position == Fragments.Main.ordinal())
//                return "Home";
//            if(position == Fragments.People.ordinal())
//                return "People";
//            if(position == Fragments.Messages.ordinal())
//                return "Messages";
//            if(position == Fragments.Analytics.ordinal())
//                return "Statistics";
//            if(position == Fragments.Menu.ordinal())
//                return "Menu";
            return "";
        }
    }

    //Used when a challenge finishes loading so that the opponent shows up in the people list
    public void updatePeopleFragment() {
        mPeopleFragment.updateList();
    }

    private static boolean onPauseFinished = false;

    private void onPauseTest() {
//        ParseObjectUtils.testPins(true);
    }
}
