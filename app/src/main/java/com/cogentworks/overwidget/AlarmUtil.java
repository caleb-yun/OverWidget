package com.cogentworks.overwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import layout.OverWidgetProvider;

/**
 * Created by cyun on 9/18/17.
 */

public class AlarmUtil {

    public static void scheduleUpdate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //String interval = prefs.getString(SettingsActivity.INTERVAL_PREF, null);
        String interval = "60";

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = Integer.parseInt(interval)*60*1000;

        PendingIntent pi = getAlarmIntent(context);
        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalMillis, pi);
    }

    private static PendingIntent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, OverWidgetProvider.class);
        intent.setAction(OverWidgetProvider.REFRESH_INTENT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }

    public static void clearUpdate(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getAlarmIntent(context));
    }

}
