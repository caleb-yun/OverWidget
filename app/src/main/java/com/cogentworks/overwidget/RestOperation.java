package com.cogentworks.overwidget;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

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

/**
 * Created by cyun on 8/6/17.
 */

public class RestOperation extends AsyncTask<String, Void, Profile> {

    @Override
    protected Profile doInBackground(String... params) {
        Profile result = null;
        String battleTag = params[0];
        if (battleTag != null) {
            result = new Profile();
            try {
                // Create URL
                URL endpoint = new URL("https://owapi.net/api/v3/u/" + battleTag.replace('#', '-') + "/stats"); //params[0].replace('#','-')

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
                            .getAsJsonObject().get("us")
                            .getAsJsonObject().get("stats")
                            .getAsJsonObject().get("competitive")
                            .getAsJsonObject().getAsJsonObject("overall_stats");
                    result.SetUser(battleTag, stats.get("avatar").getAsString());
                    Log.d("RestOperation", "SetUser");
                    result.SetLevel(stats.get("level").getAsString(), stats.get("prestige").getAsString(), stats.get("rank_image").getAsString());
                    Log.d("RestOperation", "SetLevel");
                    result.SetRank(stats.get("comprank").getAsString(), stats.get("tier").getAsString());
                    Log.d("RestOperation", "SetRank");

                    responseBody.close();
                    Log.d("RestOperation", "responseBody.close");

                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Profile result){
        super.onPostExecute(result);
    }
}