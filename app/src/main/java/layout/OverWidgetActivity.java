package layout;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cogentworks.overwidget.Profile;
import com.cogentworks.overwidget.R;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverWidgetActivityConfigureActivity OverWidgetActivityConfigureActivity}
 */
public class OverWidgetActivity extends AppWidgetProvider {

    private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Profile profile = OverWidgetActivityConfigureActivity.loadUserPref(context, appWidgetManager, appWidgetId);
    }

    public static void setWidgetViews(Context context, Profile profile, int appWidgetId, AppWidgetManager appWidgetManager) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);
        views.setTextViewText(R.id.appwidget_battletag, profile.BattleTag);
        //views.setTextViewText(R.id.appwidget_comprank, compRank);
        views.setImageViewBitmap(R.id.appwidget_comprank, buildUpdate(profile.CompRank, context));
        views.setImageViewResource(R.id.appwidget_tier, context.getResources().getIdentifier(profile.Tier, "drawable", context.getPackageName()));

        views.setOnClickPendingIntent(R.id.appwidget_layout, getPendingSelfIntent(context, SYNC_CLICKED, appWidgetId));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("OverWidgetActivity", "OnUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // Tell the AppWidgetManager to perform an update on the current App Widget
            OverWidgetActivityConfigureActivity.loadUserPref(context, appWidgetManager, appWidgetId);
        }
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action, int appWidgetId) {
        Intent intent = new Intent(context, OverWidgetActivity.class);
        intent.setAction(action);
        intent.putExtra("WIDGET_ID", appWidgetId);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            OverWidgetActivityConfigureActivity.deleteTitlePref(context, appWidgetId);
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
        super.onReceive(context, intent);
        Log.d("OverWidgetActivity", "OnReceive");

        if (SYNC_CLICKED.equals(intent.getAction())) {
            Log.d("OverWidgetActivity", "Refreshing");
            Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Bundle extras = intent.getExtras();

            if (extras != null) {
                int appWidgetId = (int) extras.get("WIDGET_ID");
                this.updateAppWidget(context, appWidgetManager, appWidgetId);
                Toast.makeText(context, "Refreshed", Toast.LENGTH_SHORT).show();
                Log.d("OverWidgetActivity", "Refreshed");
            }
        }
    }

    public static Bitmap buildUpdate(String text, Context context)
    {
        Bitmap myBitmap = Bitmap.createBitmap(160, 80, Bitmap.Config.ARGB_4444);
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        Typeface clock = Typeface.createFromAsset(context.getAssets(),"futurano2d-demibold.ttf");
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(clock);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(55);
        paint.setTextAlign(Paint.Align.CENTER);
        myCanvas.drawText(text, 80, 60, paint);
        return myBitmap;
    }
}

