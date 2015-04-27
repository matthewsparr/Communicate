package com.elevenfifty.www.elevenchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.elevenfifty.www.elevenchat.Adapters.FriendListAdapter;
import com.elevenfifty.www.elevenchat.Adapters.UserListAdapter;
import com.elevenfifty.www.elevenchat.Models.Friendship;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by bkeck on 10/31/14.
 */
public class FriendView extends RelativeLayout {
    private SearchView searchBar;
    private ListView friendList;

    private Firebase friendRef;
    private Firebase usersRef;

    private FriendListAdapter friendListAdapter;
    private UserListAdapter userListAdapter;

    private static String username;
    private Activity thisActivity;
    private boolean searching;

    public FriendView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        searchBar = (SearchView)findViewById(R.id.searchView);
        friendList = (ListView)findViewById(R.id.friendList);

        username = getContext().getSharedPreferences("ChatPrefs", 0).getString("username", null);

        friendRef = new Firebase(this.getResources().getString(R.string.firebase_url)).child("Friendships");
        usersRef = new Firebase(getContext().getResources().getString(R.string.firebase_url)).child("ChatUsers");

        thisActivity = (Activity)getContext();

        friendListAdapter = new FriendListAdapter(friendRef.startAt(username).endAt(username), thisActivity);
        friendList.setAdapter(friendListAdapter);

        setupSearchBar();

        searching = false;
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (searching) {
                    TextView emailField = (TextView) view.findViewById(R.id.userEmail);
                    String userEmail = emailField.getText().toString();
                    String key = username.replace(".", "_") + "__" + userEmail.replace(".", "_");
                    friendRef.child(key).setValue(new Friendship(username, userEmail), username.toLowerCase());

                    ImageView checkmark = (ImageView)view.findViewById(R.id.checkmark);
                    checkmark.setVisibility(View.VISIBLE);
                }
            }
        });

        friendList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                if (!searching) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                    alertBuilder.setTitle("Remove Friendship?").setMessage("Remove this user as a friend?");
                    alertBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TextView emailField = (TextView) view.findViewById(R.id.userEmail);
                            String userEmail = emailField.getText().toString();
                            String key = username.replace(".", "_") + "__" + userEmail.replace(".", "_");
                            friendRef.child(key).removeValue();
                        }
                    });
                    alertBuilder.setNegativeButton("Cancel", null);
                    AlertDialog dialog = alertBuilder.create();
                    dialog.show();
                }
                return true;
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        if (userListAdapter != null) {
            userListAdapter.cleanup();
        }
        if (friendListAdapter != null) {
            friendListAdapter.cleanup();
        }
        super.onDetachedFromWindow();
    }

    private UserListAdapter getUserAdapter(String text) {
        if (text.equals("")) {
            return new UserListAdapter(usersRef.limit(50), thisActivity, friendListAdapter.getList());
        } else {
            text = text.toLowerCase();
            return new UserListAdapter(usersRef.startAt(text).endAt(text+"~"), thisActivity, friendListAdapter.getList());
        }
    }

    private void setupSearchBar() {
        searchBar.setOnSearchClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searching = true;
                userListAdapter = getUserAdapter("");
                friendList.setAdapter(userListAdapter);
            }
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searching = true;
                userListAdapter = getUserAdapter(query);
                friendList.setAdapter(userListAdapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searching = true;
                userListAdapter = getUserAdapter(newText);
                friendList.setAdapter(userListAdapter);
                return false;
            }
        });

        searchBar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searching = false;
                friendList.setAdapter(friendListAdapter);
                return false;
            }
        });
    }
}