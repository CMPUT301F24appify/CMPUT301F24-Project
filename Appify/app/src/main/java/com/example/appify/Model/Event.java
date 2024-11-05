package com.example.appify.Model;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.UUID;

public class Event {
    private String name;
    private String date;
    private String registrationEndDate;
    private String description;
    private String facility;
    private int maxWishEntrants;
    private int maxSampleEntrants;
    private String posterUri;  // Store URI as String
    private boolean isGeolocate;
    private String eventId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private String organizerID;


    public Event(String name, String date, String registrationEndDate, String description, String facility, int maxWishEntrants,
                 int maxSampleEntrants, String posterUri, boolean isGeolocate) {
        this.name = name;
        this.date = date;
        this.registrationEndDate = registrationEndDate;
        this.description = description;
        this.facility = facility;
        this.maxWishEntrants = maxWishEntrants;
        this.maxSampleEntrants = maxSampleEntrants;
        this.posterUri = posterUri;
        this.isGeolocate = isGeolocate;
        this.eventId = UUID.randomUUID().toString();
    }

    public static Event fromFirestore(QueryDocumentSnapshot document) {
        String name = document.getString("name");
        String date = document.getString("date");
        String registrationEndDate = document.getString("registrationEndDate");
        String description = document.getString("description");
        String facility = document.getString("facility");
        int maxWishEntrants = document.getLong("maxWishEntrants").intValue();
        int maxSampleEntrants = document.getLong("maxSampleEntrants").intValue();
        String posterUri = document.getString("posterUri");
        boolean isGeolocate = document.getBoolean("isGeolocate") != null ? document.getBoolean("isGeolocate") : false;

        Event event = new Event(name, date, registrationEndDate, description, facility, maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate);
        event.eventId = document.getId(); // Use Firestore ID if available

        return event;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getRegistrationEndDate() { return registrationEndDate; }

    public String getFacility() { return facility; }

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
