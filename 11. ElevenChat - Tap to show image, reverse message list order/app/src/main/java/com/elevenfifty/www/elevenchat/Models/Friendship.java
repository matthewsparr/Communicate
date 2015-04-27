package com.elevenfifty.www.elevenchat.Models;

import com.elevenfifty.www.elevenchat.Models.ChatUser;

/**
 * Created by bkeck on 10/31/14.
 */
public class Friendship {
    private ChatUser currentUser;
    private ChatUser theFriend;

    private Friendship() { }

    public Friendship(ChatUser current, ChatUser friend) {
        this.currentUser = current;
        this.theFriend = friend;
    }

    public Friendship(String current, String friend) {
        this.currentUser = new ChatUser(current);
        this.theFriend = new ChatUser(friend);
    }

    public ChatUser getCurrentUser() {
        return currentUser;
    }

    public ChatUser getTheFriend() {
        return theFriend;
    }
}
