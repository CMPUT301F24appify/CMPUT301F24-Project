package com.example.appify.Model;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.UUID;

/**
 * Represents an event with various attributes such as name, date, facility, and more.
 * Allows retrieval of event data from Firestore and provides methods to access event details.
 */
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


    /**
     * Constructs an Event object with the specified attributes.
     *
     * @param name              the name of the event
     * @param date              the date of the event
     * @param registrationEndDate the registration end date for the event
     * @param description       a description of the event
     * @param facility          the facility where the event takes place
     * @param maxWishEntrants   the maximum number of wish-listed entrants
     * @param maxSampleEntrants the maximum number of sample-selected entrants
     * @param posterUri         the URI of the event's poster image
     * @param isGeolocate       whether geolocation is required for this event
     */
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

    /**
     * Creates an Event object from a Firestore document.
     *
     * @param document the Firestore document representing the event
     * @return an Event object populated with data from the Firestore document
     */
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

    /**
     * Gets the name of the event.
     *
     * @return the name of the event
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the date of the event.
     *
     * @return the date of the event
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the registration end date for the event.
     *
     * @return the registration end date for the event
     */
    public String getRegistrationEndDate() { return registrationEndDate; }

    /**
     * Gets the facility where the event takes place.
     *
     * @return the facility of the event
     */
    public String getFacility() { return facility; }

    /**
     * Gets the description of the event.
     *
     * @return the description of the event
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the maximum number of wish-listed entrants allowed for the event.
     *
     * @return the maximum number of wish-listed entrants
     */
    public int getMaxWishEntrants() {
        return maxWishEntrants;
    }

    /**
     * Gets the maximum number of sample-selected entrants for the event.
     *
     * @return the maximum number of sample-selected entrants
     */
    public int getMaxSampleEntrants() {
        return maxSampleEntrants;
    }

    /**
     * Gets the URI of the event's poster image.
     *
     * @return the URI of the event's poster
     */
    public String getPosterUri() {
        return posterUri;
    }

    /**
     * Gets the ID of the organizer associated with the event.
     *
     * @return the organizer ID
     */
    public String getOrganizerID() {
        return organizerID;
    }

    /**
     * Gets the unique ID of the event.
     *
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Indicates whether geolocation is required for the event.
     *
     * @return true if geolocation is required, false otherwise
     */
    public boolean isGeolocate() {
        return isGeolocate;
    }


}
