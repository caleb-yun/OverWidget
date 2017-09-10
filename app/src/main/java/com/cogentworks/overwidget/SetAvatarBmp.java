package com.cogentworks.overwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

/**
 * Created by cyun on 8/6/17.
 */

public class SetAvatarBmp extends AsyncTask<String, Void, Bitmap> {

    Context context;

    public SetAvatarBmp(Context context) {
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap result = null;
        String url = params[0];

        if (url != null) {
            try {
                result = BuildAvatarBmp(this.context, url);
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

    private static Bitmap BuildAvatarBmp(Context context, String url) throws IOException {
        URL avatarUrl = new URL(url);
        Bitmap avatarBmp = BitmapFactory.decodeStream(avatarUrl.openConnection().getInputStream());

        //Bitmap bmOverlay = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        //Canvas canvas = new Canvas(bmOverlay);
        //canvas.drawBitmap(avatarBmp, 0, 0, null);

        return avatarBmp;
    }
}