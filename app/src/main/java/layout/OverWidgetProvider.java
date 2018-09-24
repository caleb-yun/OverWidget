package layout;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.cogentworks.overwidget.Profile;
import com.cogentworks.overwidget.UpdateService;
import com.cogentworks.overwidget.WidgetUtils;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverWidgetConfigure OverWidgetConfigure}
 */

public class OverWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "OverWidgetProvider";
    public static final String REFRESH_INTENT = "com.cogentworks.overwidget.action.UPDATE";
    public static final String SYNC_CLICKED = "com.cogentworks.overwidget.action.SYNC_CLICKED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            Intent intent = new Intent(context, OverWidgetProvider.class);
            intent.setAction(OverWidgetProvider.REFRESH_INTENT);
            intent.putExtra("appWidgetId", appWidgetId);
            PendingIntent pi = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            int updateInterval = 0;
            Profile profile = WidgetUtils.loadUserPrefOffline(context, appWidgetId);
            if (profile != null)
                updateInterval = profile.getUpdateInterval();
            //Log.d(TAG, "(" + appWidgetId + ") Update Interval: " + updateInterval);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            assert alarmManager != null;
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), updateInterval, pi);
            //alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 10*1000, pi); // TESTING


            context.sendBroadcast(intent);
            //WidgetUtils.setLoadingLayout(context, appWidgetId, AppWidgetManager.getInstance(context));
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WidgetUtils.deletePrefs(context, appWidgetId);

            // Cancel alarm
            Intent intent = new Intent(context, OverWidgetProvider.class);
            intent.setAction(OverWidgetProvider.REFRESH_INTENT);
            intent.putExtra("appWidgetId", appWidgetId);
            PendingIntent pi = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            assert alarmManager != null;
            alarmManager.cancel(pi);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, "OnReceive: " + intent.getAction());

        if (REFRESH_INTENT.equals(intent.getAction()) || SYNC_CLICKED.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra("appWidgetId", 0);

            Intent serviceIntent = new Intent(intent);
            serviceIntent.setAction("com.cogentworks.overwidget.UPDATE_SERVICE");
            serviceIntent.putExtra("appWidgetId", appWidgetId);

            UpdateService.enqueueWork(context, serviceIntent);
            context.startService(serviceIntent);

            if (SYNC_CLICKED.equals(intent.getAction())) {
                //Toast.makeText(context, "Refreshing (" + appWidgetId + ")", Toast.LENGTH_SHORT).show();
                WidgetUtils.setLoadingLayout(context, appWidgetId, AppWidgetManager.getInstance(context));
            }

        }
        super.onReceive(context, intent);
    }



    //region onAppWidgetOptionsChanged

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        //Log.d(TAG, "Changed dimensions");

        // Update widget
        Profile profile = WidgetUtils.loadUserPrefOffline(context, appWidgetId);
        if (profile != null) {
            WidgetUtils.setWidgetViews(context, profile, appWidgetId, appWidgetManager);
        }

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }



    //endregion
}

