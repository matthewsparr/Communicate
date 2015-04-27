package com.elevenfifty.www.elevenchat;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by bkeck on 1/16/15.
 */
public class LoadImageTask extends AsyncTask<String, Void, Drawable> {
    private final WeakReference<ImageView> imageViewReference;

    public LoadImageTask(ImageView imageView) {
        imageViewReference = new WeakReference<>(imageView);
    }

    @Override
    protected Drawable doInBackground(String... params) {
        String imageName = params[0];
        File mediaStorageDir;
        if (params.length == 1) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
        } else {
            mediaStorageDir = new File(params[1]);
        }
        File imageFile = new File(mediaStorageDir.getPath() + File.separator + imageName);
        return Drawable.createFromPath(imageFile.toString());
    }

    @Override
    protected void onPostExecute(Drawable image) {
        if (imageViewReference != null && image != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageDrawable(image);
            }
        }
    }
}
