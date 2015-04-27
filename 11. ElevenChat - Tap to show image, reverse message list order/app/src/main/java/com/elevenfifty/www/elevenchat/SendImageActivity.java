package com.elevenfifty.www.elevenchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.elevenfifty.www.elevenchat.Adapters.FriendListAdapter;
import com.elevenfifty.www.elevenchat.Models.ChatPicture;
import com.elevenfifty.www.elevenchat.Models.Friendship;
import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SendImageActivity extends Activity {
    private Firebase chatPictureRef;
    private FriendListAdapter friendListAdapter;

    private ArrayList<String> selectedFriends;

    private String timestamp;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);

        Intent intent = getIntent();
        timestamp = intent.getStringExtra("timestamp");

        username = this.getSharedPreferences("ChatPrefs", 0).getString("username", null);
        selectedFriends = new ArrayList<>();

        ListView friendList = (ListView)findViewById(R.id.friendList);

        Firebase friendRef = new Firebase(this.getResources().getString(R.string.firebase_url)).child("Friendships");
        friendListAdapter = new FriendListAdapter(friendRef.startAt(username).endAt(username), this);
        friendListAdapter.setSelectedFriends(selectedFriends);
        friendList.setAdapter(friendListAdapter);

        chatPictureRef = new Firebase(this.getResources().getString(R.string.firebase_url)).child("ChatPictures");

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friendship friendship = (Friendship) friendListAdapter.getItem(position);
                String userEmail = friendship.getTheFriend().getEmail();

                if (selectedFriends.contains(userEmail)) {
                    selectedFriends.remove(userEmail);
                } else {
                    selectedFriends.add(userEmail);
                }

                friendListAdapter.setSelectedFriends(selectedFriends);
            }
        });
    }

    public void sendImage(View view) {
        if (selectedFriends.size() > 0) {
            final ImageStringTask task = new ImageStringTask();
            final Context context = this;
            task.setTaskCompletionListener(new ImageStringTask.OnTaskCompletion() {
                @Override
                public void setMyTaskComplete() {
                    String imageString = task.imageString;
                    if (imageString.equals("empty")) {
                        new AlertDialog.Builder(context)
                                .setTitle("Error")
                                .setMessage("There was an error preparing your image, please try again")
                                .setPositiveButton(android.R.string.ok, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        for (String email : selectedFriends) {
                            ChatPicture chatPicture = new ChatPicture(username, email, imageString);
                            chatPictureRef.push().setValue(chatPicture, email);
                        }
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                        alertBuilder.setTitle("Pictures Sent!").setMessage("Your picture has been sent to your friend(s)");
                        alertBuilder.setPositiveButton("Great!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        AlertDialog dialog = alertBuilder.create();
                        dialog.show();
                    }
                }
            });
            task.execute(timestamp);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Select a Friend")
                    .setMessage("You must select at least one friend to sent the picture to")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}

class ImageStringTask extends AsyncTask<String, Void, String> {
    private OnTaskCompletion onTaskCompletion;
    public String imageString;

    public interface OnTaskCompletion {
        public void setMyTaskComplete();
    }

    public void setTaskCompletionListener(OnTaskCompletion onTaskCompletion) {
        this.onTaskCompletion = onTaskCompletion;
    }

    public ImageStringTask() { }

    @Override
    protected String doInBackground(String... params) {
        String imageTime = params[0];
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        File imageFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ imageTime + ".jpg");
        try {
            InputStream inputStream = new FileInputStream(imageFile);
            byte[] bytes;
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            bytes = output.toByteArray();
            imageString = Base64.encodeToString(bytes, Base64.DEFAULT);
            return imageString;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "empty";
        }
    }

    @Override
    protected void onPostExecute(String string) {
        onTaskCompletion.setMyTaskComplete();
    }
}
