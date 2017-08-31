package layout;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cogentworks.overwidget.Profile;
import com.cogentworks.overwidget.R;
import com.cogentworks.overwidget.SetLevelBmp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverWidgetActivityConfigureActivity OverWidgetActivityConfigureActivity}
 */
public class OverWidgetActivity extends AppWidgetProvider {

    private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";

    public static void setWidgetViews(Context context, Profile profile, int appWidgetId, AppWidgetManager appWidgetManager) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);

        views.setTextViewText(R.id.appwidget_battletag, profile.BattleTag);

        // Comp Rank
        /*views.setImageViewBitmap(R.id.appwidget_comprank, BuildTextBmp(profile.CompRank, context));
        views.setImageViewResource(R.id.appwidget_tier, context.getResources().getIdentifier(profile.Tier, "drawable", context.getPackageName()));*/

        // Level
        SetLevelBmp setLevelBmp = new SetLevelBmp(context);
        try {
            views.setImageViewBitmap(R.id.appwidget_level, setLevelBmp.execute(profile.RankImageURL, profile.Prestige, profile.Level).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Tap to refresh
        //OverWidgetActivity.setSyncClicked(context, appWidgetId);
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

    public static void setSyncClicked(Context context, int appWidgetId, AppWidgetManager appWidgetManager) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);
        views.setOnClickPendingIntent(R.id.appwidget_layout, getPendingSelfIntent(context, SYNC_CLICKED, appWidgetId));
        appWidgetManager.updateAppWidget(appWidgetId, views);
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
                OverWidgetActivityConfigureActivity.loadUserPref(context, appWidgetManager, appWidgetId);
                Toast.makeText(context, "Refreshed", Toast.LENGTH_SHORT).show();
                Log.d("OverWidgetActivity", "Refreshed");
            }
        }
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
        paint.setTextSize(55);
        paint.setTextAlign(Paint.Align.CENTER);

        Paint paint2 = new Paint();
        paint2.setColor(Color.RED);
        paint2.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint2);

        canvas.drawText(text, 80, 60, paint);
        return bitmap;
    }
}

