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

import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.Fragments.AnalyticsFragment;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.Fragments.MenuFragment;
import com.cognitutor.cognistudyapp.Fragments.MessagesFragment;
import com.cognitutor.cognistudyapp.Fragments.PeopleFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.facebook.AccessToken;
import com.parse.ParseException;

public class MainActivity extends AuthenticationActivity {

    private final String TAG = "MainActivity";
    private Activity mActivity = this;
    private ViewPager mViewPager;

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
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Log.i(TAG, "Access Token: " + AccessToken.getCurrentAccessToken().getToken());
        try {
            UserUtils.getPinTest();
        }
        catch (ParseException e) { handleParseError(e); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Access Token: " + AccessToken.getCurrentAccessToken().getToken());
        try {
            UserUtils.getPinTest();
        }
        catch (ParseException e) { handleParseError(e); }
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
        if(mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        }
        else {
            mViewPager.setCurrentItem(0);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MainFragment.newInstance();
                case 1:
                    return PeopleFragment.newInstance(new PeopleListOnClickHandler() {
                        @Override
                        public void onListItemClick(PublicUserData publicUserData) {
                            Intent intent = new Intent(mActivity, StudentProfileActivity.class);
                            intent.putExtra("publicUserDataId", publicUserData.getObjectId());
                            mActivity.startActivity(intent);
                        }
                    });
                case 2:
                    return MessagesFragment.newInstance();
                case 3:
                    return AnalyticsFragment.newInstance();
                case 4:
                    return MenuFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // TODO:3 make these images
            switch (position) {
                case 0:
                    return "Home";
                case 1:
                    return "People";
                case 2:
                    return "Messages";
                case 3:
                    return "Statistics";
                case 4:
                    return "Menu";
            }
            return null;
        }
    }
}
