package com.example.storymaker.help;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;


public class VideoApplication extends Application {

    private static VideoApplication instance;

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized VideoApplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
