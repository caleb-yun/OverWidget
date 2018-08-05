package com.cogentworks.overwidget;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class UpdateListTask extends AsyncTask<String, Void, ArrayList<Profile>> {

    private Context context;

    private ArrayList<Profile> profiles;
    private boolean error = false;

    public UpdateListTask(Context context, ArrayList<Profile> profiles) {
        this.context = context;
        this.profiles = profiles;
    }

    @Override
    protected ArrayList<Profile> doInBackground(String... params) {
        ((MainActivity) context).isBusy = true;
        ((MainActivity) context).disableDrag(true);

        if (profiles.size() == 0) {
            error = false;
            return null;
        }

        for (int i = 0; i < profiles.size(); i++) {
            Profile profile = profiles.get(i);
            try {
                int id = profile.Id;

                Profile newProfile = WidgetUtils.getProfile(profile.BattleTag, profile.Platform, profile.Region, null, null);
                if (newProfile == null)
                    Snackbar.make(((Activity) context).findViewById(R.id.list), "Could not update a player", Snackbar.LENGTH_LONG).show();
                else if(newProfile.BattleTag != null) {
                    newProfile.Id = id;
                    profiles.set(i, newProfile);
                } else {
                    Snackbar.make(((Activity) context).findViewById(R.id.list), newProfile.getErrorMsg(), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                error = true;
                return null;
            }
        }
        return profiles;
    }

    @Override
    protected void onPostExecute(ArrayList<Profile> result) {

        MainActivity activity = (MainActivity) context;
        if (result != null) {
            activity.dbHelper.setList(result);
            activity.mDragListView.getAdapter().setItemList(result);


        } else {
            if (error)
                Snackbar.make(activity.findViewById(R.id.layout_main), "An update error occurred", Snackbar.LENGTH_LONG).show();
        }

        activity.isBusy = false;
        ((SwipeRefreshLayout) activity.findViewById(R.id.swiperefresh)).setRefreshing(false);
        activity.disableDrag(false);
    }
}
