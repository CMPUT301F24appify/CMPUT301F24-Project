package com.example.appify;

import android.app.Application;

public class MyApp extends Application {
    private String androidId;

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }
}
