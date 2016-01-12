package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.Fragments.AnalyticsFragment;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.Fragments.MenuFragment;
import com.cognitutor.cognistudyapp.Fragments.MessagesFragment;
import com.cognitutor.cognistudyapp.Fragments.PeopleFragment;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;

public class MainActivity extends AuthenticationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sliding tabs
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

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


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MainFragment();
                case 1:
                    return new PeopleFragment();
                case 2:
                    return new MessagesFragment();
                case 3:
                    return new AnalyticsFragment();
                case 4:
                    return new MenuFragment();
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
