package com.cogentworks.overwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by cyun on 8/6/17.
 */

public class SetLevelBmp extends AsyncTask<String, Void, Bitmap> {

    Context context;

    public SetLevelBmp(Context context) {
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap result = null;
        String url = params[0];
        Integer prestige = Integer.valueOf(params[1]);
        String level = params[2];

        if (url != null) {
            try {
                result = BuildLevelBmp(url, level, prestige, this.context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Bitmap result){
        super.onPostExecute(result);
    }

    public static Bitmap BuildLevelBmp(String url, String level, int prestige, Context context) throws IOException {
        URL border = new URL(url);
        URL rank = new URL(url.replace("Border", "Rank"));
        Bitmap borderBmp = BitmapFactory.decodeStream(border.openConnection().getInputStream());
        Bitmap levelBmp = BuildTextBmp(level, context);

        Bitmap bmOverlay = Bitmap.createBitmap(256, 256+8, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(borderBmp, new Matrix(), null);
        canvas.drawBitmap(levelBmp, 0, 90, null);

        if(prestige > 0) {
            Bitmap rankBmp = BitmapFactory.decodeStream(rank.openConnection().getInputStream());
            canvas.drawBitmap(rankBmp, 0, bmOverlay.getHeight() - 128, null);
        }

        return bmOverlay;
    }

    public static Bitmap BuildTextBmp(String text, Context context)
    {
        Bitmap bitmap = Bitmap.createBitmap(256, 70, Bitmap.Config.ARGB_8888);
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

        canvas.drawText(text, bitmap.getWidth()/2, bitmap.getHeight()-15, paint);
        return bitmap;
    }
}