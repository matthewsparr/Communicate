package com.elevenfifty.www.elevenchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.elevenfifty.www.elevenchat.Adapters.MessageListAdapter;
import com.elevenfifty.www.elevenchat.Models.ChatPicture;
import com.elevenfifty.www.elevenchat.Models.ChatPictureEvent;
import com.firebase.client.Firebase;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by bkeck on 11/4/14.
 */
public class MessageView extends RelativeLayout {
    private ListView messageList;

    private MessageListAdapter messageListAdapter;

    private boolean longtap;
    private ImageView fullImage;

    private AdapterView.OnItemClickListener onItemClickListener;

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        longtap = false;
        messageList = (ListView)findViewById(R.id.messageList);
        fullImage = (ImageView)findViewById(R.id.fullImage);

        String username = getContext().getSharedPreferences("ChatPrefs", 0).getString("username", null);
        Activity thisActivity = (Activity)getContext();

        Firebase messageRef = new Firebase(getResources().getString(R.string.firebase_url)).child("ChatPictures");
        messageListAdapter = new MessageListAdapter(messageRef.startAt(username).endAt(username),thisActivity);
        messageList.setAdapter(messageListAdapter);

        EventBus.getDefault().register(this);

        onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatPicture chatPicture = (ChatPicture)messageListAdapter.getItem(position);
                chatPicture.cacheImage(getContext());

                Intent intent = new Intent(getContext(), ImageActivity.class);
                intent.putExtra("key", chatPicture.getKey());
                getContext().startActivity(intent);
            }
        };

        messageList.setOnItemClickListener(onItemClickListener);

        messageList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longtap = true;
                messageList.setOnItemClickListener(null);
                ChatPicture chatPicture = (ChatPicture)messageListAdapter.getItem(position);

                fullImage.setImageDrawable(null);
                fullImage.setVisibility(View.VISIBLE);
                EventBus.getDefault().post(new ChatPictureEvent(chatPicture));

                return true;
            }
        });

        messageList.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (longtap) {
                            fullImage.setVisibility(View.GONE);
                            longtap = false;
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    messageList.setOnItemClickListener(onItemClickListener);
                                }
                            }, 50);
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        if (messageListAdapter != null) {
            messageListAdapter.cleanup();
        }
        EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }

    public void onEventBackgroundThread(ChatPictureEvent event) {
        ChatPicture chatPicture = event.chatPicture;
        File cacheDir = getContext().getCacheDir();
        File cacheFile = new File(cacheDir.getPath() + File.separator + chatPicture.getKey() + ".jpg");
        if (cacheFile.exists()) {
            Drawable image = Drawable.createFromPath(cacheFile.toString());
            fullImage.setImageDrawable(image);
        } else {
            fullImage.setImageBitmap(chatPicture.createImage());
        }
        fullImage.setVisibility(View.VISIBLE);
    }
}
