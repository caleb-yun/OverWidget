package com.cogentworks.overwidget;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.IOException;

import layout.OverWidgetConfigure;
import layout.OverWidgetProvider;

import static android.app.Activity.RESULT_OK;

/**
 * Created by cyun on 8/6/17.
 */

public class CreateWidget extends AsyncTask<String, Void, Profile> {
    private static final String TAG = "CreateWidget";

    private Context context;
    private int appWidgetId;

    private Activity mActivity;

    private String battleTag;
    private String platform;
    private String region;


    public CreateWidget(Context context, int appWidgetId) {
        this.context = context;
        this.appWidgetId = appWidgetId;
        this.mActivity = (Activity) context;
    }

    @Override
    protected Profile doInBackground(String... params) {
        Profile result = null;

        battleTag = params[0];
        platform = params[1];
        region = params[2];

        if (battleTag != null && platform != null && region != null) {
            try {
                result = WidgetUtils.getProfile(context, appWidgetId);
            } catch (IOException ex) {
                ex.printStackTrace();
                result = null;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Profile result) {
        super.onPostExecute(result);

        ProgressBar progressBar = mActivity.findViewById(R.id.progress_bar);
        LinearLayout content = mActivity.findViewById(R.id.layout_main);
        FloatingActionButton fab = mActivity.findViewById(R.id.fab);

        if (result != null && result.BattleTag != null && !result.BattleTag.equals("")) {
            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            WidgetUtils.setWidgetViews(context, result, this.appWidgetId, appWidgetManager);

            // Convert Profile to Gson and save to SharedPrefs
            toGson(result);

            // Set AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, OverWidgetProvider.class);
            intent.setAction(OverWidgetProvider.REFRESH_INTENT);
            intent.putExtra("appWidgetId", appWidgetId);

            int updateInterval = result.getUpdateInterval();
            //Log.d(TAG, "(" + appWidgetId + ") Update Interval: " + updateInterval);

            PendingIntent pi = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            assert alarmManager != null;
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), updateInterval, pi);
            //alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 10*1000, pi);


            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            mActivity.setResult(RESULT_OK, resultValue);
            mActivity.finish();
            //Log.d(TAG, "Check profile completed");
        } else {
            OverWidgetConfigure.crossfade(context, content, progressBar);
            fab.show();
            
            try {
                Snackbar.make(content, result.getErrorMsg(), Snackbar.LENGTH_SHORT).show(); // Add "Retry" action
            } catch (Exception e) {
                Snackbar.make(content, "Network Error", Snackbar.LENGTH_SHORT).show();
            }

            final LinearLayout fContent = content;
            final ProgressBar fProgressBar = progressBar;

            // Make sure animation is finished
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fProgressBar.setVisibility(View.GONE);
                    fContent.setVisibility(View.VISIBLE);
                    fContent.setAlpha(1f);
                }
            }, context.getResources().getInteger(android.R.integer.config_shortAnimTime));

        }
        //}
    }

    private void toGson(Profile result) {
        SharedPreferences.Editor newPrefs = context.getSharedPreferences(OverWidgetConfigure.PREFS_NAME, 0).edit();
        Gson gson = new Gson();
        String profileJson = gson.toJson(result);
        newPrefs.putString(OverWidgetConfigure.PREF_PREFIX_KEY + appWidgetId + "_profile", profileJson);
        newPrefs.apply();
    }
}