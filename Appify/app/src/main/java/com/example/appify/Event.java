package com.example.appify;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class Event {
    private String name;
    private String date;
    private String description;
    private int maxWishEntrants;
    private int maxSampleEntrants;
    private String posterUri;  // Store URI as String
    private boolean isGeolocate;
    private String eventId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private String organizerID;


    public Event(Context context,String  name, String date, String description, int maxWishEntrants, int maxSampleEntrants, Uri posterUri, boolean isGeolocate) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.maxWishEntrants = maxWishEntrants;
        this.maxSampleEntrants = maxSampleEntrants;
        this.posterUri = posterUri != null ? posterUri.toString() : null; // Convert URI to String
        this.isGeolocate = isGeolocate;
        this.eventId = UUID.randomUUID().toString();
        this.context = context;
        this.organizerID = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        System.out.println("Device ID: " + organizerID);

    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxWishEntrants() {
        return maxWishEntrants;
    }

    public int getMaxSampleEntrants() {
        return maxSampleEntrants;
    }

    public String getPosterUri() {
        return posterUri;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public String getEventId() {
        return eventId;
    }

    public boolean isGeolocate() {
        return isGeolocate;
    }

    // Override the toString() method to display the event name in the ListView
    @Override
    public String toString() {
        return name;
    }
}
