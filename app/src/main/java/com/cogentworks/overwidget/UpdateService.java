package com.cogentworks.overwidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import java.io.IOException;

import layout.OverWidgetActivity;
import layout.OverWidgetActivityConfigureActivity;

/**
 * Created by cyun on 11/16/17.
 */

public class UpdateService extends Service {

    @Nullable
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

        WidgetUtils.SetWidgetViews(getApplicationContext(), profile, appWidgetId, appWidgetManager);

        return super.onStartCommand(intent, flags, startId);
    }
}
