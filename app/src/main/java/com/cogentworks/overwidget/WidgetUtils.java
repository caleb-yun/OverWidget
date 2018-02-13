package com.cogentworks.overwidget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import layout.OverWidgetProvider;

/**
 * Created by cyun on 11/16/17.
 */

public class WidgetUtils {
    //private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";
    private static final String SYNC_CLICKED = OverWidgetProvider.SYNC_CLICKED;
    public static final String TAG = "WidgetUtils";

    public static final String PREFS_NAME = "layout.OverWidgetProvider";
    public static final String PREF_PREFIX_KEY = "overwidget_";

    public static void SetWidgetViews(Context context, Profile profile, int appWidgetId, AppWidgetManager appWidgetManager) {
        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // Get min width and height.
        int columns = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
        Log.d(TAG, "Columns: " + columns);
        //int rows = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));

        // Set up layout
        RemoteViews views;
        if (columns == 1) {
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);
        } else if (columns == 2) {
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity_2);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity_3);
        }

        // Populate the RemoteViews object

        // General views
        views.setTextViewText(R.id.appwidget_battletag, profile.BattleTag);
        // Comp Rank
        views.setImageViewBitmap(R.id.appwidget_comprank, WidgetUtils.BuildTextBmp(profile.CompRank, context));
        views.setImageViewResource(R.id.appwidget_tier, context.getResources().getIdentifier(profile.Tier, "drawable", context.getPackageName()));
        // Tap to refresh
        views.setOnClickPendingIntent(R.id.appwidget_layout, getPendingSelfIntent(context, SYNC_CLICKED, appWidgetId));

        // Specific views
        if (columns >= 2){
            // Level
            SetLevelBmp setLevelBmp = new SetLevelBmp(context, appWidgetManager, appWidgetId, views);
            setLevelBmp.execute(profile.RankImageURL, profile.Prestige, profile.Level);
        }
        if (columns >= 3){
            // Avatar
            SetAvatarBmp setAvatarBmp = new SetAvatarBmp(context, appWidgetManager, appWidgetId, views);
            setAvatarBmp.execute(profile.AvatarURL);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.d(TAG, "SetWidgetViews");
    }

    public static void SetSyncClicked(Context context, int appWidgetId, AppWidgetManager appWidgetManager) {
        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        // Get min width and height.
        int columns = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
        int rows = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        // Set up layout
        RemoteViews views = null;
        if (columns == 1) {
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);
        } else if (columns == 2) {
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity_2);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity_3);
        }

        views.setTextViewText(R.id.appwidget_battletag, "Tap to refresh");
        views.setOnClickPendingIntent(R.id.appwidget_layout, getPendingSelfIntent(context, SYNC_CLICKED, appWidgetId));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // Returns number of cells needed for given size of the widget
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action, int appWidgetId) {
        Intent intent = new Intent(context, OverWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra("WIDGET_ID", appWidgetId);

        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Profile getProfile(Context context, int appWidgetId) throws IOException {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String battleTag = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_battletag", null);
        String platform = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_platform", null);
        String region = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_region", null);
        if (battleTag != null && platform != null && region != null) {
            Profile result = new Profile();

            URL endpoint = new URL("https://owapi.net/api/v3/u/" + battleTag.replace('#', '-') + "/blob?platform=" + platform.toLowerCase());
            HttpsURLConnection urlConnection = (HttpsURLConnection) endpoint.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                // Success
                InputStream responseBody = new BufferedInputStream(urlConnection.getInputStream());
                // Parser
                JsonParser jsonParser = new JsonParser();
                JsonObject stats = jsonParser.parse(new InputStreamReader(responseBody, "UTF-8"))
                        .getAsJsonObject().get(region.toLowerCase()) // Select Region
                        .getAsJsonObject().get("stats")
                        .getAsJsonObject().get("competitive")
                        .getAsJsonObject().getAsJsonObject("overall_stats");
                result.SetUser(battleTag, stats.get("avatar").getAsString());
                result.SetLevel(stats.get("level").getAsString(), stats.get("prestige").getAsString(), stats.get("rank_image").getAsString());
                try {
                    result.SetRank(stats.get("comprank").getAsString(), stats.get("tier").getAsString());
                } catch (UnsupportedOperationException e) {
                    result.SetRank("- - -", "nullrank");
                }
                responseBody.close();
                Log.d(TAG, "responseBody.close");
            } else {
                // Other response code
                Log.e(TAG, urlConnection.getResponseMessage());
                return null;
            }
            urlConnection.disconnect();
            return result;
        } else {
            return null;
        }
    }

    // Write the prefix to the SharedPreferences object for this widget
    public static void savePrefs(Context context, int appWidgetId, String battleTag, String platform, String region) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_battletag", battleTag);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_platform", platform);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_region", region);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static void loadUserPref(Context context, AppWidgetManager appWidgetManager, int appWidgetId, boolean showToast) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String battleTag = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_battletag", null);
        String platform = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_platform", null);
        String region = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_region", null);

        RestOperation restOperation = new RestOperation(context, appWidgetManager, appWidgetId);
        restOperation.ShowToast = showToast;
        restOperation.execute(battleTag, platform, region);
    }

    // Overload - for when not to show toast
    public static void loadUserPref(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        loadUserPref(context, appWidgetManager, appWidgetId, false);
    }

    public static Profile loadUserPrefOffline(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String profileJson = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_profile", null);
        Gson gson = new Gson();
        return gson.fromJson(profileJson, Profile.class);
    }

    public static void deletePrefs(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_battletag");
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_platform");
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_region");
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_profile");
        prefs.apply();
    }

    public static Bitmap BuildTextBmp(String text, Context context)
    {
        Bitmap bitmap = Bitmap.createBitmap(160, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        Typeface futura = Typeface.createFromAsset(context.getAssets(),"futurano2d-demibold.ttf");
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(futura);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, 80, 60, paint);
        return bitmap;
    }
}
