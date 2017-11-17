package com.cogentworks.overwidget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import layout.OverWidgetActivity;
import layout.OverWidgetActivityConfigureActivity;

/**
 * Created by cyun on 10/31/17.
 */

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, OverWidgetActivity.class));
        if (ids.length > 0) {
            AlarmUtil.scheduleUpdate(context);
            for (int appWidgetId : ids) {
                // Tell the AppWidgetManager to perform an update on the current App Widget
                WidgetUtils.loadUserPref(context, appWidgetManager, appWidgetId);
            }
        }
    }
}
