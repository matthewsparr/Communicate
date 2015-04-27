package com.elevenfifty.www.elevenchat.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.elevenfifty.www.elevenchat.Models.ChatPicture;
import com.elevenfifty.www.elevenchat.R;
import com.firebase.client.Query;

/**
 * Created by bkeck on 11/4/14.
 */
public class MessageListAdapter extends FirebaseListAdapter<ChatPicture> {
    private final LayoutInflater inflater;

    public MessageListAdapter(Query ref, Activity activity) {
        super(ref, ChatPicture.class, R.layout.message_list_item, activity);
        inflater = activity.getLayoutInflater();
    }

    @Override
    protected void populateView(View v, ChatPicture chatPicture) {
        ImageView thumbnail = (ImageView)v.findViewById(R.id.chatThumbnail);
        TextView userEmail = (TextView)v.findViewById(R.id.userEmail);

        userEmail.setText("from " + chatPicture.getFromUser().getEmail());
        thumbnail.setImageBitmap(chatPicture.createThumbnail());
    }

    @Override
    public Object getItem(int i) {
        return models.get(models.size() - i - 1);
    }

    @Override
    public long getItemId(int i) {
        return models.size() - i - 1;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.message_list_item, viewGroup, false);
        }

        ChatPicture model =  models.get(models.size() - i - 1);
        // Call out to subclass to marshall this model into the provided view
        populateView(view, model);
        return view;
    }
}
