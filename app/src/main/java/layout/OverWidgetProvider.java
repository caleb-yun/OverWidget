package layout;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.cogentworks.overwidget.Profile;
import com.cogentworks.overwidget.UpdateService;
import com.cogentworks.overwidget.WidgetUtils;

import java.io.IOException;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverWidgetConfigure OverWidgetConfigure}
 */

public class OverWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "OverWidgetProvider";
    public static final String REFRESH_INTENT = "com.cogentworks.overwidget.action.UPDATE";
    public static final String SYNC_CLICKED = "com.cogentworks.overwidget.action.SYNC_CLICKED";
    //public static final String SYNC_CLICKED = REFRESH_INTENT;
    //private static final String URI_SCHEME = "OVRWG";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            //int interval = prefs.getInterval();
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, OverWidgetProvider.class);
            intent.setAction(OverWidgetProvider.REFRESH_INTENT);
            intent.putExtra("appWidgetId", appWidgetId);

            int updateInterval = 60*60*1000;
            Profile profile = WidgetUtils.loadUserPrefOffline(context, appWidgetId);
            if (profile != null)
                updateInterval = profile.getUpdateInterval();

            PendingIntent pi = PendingIntent.getBroadcast(context, appWidgetId, intent, appWidgetId);
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), updateInterval, pi);

            context.sendBroadcast(intent);

            WidgetUtils.setLoadingLayout(context, appWidgetId, AppWidgetManager.getInstance(context));
        }
    }

    private void onUpdate(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        ComponentName thisAppWidgetComponentName = new ComponentName(context.getPackageName(),getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WidgetUtils.deletePrefs(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        //AlarmUtil.scheduleUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        //AlarmUtil.clearUpdate(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "OnReceive: " + intent.getAction());

        /*if (SYNC_CLICKED.equals(intent.getAction())) {
            Log.d(TAG, "Refreshing");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Bundle extras = intent.getExtras();

            if (extras != null) {
                int appWidgetId = (int) extras.get("appWidgetId");
                Log.d(TAG, "appWidgetId: " + Integer.toString(appWidgetId));
                //WidgetUtils.loadUserPref(context, appWidgetManager, appWidgetId, true);
                Intent updateIntent = new Intent(context.getApplicationContext(), UpdateService.class);
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                context.startService(intent);

                Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show();
            }
        } else*/ if (REFRESH_INTENT.equals(intent.getAction()) || SYNC_CLICKED.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra("appWidgetId", 0);

            // If in power saving mode and not charging
            /*if (Build.VERSION.SDK_INT >= 23) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                if (pm.isPowerSaveMode() && !bm.isCharging()) {
                    if (SYNC_CLICKED.equals(intent.getAction())) {
                        Profile profile = WidgetUtils.loadUserPrefOffline(context, appWidgetId);
                        if (profile != null)
                            WidgetUtils.setWidgetViews(context, profile, appWidgetId, AppWidgetManager.getInstance(context));

                        Toast.makeText(context, "Power save mode is on", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }*/

            // Normal

            if (intent.getAction().equals(SYNC_CLICKED))
                WidgetUtils.setLoadingLayout(context, appWidgetId, AppWidgetManager.getInstance(context));

            Intent serviceIntent = new Intent(intent);
            serviceIntent.setAction("com.cogentworks.overwidget.UPDATE_SERVICE");
            serviceIntent.putExtra("appWidgetId", appWidgetId);

            UpdateService.enqueueWork(context, serviceIntent);
            context.startService(serviceIntent);

            if (SYNC_CLICKED.equals(intent.getAction()))
                Toast.makeText(context, "Refreshing (" + appWidgetId + ")", Toast.LENGTH_SHORT).show();
        }
        super.onReceive(context, intent);
    }



    //region onAppWidgetOptionsChanged

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        Log.d(TAG, "Changed dimensions");

        // Update widget
        Profile profile = WidgetUtils.loadUserPrefOffline(context, appWidgetId);
        if (profile != null) {
            WidgetUtils.setWidgetViews(context, profile, appWidgetId, appWidgetManager);
        }

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }



    //endregion
}

