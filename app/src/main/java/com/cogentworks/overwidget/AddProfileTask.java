package com.cogentworks.overwidget;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;

import java.io.IOException;

public class AddProfileTask extends AsyncTask<String, Void, Profile> {

    private Context context;

    private String battleTag;
    private String platform;
    private String region;

    public AddProfileTask(Context context, String battleTag, String platform, String region) {
        this.context = context;
        this.battleTag = battleTag;
        this.platform = platform;
        this.region = region;
    }

    @Override
    protected Profile doInBackground(String... params) {
        ((MainActivity)context).isBusy = true;
        try {
            return WidgetUtils.getProfile(battleTag, platform, region, null, null);
        } catch (IOException ex) {
            ex.printStackTrace();
            Snackbar.make(((Activity) context).findViewById(R.id.swiperefresh), "An error occurred", Snackbar.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Profile result) {
        if (result != null && result.BattleTag != null) {
            MainActivity activity = (MainActivity) context;
            activity.dbHelper.insertNewProfile(battleTag, result);
            activity.showItemList();
        } else {
            String error;
            if (result != null)
                error = result.getErrorMsg();
            else
                error = "An error occurred";
            Snackbar.make(((Activity) context).findViewById(R.id.swiperefresh), error, Snackbar.LENGTH_LONG).show();
        }

        ((MainActivity)context).isBusy = false;
    }
}
