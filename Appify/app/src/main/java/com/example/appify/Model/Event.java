// Event.java
package com.example.appify.Model;

import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
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
    private int maxWaitEntrants;
    private int maxSampleEntrants;
    private String posterUri;  // Store URI as String
    private boolean isGeolocate;
    private String eventId;
    private boolean notifyWaitlisted;
    private boolean notifyEnrolled;
    private boolean notifyCancelled;
    private boolean notifyInvited;
    private int lotteryRan; // Field changed to int
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String organizerID;

    // Notification message fields
    private String waitlistedMessage;  // For "enrolled" status
    private String enrolledMessage;    // For "accepted" status
    private String cancelledMessage;   // For "rejected" status
    private String invitedMessage;     // For "invited" status

    /**
     * Constructs an Event object with the specified attributes.
     *
     * @param name                the name of the event
     * @param date                the date of the event
     * @param facility            the facility where the event takes place
     * @param registrationEndDate the registration end date for the event
     * @param description         a description of the event
     * @param maxWaitEntrants     the maximum number of wait-listed entrants
     * @param maxSampleEntrants   the maximum number of sample-selected entrants
     * @param posterUri           the URI of the event's poster image
     * @param isGeolocate         whether geolocation is required for this event
     * @param notifyWaitlisted    whether to notify waitlisted entrants
     * @param notifyEnrolled      whether to notify enrolled entrants
     * @param notifyCancelled     whether to notify cancelled entrants
     * @param notifyInvited       whether to notify invited entrants
     * @param waitlistedMessage   the notification message for waitlisted entrants
     * @param enrolledMessage     the notification message for enrolled entrants
     * @param cancelledMessage    the notification message for cancelled entrants
     * @param invitedMessage      the notification message for invited entrants
     * @param organizerID         the ID of the organizer
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
        this.lotteryRan = 0; // Initialize to 0
    }
    /**
     * Constructor used for Admin View, only taking required fields.
     *
     * @param name              the name of the event
     * @param date              the date of the event
     * @param facility          the facility where the event takes place
     * @param registrationEndDate the registration end date for the event
     * @param maxWaitEntrants   the maximum number of wait-listed entrants
     * @param maxSampleEntrants the maximum number of sample-selected entrants
     * @param organizerID       the ID of the organizer
     */
    public Event(String name, String date, String facility, String registrationEndDate,
                  int maxWaitEntrants, int maxSampleEntrants, String organizerID){
        this.name = name;
        this.date = date;
        this.registrationEndDate = registrationEndDate;
        this.facility = facility;
        this.maxWaitEntrants = maxWaitEntrants;
        this.maxSampleEntrants = maxSampleEntrants;
        this.organizerID = organizerID;
    }

    /**
     * No-argument constructor required by Firestore.
     */
    public Event() {
    }

    // Getters

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getRegistrationEndDate() {
        return registrationEndDate;
    }

    public String getFacility() {
        return facility;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxWaitEntrants() {
        return maxWaitEntrants;
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

    public String getWaitlistedMessage() {
        return waitlistedMessage;
    }

    public String getEnrolledMessage() {
        return enrolledMessage;
    }

    public String getCancelledMessage() {
        return cancelledMessage;
    }

    public String getInvitedMessage() {
        return invitedMessage;
    }

    public int getLotteryRan() {
        return lotteryRan;
    }

    // Setters

    public void setGeolocate(boolean geolocate) {
        this.isGeolocate = geolocate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public void setRegistrationEndDate(String registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMaxWaitEntrants(int maxWaitEntrants) {
        this.maxWaitEntrants = maxWaitEntrants;
    }

    public void setMaxSampleEntrants(int maxSampleEntrants) {
        this.maxSampleEntrants = maxSampleEntrants;
    }

    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    public void setNotifyWaitlisted(boolean notifyWaitlisted) {
        this.notifyWaitlisted = notifyWaitlisted;
    }

    public void setNotifyEnrolled(boolean notifyEnrolled) {
        this.notifyEnrolled = notifyEnrolled;
    }

    public void setNotifyCancelled(boolean notifyCancelled) {
        this.notifyCancelled = notifyCancelled;
    }

    public void setNotifyInvited(boolean notifyInvited) {
        this.notifyInvited = notifyInvited;
    }

    public void setWaitlistedMessage(String waitlistedMessage) {
        this.waitlistedMessage = waitlistedMessage;
    }

    public void setEnrolledMessage(String enrolledMessage) {
        this.enrolledMessage = enrolledMessage;
    }

    public void setCancelledMessage(String cancelledMessage) {
        this.cancelledMessage = cancelledMessage;
    }

    public void setInvitedMessage(String invitedMessage) {
        this.invitedMessage = invitedMessage;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public void setOrganizerID(String organizerID) {
        this.organizerID = organizerID;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setLotteryRan(int lotteryRan) {
        this.lotteryRan = lotteryRan;
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
                    db.collection("facilities").document(this.getFacility())
                            .collection("events").document(this.getEventId()).set(new HashMap<>())
                            .addOnSuccessListener(aVoid2 -> {
                                if (callback != null) {
                                    callback.onEventAdded(this);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("EventAdd", "Failed to add event to facility collection", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("EventAdd", "Failed to add event to Firestore", e);
                });
    }

    /**
     * Runs a lottery to randomly select entrants with "enrolled" status from the waiting list of a specified event
     * and updates their status to "invited." The method first checks the number of entrants already accepted and
     * adjusts the number of additional entrants to invite based on the remaining available slots (`maxSampleEntrants - acceptedCount`).
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
                        incrementLotteryRanFlag(db, eventID);
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

                                // Randomly select entrants until reaching ava (ilable slots or list is empty
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

                                incrementLotteryRanFlag(db, eventID);
                                Log.d("Lottery", "Lottery completed. Total invited entrants: " + selectedEntrants.size());
                            })
                            .addOnFailureListener(e -> Log.e("Lottery", "Error retrieving waiting list for event " + eventID, e));
                })
                .addOnFailureListener(e -> Log.e("Lottery", "Error retrieving accepted entrants for event " + eventID, e));
    }


    private void incrementLotteryRanFlag(FirebaseFirestore db, String eventID) {
        db.collection("events").document(eventID)
                .update("lotteryRan", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.d("Lottery", "LotteryRan counter incremented for event " + eventID))
                .addOnFailureListener(e -> Log.e("Lottery", "Error incrementing lotteryRan counter for event " + eventID, e));
    }
}
