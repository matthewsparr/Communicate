package com.elevenfifty.www.elevenchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.elevenfifty.www.elevenchat.Models.ImageKeyEvent;

import java.io.File;

import de.greenrobot.event.EventBus;

public class ImageActivity extends Activity {
    private ImageView chatImage;

    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private final PointF start = new PointF();
    private final PointF mid = new PointF();
    private float oldDist = 1f;

    private RectF imageRect;
    private RectF viewRect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        chatImage = (ImageView)findViewById(R.id.chat_image);
        Intent intent = getIntent();
        String key = intent.getStringExtra("key");

        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new ImageKeyEvent(key));
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void checkBounds() {
        float[] values = new float[9];
        matrix.getValues(values);
        if (values[Matrix.MSCALE_X] < 0.5 || values[Matrix.MSCALE_Y] < 0.5) {
            values[Matrix.MSCALE_X] = 0.5f;
            values[Matrix.MSCALE_Y] = 0.5f;
        } else if (values[Matrix.MSCALE_X] > 10 || values[Matrix.MSCALE_Y] > 10) {
            values[Matrix.MSCALE_X] = 10;
            values[Matrix.MSCALE_Y] = 10;
        }

        float iWidth = values[Matrix.MSCALE_X] * imageRect.width();

        if (iWidth > viewRect.width()) {
            if (values[Matrix.MTRANS_X] > 0) {
                values[Matrix.MTRANS_X] = 0;
            } else if (values[Matrix.MTRANS_X] < viewRect.width() - iWidth) {
                values[Matrix.MTRANS_X] = viewRect.width() - iWidth;
            }
        } else {
            if (values[Matrix.MTRANS_X] < 0) {
                values[Matrix.MTRANS_X] = 0;
            } else if (values[Matrix.MTRANS_X] > viewRect.width() - iWidth) {
                values[Matrix.MTRANS_X] = viewRect.width() - iWidth;
            }
        }

        float iHeight = values[Matrix.MSCALE_Y] * imageRect.height();

        if (iHeight > viewRect.height()) {
            if (values[Matrix.MTRANS_Y] > 0) {
                values[Matrix.MTRANS_Y] = 0;
            } else if (values[Matrix.MTRANS_Y] < viewRect.height() - iHeight) {
                values[Matrix.MTRANS_Y] = viewRect.height() - iHeight;
            }
        } else {
            if (values[Matrix.MTRANS_Y] < 0) {
                values[Matrix.MTRANS_Y] = 0;
            } else if (values[Matrix.MTRANS_Y] > viewRect.height() - iHeight) {
                values[Matrix.MTRANS_Y] = viewRect.height() - iHeight;
            }
        }

        matrix.setValues(values);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void onEventBackgroundThread(ImageKeyEvent event) {
        File cacheDir = getCacheDir();
        File cacheFile = new File(cacheDir.getPath() + File.separator + event.key + ".jpg");
        Drawable image = Drawable.createFromPath(cacheFile.toString());
        chatImage.setImageDrawable(image);

        imageRect = new RectF(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        viewRect = new RectF(0, 0, size.x, size.y);
        matrix.setRectToRect(imageRect, viewRect, Matrix.ScaleToFit.CENTER);

        chatImage.setImageMatrix(matrix);

        chatImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                        break;
                }
                checkBounds();
                view.setImageMatrix(matrix);
                return true;
            }
        });
    }
}