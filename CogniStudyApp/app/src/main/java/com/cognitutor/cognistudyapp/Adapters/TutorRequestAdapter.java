package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/14/2016.
 */
public class TutorRequestAdapter extends ArrayAdapter<PublicUserData> {

    private Activity mActivity;
    private MainFragment mFragment;
    private PrivateStudentData mPrivateStudentData;

    public TutorRequestAdapter(Context context, MainFragment fragment, List<PublicUserData> tutorRequests) {
        super(context, R.layout.list_item_tutor_request, tutorRequests);
        mActivity = (Activity) context;
        mFragment = fragment;
        mPrivateStudentData = PrivateStudentData.getPrivateStudentData();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final PublicUserData tutor = getItem(position);

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_tutor_request, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setItemViewContents(holder, tutor);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptAcceptRequest(tutor);
            }
        });

        return convertView;
    }

    private void promptAcceptRequest(final PublicUserData tutorPublicUserData) {

        new AlertDialog.Builder(mActivity)
                .setTitle("Tutor Request")
                .setMessage("Would you like to add " + tutorPublicUserData.getDisplayName() + " as your tutor?")
                .setNeutralButton(R.string.cancel_dialog_accept_challenge, null)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rejectRequest(tutorPublicUserData);
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        acceptRequest(tutorPublicUserData);
                    }
                }).create().show();
    }

    private void acceptRequest(PublicUserData tutorPublicUserData) {
        mPrivateStudentData.removeTutorRequest(tutorPublicUserData);
        mPrivateStudentData.addTutor(tutorPublicUserData);
        mPrivateStudentData.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                mFragment.refresh();
            }
        });

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("studentPublicDataId", PublicUserData.getPublicUserData().getObjectId());
        params.put("tutorPublicDataId", tutorPublicUserData.getObjectId());
        ParseCloud.callFunctionInBackground(Constants.CloudCodeFunction.ADD_STUDENT, params).continueWith(new Continuation<Object, Void>() {
            @Override
            public Void then(Task<Object> task) throws Exception {
                if (task.getError() != null) {
                    task.getError().printStackTrace();
                }
                return null;
            }
        });
    }

    private void rejectRequest(PublicUserData tutorPublicUserData) {
        mPrivateStudentData.removeTutorRequest(tutorPublicUserData);
        mPrivateStudentData.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                mFragment.refresh();
            }
        });
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) v.findViewById(R.id.txtName);
        holder.imgProfile = (RoundedImageView) v.findViewById(R.id.imgProfileRounded);
        return holder;
    }

    private void setItemViewContents(ViewHolder holder, PublicUserData tutorPublicUserData) {
        holder.imgProfile.setParseFile(tutorPublicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtName.setText(tutorPublicUserData.getDisplayName());
    }

    private static class ViewHolder {
        public TextView txtName;
        public RoundedImageView imgProfile;
    }
}
