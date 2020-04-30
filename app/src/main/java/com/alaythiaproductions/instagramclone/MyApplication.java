package com.alaythiaproductions.instagramclone;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Firebase Offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
