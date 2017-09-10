package com.cogentworks.overwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import layout.OverWidgetActivity;
import layout.OverWidgetActivityConfigureActivity;

/**
 * Created by cyun on 8/6/17.
 */

public class RestOperation extends AsyncTask<String, Void, Profile> {

    Context context;
    AppWidgetManager appWidgetManager;
    int appWidgetId;

    public RestOperation(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.appWidgetId = appWidgetId;
    }

    @Override
    protected Profile doInBackground(String... params) {
        Profile result = null;

        String battleTag = params[0];
        String platform = params[1];
        String region;
        if (platform != null) {
            if (platform.equals("PC")) {
                region = params[2]; // Set region on PC
            } else {
                region = "any"; // Set region to any on console
            }

            if (battleTag != null) {
                result = new Profile();
                try {
                    // Create URL
                    URL endpoint = new URL("https://owapi.net/api/v3/u/" + battleTag.replace('#', '-') + "/blob?platform=" + platform.toLowerCase());

                    // Create connection
                    HttpsURLConnection urlConnection = (HttpsURLConnection) endpoint.openConnection();

                    // Set methods and timeouts
                    urlConnection.setRequestMethod("GET");
                    //urlconnection.setReadTimeout(READ_TIMEOUT);
                    //urlconnection.setConnectTimeout(CONNECTION_TIMEOUT);

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

                        result.SetUser(battleTag, stats.get("avatar").getAsString());
                        Log.d("RestOperation", "SetUser");
                        result.SetLevel(stats.get("level").getAsString(), stats.get("prestige").getAsString(), stats.get("rank_image").getAsString());
                        Log.d("RestOperation", "SetLevel");
                        result.SetRank(stats.get("comprank").getAsString(), stats.get("tier").getAsString());
                        Log.d("RestOperation", "SetRank");

                        /*JsonObject heroStats = jsonParser.parse(new InputStreamReader(responseBody, "UTF-8"))
                                .getAsJsonObject().get(region.toLowerCase()) // Select Region
                                .getAsJsonObject().get("heroes")
                                .getAsJsonObject().get("playtime")
                                .getAsJsonObject().getAsJsonObject("quickplay");*/

                        //result.SetHero(heroStats.entrySet()[0].getKey());

                        responseBody.close();
                        Log.d("RestOperation", "responseBody.close");
                    } else {
                        // Other response code
                        Log.e("RestOperation", urlConnection.getResponseMessage());
                        result = null;
                    }
                    urlConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    result = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    result = null;
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Profile result){
        super.onPostExecute(result);

        if (result != null) {
            // Convert Profile to Gson and save to SharedPrefs
            SharedPreferences.Editor newPrefs = context.getSharedPreferences(OverWidgetActivityConfigureActivity.PREFS_NAME, 0).edit();
            Gson gson = new Gson();
            String profileJson = gson.toJson(result);
            newPrefs.putString(OverWidgetActivityConfigureActivity.PREF_PREFIX_KEY + appWidgetId + "_profile", profileJson);
            newPrefs.apply();
            OverWidgetActivity.setWidgetViews(context, result, this.appWidgetId, this.appWidgetManager);
        } else {
            OverWidgetActivity.setSyncClicked(context, this.appWidgetId, this.appWidgetManager);
        }
    }
}