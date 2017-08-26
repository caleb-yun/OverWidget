package layout;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.cogentworks.overwidget.Profile;
import com.cogentworks.overwidget.R;
import com.cogentworks.overwidget.RestOperation;


import java.util.concurrent.ExecutionException;

/**
 * The configuration screen for the {@link OverWidgetActivity OverWidgetActivity} AppWidget.
 */
public class OverWidgetActivityConfigureActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "layout.OverWidgetActivity";
    private static final String PREF_PREFIX_KEY = "overwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    EditText mAppWidgetText;
    ProgressBar progressBar;
    Spinner platformSpinner;
    Spinner regionSpinner;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            progressBar.setVisibility(View.VISIBLE);

            final Context context = OverWidgetActivityConfigureActivity.this;

            // When the button is clicked, store the string locally
            String battleTag = mAppWidgetText.getText().toString();
            String platform = platformSpinner.getSelectedItem().toString();
            String region = regionSpinner.getSelectedItem().toString();
            savePrefs(context, mAppWidgetId, battleTag, platform, region);

            // Check if user exists
            Profile result = new Profile();
            try {
                RestOperation restOperation = new RestOperation();
                result = restOperation.execute(battleTag, platform, region).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                result = null;
            } catch (ExecutionException e) {
                e.printStackTrace();
                result = null;
            }

            // Profile Exists
            if (result != null) {
                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                OverWidgetActivity.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            } else if (result == null) {
                Toast.makeText(context, "Player not found", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    public OverWidgetActivityConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void savePrefs(Context context, int appWidgetId, String battleTag, String platform, String region) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_battletag", battleTag);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_platform", platform);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_region", region);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static Profile loadUserPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String battleTag = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_battletag", null);
        String platform = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_platform", null);
        String region = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_region", null);
        Profile result = new Profile();
        try {
            RestOperation restOperation = new RestOperation();
            result = restOperation.execute(battleTag, platform, region).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.over_widget_activity_configure);

        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        platformSpinner = (Spinner) findViewById(R.id.spinner_platform);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> platformAdapter = ArrayAdapter.createFromResource(this, R.array.platform_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        platformSpinner.setAdapter(platformAdapter);

        regionSpinner = (Spinner) findViewById(R.id.spinner_region);
        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this, R.array.region_array, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        //mAppWidgetText.setText(loadUserPref(OverWidgetActivityConfigureActivity.this, mAppWidgetId));
    }
}