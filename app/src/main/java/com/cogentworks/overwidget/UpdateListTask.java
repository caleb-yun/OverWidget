package com.cogentworks.overwidget;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.IOException;
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
        ((MainActivity)context).isBusy = true;

        if (profiles.size() == 0) {
            error = false;
            return null;
        }

        try {
            for (int i = 0; i < profiles.size(); i++) {
                Profile profile = profiles.get(i);
                profiles.set(i, WidgetUtils.getProfile(profile.BattleTag, profile.Platform, profile.Region, null, null));
                Log.d("UpdateListTask", profile.BattleTag);
            }
            return profiles;
        } catch (IOException ex) {
            ex.printStackTrace();
            Snackbar.make(((Activity) context).findViewById(R.id.layout_main), "Could not update player", Snackbar.LENGTH_LONG).show();
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        error = true;
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Profile> result) {
        if (result != null) {
            MainActivity activity = (MainActivity) context;
            for (Profile profile : result)
                try {
                    activity.dbHelper.updateItem(profile.BattleTag, profile);
                } catch (IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }

            activity.showItemList();
        } else {
            if (error)
                Snackbar.make(((Activity) context).findViewById(R.id.layout_main), "An update error occurred", Snackbar.LENGTH_LONG).show();
        }

        ((MainActivity)context).isBusy = false;
    }
}
