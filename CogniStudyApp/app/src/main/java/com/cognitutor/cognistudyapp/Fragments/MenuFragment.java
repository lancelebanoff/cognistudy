package com.cognitutor.cognistudyapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.cognitutor.cognistudyapp.Activities.AchievementsActivity;
import com.cognitutor.cognistudyapp.Activities.BookmarksActivity;
import com.cognitutor.cognistudyapp.Activities.HelpActivity;
import com.cognitutor.cognistudyapp.Activities.SettingsActivity;
import com.cognitutor.cognistudyapp.Activities.ShopActivity;
import com.cognitutor.cognistudyapp.Activities.SuggestedQuestionsActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lance on 12/27/2015.
 */
public class MenuFragment extends CogniFragment {

    public static final MenuFragment newInstance() {
        return new MenuFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);

//        int[] allButtonIds = {
//                R.id.btnSuggestedQuestions,
//                R.id.btnBookmarks,
//                R.id.btnAchievements,
//                R.id.btnShop,
//                R.id.btnSettings,
//                R.id.btnHelp,
//                R.id.btnLogout
//        };
//
//        for(int buttonId : allButtonIds) {
//            Button b = (Button) rootView.findViewById(buttonId);
//            b.setOnClickListener(this);
//        }

        createMenuListView(rootView);

        return rootView;
    }

    private void createMenuListView(View rootView) {
        final ListView listView = (ListView) rootView.findViewById(R.id.listView);

        String[] from = new String[] {
                Constants.MenuItem.Attribute.LABEL,
                Constants.MenuItem.Attribute.ICON
        };
        int[] to = new int[] {
                R.id.txtLabel,
                R.id.imgIcon
        };
        List<HashMap<String, String>> menuItemMaps = new ArrayList<HashMap<String, String>>();

        List<MenuItem> menuItems = getMenuItems();
        for(MenuItem menuItem : menuItems) {
            HashMap<String, String> menuItemMap = new HashMap<>();
            menuItemMap.put(Constants.MenuItem.Attribute.LABEL, menuItem.label);
            menuItemMap.put(Constants.MenuItem.Attribute.ICON, Integer.toString(menuItem.icon));
            menuItemMaps.add(menuItemMap);
        }

        final SimpleAdapter adapter = new SimpleAdapter(
                rootView.getContext(), menuItemMaps, R.layout.list_item_menu, from, to
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap menuItemMap = (HashMap) adapter.getItem(position);
                String label = menuItemMap.get(Constants.MenuItem.Attribute.LABEL).toString();
                onClick(label);
            }
        });
    }

    private List<MenuItem> getMenuItems() {
        ArrayList<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(Constants.MenuItem.SUGGESTED_QUESTIONS, R.drawable.icon_settings));
        menuItems.add(new MenuItem(Constants.MenuItem.BOOKMARKS, R.drawable.icon_settings));
        menuItems.add(new MenuItem(Constants.MenuItem.ACHIEVEMENTS, R.drawable.icon_settings));
        menuItems.add(new MenuItem(Constants.MenuItem.SHOP, R.drawable.icon_settings));
        menuItems.add(new MenuItem(Constants.MenuItem.SETTINGS, R.drawable.icon_settings));
        menuItems.add(new MenuItem(Constants.MenuItem.HELP, R.drawable.icon_settings));
        menuItems.add(new MenuItem(Constants.MenuItem.SIGN_OUT, R.drawable.icon_settings));
        return menuItems;
    }

    private void onClick(String label) {
        switch(label) {
            case Constants.MenuItem.SUGGESTED_QUESTIONS:
                navigateToActivity(SuggestedQuestionsActivity.class);
                break;
            case Constants.MenuItem.BOOKMARKS:
                navigateToActivity(BookmarksActivity.class);
                break;
            case Constants.MenuItem.ACHIEVEMENTS:
                navigateToActivity(AchievementsActivity.class);
                break;
            case Constants.MenuItem.SHOP:
                navigateToActivity(ShopActivity.class);
                break;
            case Constants.MenuItem.SETTINGS:
                navigateToActivity(SettingsActivity.class);
                break;
            case Constants.MenuItem.HELP:
                navigateToActivity(HelpActivity.class);
                break;
            case Constants.MenuItem.SIGN_OUT:
                promptLogout();
                break;
        }
    }

    private void navigateToActivity(Class activityClass) {
        Intent intent = new Intent(getActivity(), activityClass);
        startActivity(intent);
    }

    private void promptLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure you want to log out from CogniStudy?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                try {
                    logout();
                } catch (ParseException e) { handleParseError(e); return; }
                navigateToRegistrationActivity();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public class MenuItem {

        public String label;
        public int icon;

        public MenuItem(String label, int icon) {
            this.label = label;
            this.icon = icon;
        }
    }
}
