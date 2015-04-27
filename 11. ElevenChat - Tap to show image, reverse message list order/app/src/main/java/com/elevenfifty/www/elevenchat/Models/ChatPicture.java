package com.elevenfifty.www.elevenchat.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by bkeck on 10/31/14.
 */
public class ChatPicture {
    private ChatUser fromUser;
    private ChatUser toUser;
    private String imageData;
    private String key;

    private ChatPicture() { }

    public ChatPicture(String from, String to, String imageString) {
        fromUser = new ChatUser(from);
        toUser = new ChatUser(to);
        imageData = imageString;
    }

    public ChatUser getFromUser() {
        return fromUser;
    }

    public ChatUser getToUser() {
        return toUser;
    }

    public String getImageData() {
        return imageData;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Bitmap createImage() {
        byte[] bytes = Base64.decode(imageData, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public Bitmap createThumbnail() {
        Bitmap image = createImage();
        int width = 64;
        int height = 64;
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        if (imageWidth > imageHeight) {
            height = (int)(height * (imageHeight / imageWidth));
        } else {
            width = (int)(width * (imageWidth / imageHeight));
        }
        return ThumbnailUtils.extractThumbnail(image, width, height);
    }

    public void cacheImage(Context context) {
        File cacheDir = context.getCacheDir();
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                Log.d("ChatPicture","failed to create directory");
            }
        }
        File cacheFile = new File(cacheDir.getPath() + "/" + key + ".jpg");
        if (!cacheFile.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(cacheFile);
                byte[] imageBytes = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT);
                fos.write(imageBytes);
                fos.close();
            } catch (Exception e) {
                Log.d("ChatPicture","cacheFile not written: " + e.getMessage());
            } finally {
                if (fos != null) {
                    fos = null;
                }
            }
        }
    }
}