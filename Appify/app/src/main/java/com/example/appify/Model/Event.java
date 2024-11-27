package com.example.appify.Model;


import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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
    private int maxWaitEntrants;
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
     * @param maxWaitEntrants   the maximum number of wait-listed entrants
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
                 String description, int maxWaitEntrants, int maxSampleEntrants,
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
        this.maxWaitEntrants = maxWaitEntrants;
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

    /**
     * No-argument constructor required by Firestore.
     */
    public Event() {
    }


    //The function below was done with major assistance from chatGPT, "Help make the lottery function
    //(provided explanation of how the database is structured), (explained that
    //need to update in both waitlists)", 2024-11-05
    /**
     * Runs a lottery to randomly select entrants with "enrolled" status from the waiting list of a specified event
     * and updates their status to "invited." The method first checks the number of entrants already accepted and
     * adjusts the number of additional entrants to invite based on the remaining available slots (`maxSampleEntrants - acceptedCount`).
     * The lottery continues to invite entrants until this adjusted limit is reached or no more eligible entrants are available.
     * Additionally, it updates the entrant's status in their AndroidID collection under "waitListedEvents" for the specific event.
     *
     * @param db      The Firestore database instance used to access and update the database.
     * @param eventID The unique identifier of the event for which the lottery is being conducted.
     */
    public void lottery(FirebaseFirestore db, String eventID) {
        ArrayList<String> eligibleEntrants = new ArrayList<>();
        ArrayList<String> selectedEntrants = new ArrayList<>(); // List for randomly chosen entrants
        Random random = new Random();

        // First, count the number of already accepted entrants
        db.collection("events").document(eventID)
                .collection("waitingList")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(acceptedSnapshot -> {
                    int acceptedCount = acceptedSnapshot.size();
                    int slotsAvailable = maxSampleEntrants - acceptedCount; // Adjust for remaining slots

                    // If there are no slots available, exit early
                    if (slotsAvailable <= 0) {
                        Log.d("Lottery", "No slots available for additional invites.");
                        return;
                    }

                    // Retrieve the waiting list for "enrolled" entrants
                    db.collection("events").document(eventID)
                            .collection("waitingList")
                            .whereEqualTo("status", "enrolled") // Only consider entrants with "enrolled" status
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                // Collect all eligible entrants' IDs
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    eligibleEntrants.add(document.getId());
                                }

                                // Randomly select entrants until reaching available slots or list is empty
                                while (selectedEntrants.size() < slotsAvailable && !eligibleEntrants.isEmpty()) {
                                    int randomIndex = random.nextInt(eligibleEntrants.size());
                                    String selectedEntrantId = eligibleEntrants.remove(randomIndex); // Remove to avoid re-selection
                                    selectedEntrants.add(selectedEntrantId); // Add to selected list
                                }

                                // Invite only the selected entrants
                                for (String entrantId : selectedEntrants) {
                                    // Update the entrant's status to "invited" in the waiting list of the event
                                    db.collection("events").document(eventID)
                                            .collection("waitingList").document(entrantId)
                                            .update("status", "invited")
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("Lottery", "Entrant " + entrantId + " invited successfully.");

                                                // Update the entrant's status in their AndroidID collection as well
                                                db.collection("AndroidID").document(entrantId)
                                                        .collection("waitListedEvents").document(eventID)
                                                        .update("status", "invited")
                                                        .addOnSuccessListener(innerVoid -> {
                                                            Log.d("Lottery", "Entrant " + entrantId + " status updated in AndroidID collection for event " + eventID);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.w("Lottery", "Error updating entrant " + entrantId + " status in AndroidID collection for event " + eventID, e);
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w("Lottery", "Error inviting entrant " + entrantId, e);
                                            });
                                }

                                Log.d("Lottery", "Lottery completed. Total invited entrants: " + selectedEntrants.size());
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Lottery", "Error retrieving waiting list for event " + eventID, e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Lottery", "Error retrieving accepted entrants for event " + eventID, e);
                });
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
     * Gets the maximum number of wait-listed entrants allowed for the event.
     *
     * @return the maximum number of wait-listed entrants
     */
    public int getMaxWaitEntrants() {
        return maxWaitEntrants;
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

    /**
     * Indicates if notifications for waitlisted entrants are enabled.
     *
     * @return true if waitlisted notifications are enabled, otherwise false
     */
    public boolean isNotifyWaitlisted() {
        return notifyWaitlisted;
    }

    /**
     * Indicates if notifications for enrolled entrants are enabled.
     *
     * @return true if enrolled notifications are enabled, otherwise false
     */
    public boolean isNotifyEnrolled() {
        return notifyEnrolled;
    }

    /**
     * Indicates if notifications for cancelled entrants are enabled.
     *
     * @return true if cancelled notifications are enabled, otherwise false
     */
    public boolean isNotifyCancelled() {
        return notifyCancelled;
    }

    /**
     * Indicates if notifications for invited entrants are enabled.
     *
     * @return true if invited notifications are enabled, otherwise false
     */
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

    // Setters

    /**
     * Sets whether geolocation is required for the event.
     *
     * @param geolocate true if geolocation is required, false otherwise
     */
    public void setGeolocate(boolean geolocate) {
        this.isGeolocate = geolocate;
    }

    /**
     * Sets the name of the event.
     *
     * @param name the new name of the event
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the date of the event.
     *
     * @param date the new date of the event
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Sets the facility for the event.
     *
     * @param facility the new facility for the event
     */
    public void setFacility(String facility) {
        this.facility = facility;
    }

    /**
     * Sets the registration end date for the event.
     *
     * @param registrationEndDate the new registration end date for the event
     */
    public void setRegistrationEndDate(String registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the new description of the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the maximum number of wait-listed entrants for the event.
     *
     * @param maxWaitEntrants the new maximum wait-listed entrants
     */
    public void setMaxWaitEntrants(int maxWaitEntrants) {
        this.maxWaitEntrants = maxWaitEntrants;
    }

    /**
     * Sets the maximum number of sample-selected entrants for the event.
     *
     * @param maxSampleEntrants the new maximum sample-selected entrants
     */
    public void setMaxSampleEntrants(int maxSampleEntrants) {
        this.maxSampleEntrants = maxSampleEntrants;
    }

    /**
     * Sets the URI of the event's poster image.
     *
     * @param posterUri the new URI of the event's poster
     */
    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    /**
     * Sets whether to notify waitlisted entrants.
     *
     * @param notifyWaitlisted true to enable waitlisted notifications, false otherwise
     */
    public void setNotifyWaitlisted(boolean notifyWaitlisted) {
        this.notifyWaitlisted = notifyWaitlisted;
    }

    /**
     * Sets whether to notify enrolled entrants.
     *
     * @param notifyEnrolled true to enable enrolled notifications, false otherwise
     */
    public void setNotifyEnrolled(boolean notifyEnrolled) {
        this.notifyEnrolled = notifyEnrolled;
    }

    /**
     * Sets whether to notify cancelled entrants.
     *
     * @param notifyCancelled true to enable cancelled notifications, false otherwise
     */
    public void setNotifyCancelled(boolean notifyCancelled) {
        this.notifyCancelled = notifyCancelled;
    }

    /**
     * Sets whether to notify invited entrants.
     *
     * @param notifyInvited true to enable invited notifications, false otherwise
     */
    public void setNotifyInvited(boolean notifyInvited) {
        this.notifyInvited = notifyInvited;
    }

    /**
     * Sets the notification message for waitlisted entrants.
     *
     * @param waitlistedMessage the new waitlisted notification message
     */
    public void setWaitlistedMessage(String waitlistedMessage) {
        this.waitlistedMessage = waitlistedMessage;
    }

    /**
     * Sets the notification message for enrolled entrants.
     *
     * @param enrolledMessage the new enrolled notification message
     */
    public void setEnrolledMessage(String enrolledMessage) {
        this.enrolledMessage = enrolledMessage;
    }

    /**
     * Sets the notification message for cancelled entrants.
     *
     * @param cancelledMessage the new cancelled notification message
     */
    public void setCancelledMessage(String cancelledMessage) {
        this.cancelledMessage = cancelledMessage;
    }

    /**
     * Sets the notification message for invited entrants.
     *
     * @param invitedMessage the new invited notification message
     */
    public void setInvitedMessage(String invitedMessage) {
        this.invitedMessage = invitedMessage;
    }

    /**
     * Sets the Firestore database instance for this event.
     *
     * @param db the Firestore database instance
     */
    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Sets the ID of the organizer for this event.
     *
     * @param organizerID the new organizer ID
     */
    public void setOrganizerID(String organizerID) {
        this.organizerID = organizerID;
    }

    /**
     * Sets the unique ID of the event.
     *
     * @param eventId the new event ID
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Interface for callback after adding an event to Firestore.
     */
    public interface EventAddCallback {
        void onEventAdded(Event event);
    }

    /**
     * Adds this event to Firestore.
     *
     * @param callback The callback to handle the event addition result.
     */
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
