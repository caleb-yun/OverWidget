package com.cogentworks.overwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import layout.OverWidgetActivity;
import layout.OverWidgetActivityConfigureActivity;

import static android.app.Activity.RESULT_OK;

/**
 * Created by cyun on 8/25/17.
 */

public class CheckProfileExists extends AsyncTask<String, Void, Profile> {

    private Activity mActivity;
    private Context context;
    private int appWidgetId;

    private String battleTag;
    private String platform;
    private String region;

    public CheckProfileExists(Context context, int appWidgetId) {
        this.context = context;
        this.appWidgetId = appWidgetId;
        this.mActivity = (Activity) context;
    }

    @Override
    protected Profile doInBackground(String... params) {
        Profile result = null;
        this.battleTag = params[0];
        this.platform = params[1];
        if (platform != null) {
            if (platform.equals("PC")) {
                region = params[2]; // Set region on PC
            } else {
                region = "any"; // Set region to any on console
            }

            if (battleTag != null) {
                try {
                    // Create URL
                    URL endpoint = new URL("https://owapi.net/api/v3/u/" + battleTag.replace('#', '-') + "/stats?platform=" + platform.toLowerCase());

                    // Create connection
                    HttpsURLConnection urlConnection = (HttpsURLConnection) endpoint.openConnection();

                    // Set methods and timeouts
                    urlConnection.setRequestMethod("GET");

                    // Connect to URL
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
                        if (stats != null) {
                            result = new Profile();
                            result.SetUser(battleTag, stats.get("avatar").getAsString());
                            Log.d("RestOperation", "SetUser");
                            result.SetLevel(stats.get("level").getAsString(), stats.get("prestige").getAsString(), stats.get("rank_image").getAsString());
                            Log.d("RestOperation", "SetLevel");
                            result.SetRank(stats.get("comprank").getAsString(), stats.get("tier").getAsString());
                            Log.d("RestOperation", "SetRank");
                        } else {
                            return null;
                        }
                        responseBody.close();
                        Log.d("CheckProfileExists", "responseBody.close");
                    } else {
                        // Other response code
                        Log.e("CheckProfileExists", urlConnection.getResponseMessage());
                    }
                    urlConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Profile result) {
        super.onPostExecute(result);

        ProgressBar progressBar = mActivity.findViewById(R.id.progress_bar);

        // Profile Exists
        if (result != null) {
            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            OverWidgetActivity.setWidgetViews(context, result, this.appWidgetId, appWidgetManager);

            // Convert Profile to Gson and save to SharedPrefs
            SharedPreferences.Editor newPrefs = context.getSharedPreferences(OverWidgetActivityConfigureActivity.PREFS_NAME, 0).edit();
            Gson gson = new Gson();
            String profileJson = gson.toJson(result);
            newPrefs.putString(OverWidgetActivityConfigureActivity.PREF_PREFIX_KEY + appWidgetId + "_profile", profileJson);
            newPrefs.apply();
            OverWidgetActivity.setWidgetViews(context, result, this.appWidgetId, appWidgetManager);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            mActivity.setResult(RESULT_OK, resultValue);
            mActivity.finish();
        } else if (result == null) {
            Toast.makeText(context, "Player not found", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        } else {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }
}
