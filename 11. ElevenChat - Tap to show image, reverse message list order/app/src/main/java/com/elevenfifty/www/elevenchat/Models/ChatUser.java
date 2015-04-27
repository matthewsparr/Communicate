package com.elevenfifty.www.elevenchat.Models;

import com.firebase.client.AuthData;

/**
 * Created by bkeck on 10/31/14.
 */
public class ChatUser {
    private String email;

    private ChatUser() { }

    public ChatUser(AuthData authData) {
        email = authData.getProviderData().get("Email").toString();
    }

    public ChatUser(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
