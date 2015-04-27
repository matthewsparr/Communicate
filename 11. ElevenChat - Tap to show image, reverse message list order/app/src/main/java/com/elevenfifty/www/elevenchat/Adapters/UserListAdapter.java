package com.elevenfifty.www.elevenchat.Adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.elevenfifty.www.elevenchat.Models.ChatUser;
import com.elevenfifty.www.elevenchat.R;
import com.firebase.client.Query;

import java.util.ArrayList;

/**
 * Created by bkeck on 11/3/14.
 */
public class UserListAdapter extends FirebaseListAdapter<ChatUser> {
    private final ArrayList<String> friends;

    public UserListAdapter(Query ref, Activity activity, ArrayList<String> friends) {
        super(ref, ChatUser.class, R.layout.chatuser_list_item, activity);
        this.friends = friends;
    }

    @Override
    protected void populateView(View v, ChatUser user) {
        String userEmail = user.getEmail();

        TextView emailLabel = (TextView)v.findViewById(R.id.userEmail);
        emailLabel.setText(userEmail);

        ImageView checkbox = (ImageView)v.findViewById(R.id.checkmark);
        if (friends.contains(userEmail)) {
            checkbox.setVisibility(View.VISIBLE);
        } else {
            checkbox.setVisibility(View.INVISIBLE);
        }
    }
}
