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
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by cyun on 8/6/17.
 */

public class SetLevelBmp extends AsyncTask<String, Void, Bitmap> {

    Context context;
    RemoteViews views;
    AppWidgetManager appWidgetManager;
    int appWidgetId;
    Profile profile;

    public SetLevelBmp(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews views, Profile profile) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.appWidgetId = appWidgetId;
        this.views = views;
        this.profile = profile;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap result = null;

        if (profile.PrestigeImgURL != null) {
            try {
                result = BuildLevelBmp(profile, this.context);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Bitmap result)
    {
        super.onPostExecute(result);

        views.setImageViewBitmap(R.id.appwidget_level, result);
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
    }

    public static Bitmap BuildLevelBmp(Profile profile, Context context) throws IOException {
        URL borderUrl = new URL(profile.LevelImgURL);
        URL prestigeUrl = null;
        try {
            prestigeUrl = new URL(profile.PrestigeImgURL);
        } catch (IOException e) {}

        InputStream borderInputStream = borderUrl.openConnection().getInputStream();
        Bitmap borderBmp = BitmapFactory.decodeStream(borderInputStream);
        Bitmap levelBmp = BuildTextBmp(profile.Level, profile.getTheme(), context);

        Bitmap bmOverlay = Bitmap.createBitmap(256, 256+8, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(borderBmp, new Matrix(), null);
        canvas.drawBitmap(levelBmp, 0, 90, null);

        if (prestigeUrl != null) {
            InputStream rankInputStream = prestigeUrl.openConnection().getInputStream();
            Bitmap rankBmp = BitmapFactory.decodeStream(rankInputStream);
            canvas.drawBitmap(rankBmp, 0, bmOverlay.getHeight() - 128, null);

            rankInputStream.close();
        }

        borderInputStream.close();
        return bmOverlay;
    }

    public static Bitmap BuildTextBmp(String text, String theme, Context context)
    {
        Bitmap bitmap = Bitmap.createBitmap(256, 70, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        Typeface futura = Typeface.createFromAsset(context.getAssets(),"futurano2d-demibold.ttf");
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(futura);
        paint.setStyle(Paint.Style.FILL);
        if (theme.equals("Light"))
            paint.setColor(Color.BLACK);
        else
            paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, bitmap.getWidth()/2, bitmap.getHeight()-15, paint);
        return bitmap;
    }
}