package com.elevenfifty.www.elevenchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.elevenfifty.www.elevenchat.Models.ChatUser;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;


public class LoginActivity extends Activity {
    private TextView emailField;
    private TextView passwordField;

    private Firebase firebase;
    private ProgressDialog authProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = (TextView)findViewById(R.id.email);
        passwordField = (TextView)findViewById(R.id.password);

        String firebaseUrl = getResources().getString(R.string.firebase_url);
        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase(firebaseUrl);

        authProgressDialog = new ProgressDialog(this);
        authProgressDialog.setTitle("Authenticating");
        authProgressDialog.setTitle("Authenticating with database...");
        authProgressDialog.setCancelable(false);
        authProgressDialog.show();

        firebase.addAuthStateListener(new AuthStateListener());
    }

    @Override
    public void onBackPressed() {
        authProgressDialog.dismiss();
        super.onBackPressed();
    }

    public void login(View view) {
        authProgressDialog.show();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        firebase.authWithPassword(email, password, new LoginAuthHandler());
    }

    public void signUp(View view) {
        authProgressDialog.show();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        firebase.createUser(email, password, new SignUpAuthHandler());
    }

    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
            String username = authData.getProviderData().get("email").toString();
            prefs.edit().putString("username",username).apply();
            Intent intent = new Intent(this, ChatPagerActivity.class);
            startActivity(intent);
            authProgressDialog.dismiss();
            finish();
        }
    }

    private void displayMessage(String title, String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private class AuthStateListener implements Firebase.AuthStateListener {
        @Override
        public void onAuthStateChanged(AuthData authData) {
            authProgressDialog.hide();
            setAuthenticatedUser(authData);
        }
    }

    private class LoginAuthHandler implements Firebase.AuthResultHandler {
        @Override
        public void onAuthenticated(AuthData authData) {
            authProgressDialog.hide();
            setAuthenticatedUser(authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            authProgressDialog.hide();
            switch (firebaseError.getCode()) {
                case FirebaseError.USER_DOES_NOT_EXIST:
                    displayMessage("Error", "User email does not exist");
                    break;
                case FirebaseError.INVALID_PASSWORD:
                    displayMessage("Error", "Password is incorrect");
                    break;
                default:
                    displayMessage("Error", "There was an error authenticating");
                    break;
            }
        }
    }

    private class SignUpAuthHandler implements Firebase.ResultHandler {
        @Override
        public void onSuccess() {
            authProgressDialog.hide();
            displayMessage("Success!", "Your account has been created, you may now log in");

            Firebase usersRef = firebase.child("ChatUsers");
            String email = emailField.getText().toString();
            String key = email.replace(".","_");
            usersRef.child(key).setValue(new ChatUser(email), email.toLowerCase());
        }

        @Override
        public void onError(FirebaseError firebaseError) {
            authProgressDialog.hide();
            switch (firebaseError.getCode()) {
                case FirebaseError.EMAIL_TAKEN:
                    displayMessage("Error", "User email already in system");
                    break;
                default:
                    displayMessage("Error", "There was an error creating your account");
                    break;
            }
        }
    }
}