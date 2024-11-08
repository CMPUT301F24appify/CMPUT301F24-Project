package com.example.appify;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Custom Application class to store global application state.
 * Currently, it holds the device's unique Android ID to be shared
 * across activities without passing it through each intent.
 */
public class MyApp extends Application {
    private String androidId;
    private String facilityID;
    private String facilityName;

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
    public void setAndroidId(String androidId) {
        this.androidId = androidId;
        retrieveFacilityID();
    }

    public String getFacilityName() { return facilityName; }

    private void retrieveFacilityID() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Android ID").document(androidId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    facilityID = documentSnapshot.getString("facilityID");
                    retrieveFacilityName();
                })
                .addOnFailureListener(e -> Log.w("MyApp", "Failed to retrieve facilityID", e));
    }

    private void retrieveFacilityName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("facilities").document(facilityID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    facilityName = documentSnapshot.getString("name");
                })
                .addOnFailureListener(e -> Log.w("MyApp", "Failed to retrieve facility name", e));
    }
}

