package com.example.appify;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Custom Application class to store global application state.
 * Currently, it holds the device's unique Android ID to be shared
 * across activities without passing it through each intent.
 */
public class MyApp extends Application {
    private String androidId;

    /**
     * Retrieves the Android ID, a unique identifier for the device.
     *
     * @return The unique Android ID as a String.
     */
    public String getAndroidId() {
        return androidId;
    }

    /**
     * Sets the Android ID for the application, allowing access from
     * multiple activities throughout the app.
     *
     * @param androidId The unique Android ID to set for the application.
     */
    public void setAndroidId(String androidId) { this.androidId = androidId; }
}

