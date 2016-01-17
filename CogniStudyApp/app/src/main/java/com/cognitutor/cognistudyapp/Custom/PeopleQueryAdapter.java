package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.io.ByteArrayInputStream;

/**
 * Created by Kevin on 1/14/2016.
 */
public class PeopleQueryAdapter extends ParseQueryAdapter<ParseObject> {

    /*
    public PeopleQueryAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = PublicUserData.getQuery()
                        .fromLocalDatastore()
                        .whereEqualTo(PublicUserData.Columns.fbLinked, true)
                        .whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
                return query;
            }
        });
    }
    */
    public PeopleQueryAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = PublicUserData.getQuery()
                        .fromLocalDatastore()
                        .whereContainedIn(PublicUserData.Columns.objectId, PrivateStudentData.getFriendPublicUserIds())
                        .whereEqualTo(PublicUserData.Columns.fbLinked, true);
                //.whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
                return query;
            }
        });
    }

    @Override
    public View getItemView(ParseObject object, View view, ViewGroup parent) {
        final ViewHolder holder;
        if(view == null) {
            view = View.inflate(getContext(), R.layout.list_item_people, null);
            holder = createViewHolder(view);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        super.getItemView(object, view, parent);

        PublicUserData publicUserData = (PublicUserData) object;

        boolean isDataAvailable = publicUserData.isDataAvailable();
        if(isDataAvailable) {
            Log.i("getItemView", "Data is available");
        }
        else {
            Log.i("getItemView", "Data is not available");
        }

        byte[] data = publicUserData.getProfilePicData();
        if(data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, new BitmapFactory.Options());
            holder.imgProfile.setImageBitmap(bitmap);
        }
        else {
            ParseFile file = publicUserData.getProfilePic();
            if(file != null) {
                file.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e != null) {
                            Log.e("getItemView", "error " + e.getMessage());
                        }
                        else {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, new BitmapFactory.Options());
                            holder.imgProfile.setImageBitmap(bitmap);
                        }
                    }
                });
                Log.i("getItemView", "Parse File is available!");
                //holder.imgProfile.setParseFile(publicUserData.getProfilePic());
            }
            else {
                Log.i("getItemView", "Parse File is not available!");
            }
        }
        holder.txtName.setText(publicUserData.getDisplayName());

        return view;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) v.findViewById(R.id.txtName);
        holder.imgProfile = (RoundedImageView) v.findViewById(R.id.imgProfile);
        return holder;
    }

    private static class ViewHolder {
        public TextView txtName;
        public RoundedImageView imgProfile;
    }
}
