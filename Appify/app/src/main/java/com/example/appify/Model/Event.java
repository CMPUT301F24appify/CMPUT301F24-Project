package com.example.appify.Model;

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
    private String facility;
    private String registrationEndDate;
    private String description;
    private int maxWishEntrants;
    private int maxSampleEntrants;
    private String posterUri;  // Store URI as String
    private boolean isGeolocate;
    private String eventId;
    private boolean notifyWaitlisted;
    private boolean notifyEnrolled;
    private boolean notifyCancelled;
    private boolean notifyInvited;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String organizerID;

    // New notification message fields
    private String waitlistedMessage;
    private String enrolledMessage;
    private String cancelledMessage;
    private String invitedMessage;

    /**
     * Constructs an Event object with the specified attributes.
     *
     * @param name              the name of the event
     * @param date              the date of the event
     * @param facility          the facility where the event takes place
     * @param registrationEndDate the registration end date for the event
     * @param description       a description of the event
     * @param maxWishEntrants   the maximum number of wish-listed entrants
     * @param maxSampleEntrants the maximum number of sample-selected entrants
     * @param posterUri         the URI of the event's poster image
     * @param isGeolocate       whether geolocation is required for this event
     * @param notifyWaitlisted  whether to notify waitlisted entrants
     * @param notifyEnrolled    whether to notify enrolled entrants
     * @param notifyCancelled   whether to notify cancelled entrants
     * @param notifyInvited     whether to notify invited entrants
     * @param waitlistedMessage the notification message for waitlisted entrants
     * @param enrolledMessage   the notification message for enrolled entrants
     * @param cancelledMessage  the notification message for cancelled entrants
     * @param invitedMessage    the notification message for invited entrants
     * @param organizerID       the ID of the organizer
     */
    public Event(String name, String date, String facility, String registrationEndDate,
                 String description, int maxWishEntrants, int maxSampleEntrants,
                 String posterUri, boolean isGeolocate, boolean notifyWaitlisted,
                 boolean notifyEnrolled, boolean notifyCancelled, boolean notifyInvited,
                 String waitlistedMessage, String enrolledMessage,
                 String cancelledMessage, String invitedMessage,
                 String organizerID) {
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
        this.notifyWaitlisted = notifyWaitlisted;
        this.notifyEnrolled = notifyEnrolled;
        this.notifyCancelled = notifyCancelled;
        this.notifyInvited = notifyInvited;
        this.waitlistedMessage = waitlistedMessage;
        this.enrolledMessage = enrolledMessage;
        this.cancelledMessage = cancelledMessage;
        this.invitedMessage = invitedMessage;
        this.organizerID = organizerID;
    }

    // No-argument constructor (required for Firebase)
    public Event() {
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
        boolean isGeolocate = document.getBoolean("geolocate") != null ? document.getBoolean("geolocate") : false;
        boolean notifyWaitlisted = document.getBoolean("notifyWaitlisted") != null ? document.getBoolean("notifyWaitlisted") : false;
        boolean notifyEnrolled = document.getBoolean("notifyEnrolled") != null ? document.getBoolean("notifyEnrolled") : false;
        boolean notifyCancelled = document.getBoolean("notifyCancelled") != null ? document.getBoolean("notifyCancelled") : false;
        boolean notifyInvited = document.getBoolean("notifyInvited") != null ? document.getBoolean("notifyInvited") : false;
        String organizerID = document.getString("organizerID");

        // Retrieve notification messages
        String waitlistedMessage = document.getString("waitlistedMessage");
        String enrolledMessage = document.getString("enrolledMessage");
        String cancelledMessage = document.getString("cancelledMessage");
        String invitedMessage = document.getString("invitedMessage");

        Event event = new Event(name, date, facility, registrationEndDate, description,
                maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate,
                notifyWaitlisted, notifyEnrolled, notifyCancelled, notifyInvited,
                waitlistedMessage, enrolledMessage, cancelledMessage, invitedMessage, organizerID);

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
    public String getRegistrationEndDate() {
        return registrationEndDate;
    }

    /**
     * Gets the facility where the event takes place.
     *
     * @return the facility of the event
     */
    public String getFacility() {
        return facility;
    }

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

    public boolean isNotifyWaitlisted() {
        return notifyWaitlisted;
    }

    public boolean isNotifyEnrolled() {
        return notifyEnrolled;
    }

    public boolean isNotifyCancelled() {
        return notifyCancelled;
    }

    public boolean isNotifyInvited() {
        return notifyInvited;
    }

    /**
     * Gets the notification message for waitlisted entrants.
     *
     * @return the waitlisted notification message
     */
    public String getWaitlistedMessage() {
        return waitlistedMessage;
    }

    /**
     * Gets the notification message for enrolled entrants.
     *
     * @return the enrolled notification message
     */
    public String getEnrolledMessage() {
        return enrolledMessage;
    }

    /**
     * Gets the notification message for cancelled entrants.
     *
     * @return the cancelled notification message
     */
    public String getCancelledMessage() {
        return cancelledMessage;
    }

    /**
     * Gets the notification message for invited entrants.
     *
     * @return the invited notification message
     */
    public String getInvitedMessage() {
        return invitedMessage;
    }

    // Setters (if needed)

    public void setGeolocate(boolean geolocate) {
        this.isGeolocate = geolocate;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    // EventAddCallback interface remains unchanged
    public interface EventAddCallback {
        void onEventAdded(Event event);
    }

    // Modify your addToFirestore method if necessary
    public void addToFirestore(EventAddCallback callback) {
        db.collection("events").document(this.eventId).set(this)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onEventAdded(this);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure
                });
    }
}
