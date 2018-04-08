package com.cogentworks.overwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import java.io.IOException;

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
            WidgetUtils.setLoadingLayout(context, appWidgetId, appWidgetManager);

            try {
                Profile profile = WidgetUtils.getProfile(context, appWidgetId);
                if (profile != null && !profile.BattleTag.equals("")) {
                    //WidgetUtils.setLoadingLayout(context, appWidgetId, AppWidgetManager.getInstance(context));
                    WidgetUtils.setWidgetViews(context, profile, appWidgetId, appWidgetManager);
                } else {
                    WidgetUtils.setErrorLayout(context, appWidgetId, appWidgetManager, profile.getErrorMsg());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
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
