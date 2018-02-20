package layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.cogentworks.overwidget.R;
import com.cogentworks.overwidget.RestOperation;
import com.cogentworks.overwidget.WidgetUtils;
import com.cogentworks.overwidget.WidgetPrefFragment;

/**
 * The configuration screen for the {@link OverWidgetProvider OverWidgetProvider} AppWidget.
 */

public class OverWidgetConfigure extends AppCompatActivity implements OnPreferenceChangeListener {

    private static final String TAG = "OverWidgetConfigure";
    public static final String PREFS_NAME = "layout.OverWidgetProvider";
    public static final String PREF_PREFIX_KEY = "overwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    ProgressBar progressBar;
    LinearLayout mainContent;
    FloatingActionButton fab;

    public OverWidgetConfigure() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_configure);
        mainContent = findViewById(R.id.layout_main);
        progressBar = findViewById(R.id.progress_bar);
        fab = findViewById(R.id.fab);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //sp.edit().clear().commit();
        Log.d(TAG, "setDefaultValues");

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Invalid appwidget ID
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary(newValue.toString());
        return true;
    }

    public void onFabClick(View view) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //Snackbar.make(view, sp.getString("username", "None Selected"), Snackbar.LENGTH_SHORT).show();

        //mainContent.setVisibility(View.INVISIBLE);
        //progressBar.setVisibility(View.VISIBLE);
        fab.hide();
        crossfade(this, progressBar, mainContent);

        final Context context = OverWidgetConfigure.this;

        // When the button is clicked, store the string locally
        String battleTag = sp.getString("username", "None Selected");
        String platform = sp.getString("platform", "None Selected");
        String region = sp.getString("region", "None Selected");
        WidgetUtils.savePrefs(context, mAppWidgetId, battleTag, platform, region);

        // Check if user exists
        RestOperation restOperation = new RestOperation(context, mAppWidgetId);
        restOperation.execute(battleTag, platform, region);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sp.edit().clear().apply();
    }

    public static void crossfade(Context context, View viewIn, View viewOut) {
        int mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        viewIn.setAlpha(0f);
        viewIn.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        viewIn.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        final View mViewOut = viewOut;
        viewOut.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mViewOut.setVisibility(View.GONE);
                    }
                });
    }

    /*public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference battletag = (EditTextPreference) pref;
            pref.setSummary(battletag.getText());
        }*/

    /*@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_configure);

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

        //mAppWidgetText.setText(loadUserPref(OverWidgetConfigure.this, mAppWidgetId));
    }*/
}