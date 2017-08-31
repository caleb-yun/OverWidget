package layout;

import android.annotation.TargetApi;
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
import android.os.Build;
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

    private static final String TAG = "OverWidgetActivity";

    public static void setWidgetViews(Context context, Profile profile, int appWidgetId, AppWidgetManager appWidgetManager) {
        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // Get min width and height.
        int columns = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
        int rows = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));

        // Construct the RemoteViews object
        RemoteViews views = null;
        if (columns == 1) {

            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity_2);

            // Level
            SetLevelBmp setLevelBmp = new SetLevelBmp(context);
            try {
                views.setImageViewBitmap(R.id.appwidget_level, setLevelBmp.execute(profile.RankImageURL, profile.Prestige, profile.Level).get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Populate general views
        views.setTextViewText(R.id.appwidget_battletag, profile.BattleTag);
        // Comp Rank
        views.setImageViewBitmap(R.id.appwidget_comprank, BuildTextBmp(profile.CompRank, context));
        views.setImageViewResource(R.id.appwidget_tier, context.getResources().getIdentifier(profile.Tier, "drawable", context.getPackageName()));

        // Tap to refresh
        views.setOnClickPendingIntent(R.id.appwidget_layout, getPendingSelfIntent(context, SYNC_CLICKED, appWidgetId));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.d(TAG, "SetWidgetViews");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "OnUpdate");
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
        Log.d(TAG, "OnReceive");

        if (SYNC_CLICKED.equals(intent.getAction())) {
            Log.d(TAG, "Refreshing");
            Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Bundle extras = intent.getExtras();

            if (extras != null) {
                int appWidgetId = (int) extras.get("WIDGET_ID");
                OverWidgetActivityConfigureActivity.loadUserPref(context, appWidgetManager, appWidgetId);
                Toast.makeText(context, "Refreshed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Refreshed");
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
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, 80, 60, paint);
        return bitmap;
    }

    //region onAppWidget OptionsChanged

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        Log.d(TAG, "Changed dimensions");

        // Update widget
        Profile profile = OverWidgetActivityConfigureActivity.loadUserPrefOffline(context, appWidgetManager, appWidgetId);
        OverWidgetActivity.setWidgetViews(context, profile, appWidgetId, appWidgetManager);

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    // Determine appropriate view based on width provided.
    private RemoteViews getRemoteViews(Context context, int minWidth, int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);

        RemoteViews views = null;

        if (columns == 2) {
            // Get 4 column widget remote view and return
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);
        } else {
            // Get appropriate remote view.
            views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);
        }

        return views;
    }

    // Returns number of cells needed for given size of the widget.
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    //endregion
}

