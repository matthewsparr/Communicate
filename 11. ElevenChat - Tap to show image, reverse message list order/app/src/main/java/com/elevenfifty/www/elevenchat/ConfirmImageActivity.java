package com.elevenfifty.www.elevenchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ConfirmImageActivity extends Activity {
    private String timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_image);
        ImageView confirmImage = (ImageView) findViewById(R.id.confirmImage);

        Intent intent = getIntent();
        timestamp = intent.getStringExtra("timestamp");
        LoadImageTask task = new LoadImageTask(confirmImage);
        task.execute("IMG_"+ timestamp + ".jpg");
    }

    public void retakeImage(View view) {
        finish();
    }

    public void sendImage(View view) {
        Intent intent = new Intent(this, SendImageActivity.class);
        intent.putExtra("timestamp", timestamp);
        startActivity(intent);
        finish();
    }
}