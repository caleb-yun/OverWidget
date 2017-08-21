package layout;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.cogentworks.overwidget.R;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverWidgetActivityConfigureActivity OverWidgetActivityConfigureActivity}
 */
public class OverWidgetActivity extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        String battleTag = OverWidgetActivityConfigureActivity.loadUserPref(context, appWidgetId).BattleTag;
        String compRank = OverWidgetActivityConfigureActivity.loadUserPref(context, appWidgetId).CompRank;
        String tier = OverWidgetActivityConfigureActivity.loadUserPref(context, appWidgetId).Tier;

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.over_widget_activity);
        views.setTextViewText(R.id.appwidget_battletag, battleTag);
        views.setTextViewText(R.id.appwidget_comprank, compRank);
        //views.setTextViewText(R.id.appwidget_tier, tier);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
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
}

