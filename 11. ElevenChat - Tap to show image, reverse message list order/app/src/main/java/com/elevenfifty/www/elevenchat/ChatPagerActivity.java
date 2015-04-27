package com.elevenfifty.www.elevenchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;

public class ChatPagerActivity extends Activity {
    private int lastPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_chat_pager);

        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        ChatPagerAdapter pagerAdapter = new ChatPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        viewPager.setCurrentItem(1);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                if (i == 1 && i2 == 0) {
                    if (lastPage != 1) {
                        lastPage = 1;
                        Intent intent = new Intent("StartStopCameraPreview");
                        intent.putExtra("message", "StartPreview");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }
                }
            }

            @Override
            public void onPageSelected(int i) {
                if (i != 1) {
                    if (lastPage == 1) {
                        lastPage = i;
                        Intent intent = new Intent("StartStopCameraPreview");
                        intent.putExtra("message", "StopPreview");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout(View view) {
        String firebaseUrl = getResources().getString(R.string.firebase_url);
        Firebase firebase = new Firebase(firebaseUrl);
        firebase.unauth();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    public static class ChatPagerAdapter extends PagerAdapter {
        final LayoutInflater inflater;
        final Context context;

        public ChatPagerAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = new View(context);
            switch (position) {
                case 0:
                    view = inflater.inflate(R.layout.message_page, null);
                    break;
                case 1:
                    view = inflater.inflate(R.layout.camera_page, null);
                    break;
                case 2:
                    view = inflater.inflate(R.layout.friend_list_page, null);
                    break;
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
