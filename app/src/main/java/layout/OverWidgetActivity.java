package layout;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cogentworks.overwidget.Profile;
import com.cogentworks.overwidget.R;
import com.cogentworks.overwidget.SetAvatarBmp;
import com.cogentworks.overwidget.SetLevelBmp;
import com.cogentworks.overwidget.UpdateService;
import com.cogentworks.overwidget.WidgetUtils;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverWidgetActivityConfigureActivity OverWidgetActivityConfigureActivity}
 */
public class OverWidgetActivity extends AppWidgetProvider {
    private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";

    private static final String TAG = "OverWidgetActivity";
    public static final String REFRESH_INTENT = "com.cogentworks.overwidget.action.UPDATE";
    //private static final String URI_SCHEME = "OVRWG";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            //int interval = prefs.getInterval();
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, OverWidgetActivity.class);
            intent.setAction(OverWidgetActivity.REFRESH_INTENT);
            intent.putExtra("appWidgetId", appWidgetId);
            PendingIntent pi = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000*60*60*2, pi);
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
        Log.d(TAG, "OnReceive");

        if (SYNC_CLICKED.equals(intent.getAction())) {
            Log.d(TAG, "Refreshing");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Bundle extras = intent.getExtras();

            if (extras != null) {
                int appWidgetId = (int) extras.get("WIDGET_ID");
                Log.d(TAG, Integer.toString(appWidgetId));
                //WidgetUtils.loadUserPref(context, appWidgetManager, appWidgetId, true);
                Intent updateIntent = new Intent(context.getApplicationContext(), UpdateService.class);
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                context.startService(intent);

                Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show();
            }
        } else if (REFRESH_INTENT.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra("appWidgetId", 0);
            Intent serviceIntent = new Intent(intent);
            serviceIntent.setAction("com.cogentworks.overwidget.UPDATE_SERVICE");
            serviceIntent.putExtra("appWidgetId", appWidgetId);
            UpdateService.enqueueWork(context, serviceIntent);
            context.startService(serviceIntent);
        } else super.onReceive(context, intent);
    }



    //region onAppWidget OptionsChanged

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        Log.d(TAG, "Changed dimensions");

        // Update widget
        Profile profile = WidgetUtils.loadUserPrefOffline(context, appWidgetId);
        if (profile != null) {
            WidgetUtils.SetWidgetViews(context, profile, appWidgetId, appWidgetManager);
        }

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }



    //endregion
}

