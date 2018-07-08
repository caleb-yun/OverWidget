package com.cogentworks.overwidget;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;

import com.woxthebox.draglistview.DragItemAdapter;

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
            Profile profile = WidgetUtils.getProfile(battleTag, platform, region, null, null);
            if (profile.BattleTag != null)
                profile.Id = profile.BattleTag.hashCode();
            return profile;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Profile result) {
        if (result != null && result.BattleTag != null) {
            MainActivity activity = (MainActivity) context;
            activity.dbHelper.insertNewProfile(battleTag, result);
            DragItemAdapter adapter = activity.mDragListView.getAdapter();
            adapter.addItem(adapter.getItemCount(), result);
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
