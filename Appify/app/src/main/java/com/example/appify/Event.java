package com.example.appify;

import android.net.Uri;

public class Event {
    private String name;
    private String date;
    private String description;
    private int maxWishEntrants;
    private int maxSampleEntrants;
    private String posterUri;  // Store URI as String
    private boolean isGeolocate;

    // No-argument constructor for Firestore
    public Event() {}

    public Event(String name, String date, String description, int maxWishEntrants, int maxSampleEntrants, Uri posterUri, boolean isGeolocate) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.maxWishEntrants = maxWishEntrants;
        this.maxSampleEntrants = maxSampleEntrants;
        this.posterUri = posterUri != null ? posterUri.toString() : null; // Convert URI to String
        this.isGeolocate = isGeolocate;
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

    public boolean isGeolocate() {
        return isGeolocate;
    }

    // Override the toString() method to display the event name in the ListView
    @Override
    public String toString() {
        return name;
    }
}
