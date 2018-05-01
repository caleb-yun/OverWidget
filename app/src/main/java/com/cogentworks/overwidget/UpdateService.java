package com.cogentworks.overwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import layout.OverWidgetConfigure;

/**
 * Created by cyun on 11/16/17.
 */

public class UpdateService extends JobIntentService {
    private Context context = this;
    static final int JOB_ID = 1000;
    static final String TAG = "UpdateService";

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, UpdateService.class, JOB_ID, work);
    }

    @Override
    public void onHandleWork(Intent intent) {
        if (intent != null) {
            int appWidgetId = intent.getIntExtra("appWidgetId", 0);
            Log.d(TAG, "Intent appWidgetId: " + appWidgetId);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

            //if (intent.getBooleanExtra("set_loading", false))
            WidgetUtils.setLoadingLayout(context, appWidgetId, appWidgetManager);

            try {
                Profile profile = WidgetUtils.getProfile(context, appWidgetId);
                if (profile != null && profile.BattleTag != null && !profile.BattleTag.equals("")) {
                    //WidgetUtils.setLoadingLayout(context, appWidgetId, AppWidgetManager.getInstance(context));
                    toGson(profile, appWidgetId);
                    WidgetUtils.setWidgetViews(context, profile, appWidgetId, appWidgetManager);
                } else if (profile != null && profile.getErrorMsg() != null) {
                    WidgetUtils.setErrorLayout(context, appWidgetId, appWidgetManager, profile.getErrorMsg());
                } else {
                    WidgetUtils.setErrorLayout(context, appWidgetId, appWidgetManager, "Error");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void toGson(Profile result, int appWidgetId) {
        SharedPreferences.Editor newPrefs = context.getSharedPreferences(OverWidgetConfigure.PREFS_NAME, 0).edit();
        Gson gson = new Gson();
        String profileJson = gson.toJson(result);
        newPrefs.putString(OverWidgetConfigure.PREF_PREFIX_KEY + appWidgetId + "_profile", profileJson);
        newPrefs.apply();
    }
}
    /*@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        Profile profile = new Profile();

        try {
            profile = WidgetUtils.getProfile(getApplicationContext(), appWidgetId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        WidgetUtils.setWidgetViews(getApplicationContext(), profile, appWidgetId, appWidgetManager);

        return super.onStartCommand(intent, flags, startId);
    }*/
