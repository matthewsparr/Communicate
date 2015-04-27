package com.elevenfifty.www.elevenchat.Adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.elevenfifty.www.elevenchat.Models.Friendship;
import com.elevenfifty.www.elevenchat.R;
import com.firebase.client.Query;

import java.util.ArrayList;

/**
 * Created by bkeck on 11/1/14.
 */
public class FriendListAdapter extends FirebaseListAdapter<Friendship> {
    private ArrayList<String> selectedFriends;
    private boolean checkSelection;

    public FriendListAdapter(Query ref, Activity activity) {
        super(ref, Friendship.class, R.layout.chatuser_list_item, activity);
        checkSelection = false;
    }

    @Override
    protected void populateView(View v, Friendship friendship) {
        String otherUser = friendship.getTheFriend().getEmail();

        TextView emailLabel = (TextView)v.findViewById(R.id.userEmail);
        emailLabel.setText(otherUser);

        if (checkSelection) {
            ImageView checkmark = (ImageView)v.findViewById(R.id.checkmark);
            if (selectedFriends.contains(otherUser)) {
                checkmark.setVisibility(View.VISIBLE);
            } else {
                checkmark.setVisibility(View.INVISIBLE);
            }
        }
    }

    public ArrayList<String> getList() {
        ArrayList<String> friends = new ArrayList<>();
        for (Friendship friendship : models) {
            friends.add(friendship.getTheFriend().getEmail());
        }
        return friends;
    }

    public void setSelectedFriends(ArrayList<String> friends) {
        selectedFriends = friends;
        checkSelection = true;
        this.notifyDataSetChanged();
    }
}
