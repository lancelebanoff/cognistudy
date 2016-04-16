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

import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.Custom.TutorRequestListView;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.HashSet;
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
    private TutorRequestListView mListView;

    public TutorRequestAdapter(Context context, MainFragment fragment, TutorRequestListView listView, List<PublicUserData> tutorRequests) {
        super(context, R.layout.list_item_tutor_request, tutorRequests);
        mActivity = (Activity) context;
        mFragment = fragment;
        mListView = listView;
    }

    public TutorRequestAdapter(Context context, MainFragment fragment, TutorRequestListView listView) {
        super(context, R.layout.list_item_tutor_request);
        mActivity = (Activity) context;
        mFragment = fragment;
        mListView = listView;
    }

    public void loadTutorRequests(final boolean getFromNetwork) {
        if(mPrivateStudentData == null) {
            PrivateStudentData.getPrivateStudentDataInBackground().continueWith(new Continuation<PrivateStudentData, Void>() {
                @Override
                public Void then(Task<PrivateStudentData> task) throws Exception {
                    mPrivateStudentData = task.getResult();
                    doLoadTutorRequests(getFromNetwork);
                    return null;
                }
            });
        }
        else {
            doLoadTutorRequests(getFromNetwork);
        }
    }

    private void doLoadTutorRequests(boolean getFromNetwork) {

        doGetTutorRequests();
        if(getFromNetwork) {
            mPrivateStudentData.fetchInBackground().continueWith(new Continuation<ParseObject, Object>() {
                @Override
                public Object then(Task<ParseObject> task) throws Exception {
                    doGetTutorRequests();
                    return null;
                }
            });
        }
    }

    private void doGetTutorRequests() {
        final List<PublicUserData> tutorRequests = mPrivateStudentData.getTutorRequests();

        boolean update = false;
        HashSet<String> oldIds = new HashSet<>();
        for(int i=0; i<getCount(); i++) {
            oldIds.add(getItem(i).getObjectId());
        }
        for(PublicUserData newPud : tutorRequests) {
            String newId = newPud.getObjectId();
            if(!oldIds.contains(newId)) {
                update = true;
                break;
            }
            oldIds.remove(newId);
        }
        if(oldIds.size() != 0)
            update = true;

        if(update) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clear();
                    for(PublicUserData pud : tutorRequests) {
                        add(pud);
                    }
                    notifyDataSetChanged();

                    if(getCount() == 0) {
                        mListView.getParentCardView().setVisibility(View.GONE);
                    } else {
                        mListView.getParentCardView().setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        PublicUserData tutor = null;
        try {
            tutor = getItem(position).fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_tutor_request, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setItemViewContents(holder, tutor);

        final PublicUserData tutorPublicUserData = tutor;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptAcceptRequest(tutorPublicUserData);
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
        mPrivateStudentData.linkTutor(tutorPublicUserData);
        loadTutorRequests(false);
    }

    private void rejectRequest(PublicUserData tutorPublicUserData) {
        mPrivateStudentData.removeTutorRequest(tutorPublicUserData);
        mPrivateStudentData.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                loadTutorRequests(false);
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
