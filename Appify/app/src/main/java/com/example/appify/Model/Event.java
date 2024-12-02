// Event.java
package com.example.appify.Model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * Represents an event with various attributes such as name, date, facility, and more.
 * Provides methods to manage event details, interact with Firestore, and conduct a lottery
 * to invite participants.
 */
public class Event {

    private String name; // Name of the event
    private String date; // Event date
    private String facility; // Facility where the event takes place
    private String registrationEndDate; // Registration end date
    private String description; // Event description
    private int maxWaitEntrants; // Maximum number of wait-listed entrants
    private int maxSampleEntrants; // Maximum number of sample-selected entrants
    private String posterUri; // URI of the event's poster
    private boolean isGeolocate; // Indicates if geolocation is required
    private String eventId; // Unique event ID
    private boolean notifyWaitlisted; // Flag for notifying waitlisted entrants
    private boolean notifyEnrolled; // Flag for notifying enrolled entrants
    private boolean notifyCancelled; // Flag for notifying cancelled entrants
    private boolean notifyInvited; // Flag for notifying invited entrants
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore database instance
    private String organizerID; // Organizer ID

    // Notification message fields
    private String waitlistedMessage;  // For "enrolled" status
    private String enrolledMessage;    // For "accepted" status
    private String cancelledMessage;   // For "rejected" status
    private String invitedMessage;     // For "invited" status
    private Boolean lotteryRanFlag; // Flag indicating if the lottery has been run
    private Boolean lotteryButton; // Button state for lottery functionality

    /**
     * Constructs an Event object with the specified attributes.
     *
     * @param name                Name of the event.
     * @param date                Date of the event.
     * @param facility            Facility where the event takes place.
     * @param registrationEndDate Registration end date for the event.
     * @param description         Description of the event.
     * @param maxWaitEntrants     Maximum number of wait-listed entrants.
     * @param maxSampleEntrants   Maximum number of sample-selected entrants.
     * @param posterUri           URI of the event's poster.
     * @param isGeolocate         Whether geolocation is required for this event.
     * @param notifyWaitlisted    Whether to notify waitlisted entrants.
     * @param notifyEnrolled      Whether to notify enrolled entrants.
     * @param notifyCancelled     Whether to notify cancelled entrants.
     * @param notifyInvited       Whether to notify invited entrants.
     * @param waitlistedMessage   Notification message for waitlisted entrants.
     * @param enrolledMessage     Notification message for enrolled entrants.
     * @param cancelledMessage    Notification message for cancelled entrants.
     * @param invitedMessage      Notification message for invited entrants.
     * @param organizerID         Organizer ID.
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
        this.lotteryRanFlag = false;
        this.lotteryButton = false;
    }

    /**
     * Constructs an Event object with minimal required attributes, typically for admin views.
     *
     * @param name                Name of the event.
     * @param date                Date of the event.
     * @param facility            Facility where the event takes place.
     * @param registrationEndDate Registration end date for the event.
     * @param maxWaitEntrants     Maximum number of wait-listed entrants.
     * @param maxSampleEntrants   Maximum number of sample-selected entrants.
     * @param organizerID         Organizer ID.
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

    /**
     * Gets the name of the event.
     *
     * @return The event's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the date of the event.
     *
     * @return The event's date.
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the registration end date of the event.
     *
     * @return The registration end date.
     */
    public String getRegistrationEndDate() {
        return registrationEndDate;
    }

    /**
     * Gets the facility where the event is held.
     *
     * @return The facility's identifier.
     */
    public String getFacility() {
        return facility;
    }

    /**
     * Gets the description of the event.
     *
     * @return The event's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the maximum number of wait-listed entrants for the event.
     *
     * @return The maximum number of wait-listed entrants.
     */
    public int getMaxWaitEntrants() {
        return maxWaitEntrants;
    }

    /**
     * Gets the maximum number of sample-selected entrants for the event.
     *
     * @return The maximum number of sample-selected entrants.
     */
    public int getMaxSampleEntrants() {
        return maxSampleEntrants;
    }

    /**
     * Gets the URI of the event's poster.
     *
     * @return The poster URI as a string.
     */
    public String getPosterUri() {
        return posterUri;
    }

    /**
     * Gets the ID of the organizer for the event.
     *
     * @return The organizer's ID.
     */
    public String getOrganizerID() {
        return organizerID;
    }

    /**
     * Gets the unique identifier for the event.
     *
     * @return The event ID.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Checks whether geolocation is required for the event.
     *
     * @return True if geolocation is required, otherwise false.
     */
    public boolean isGeolocate() {
        return isGeolocate;
    }

    /**
     * Checks whether waitlisted entrants will be notified.
     *
     * @return True if waitlisted entrants are notified, otherwise false.
     */
    public boolean isNotifyWaitlisted() {
        return notifyWaitlisted;
    }

    /**
     * Checks whether enrolled entrants will be notified.
     *
     * @return True if enrolled entrants are notified, otherwise false.
     */
    public boolean isNotifyEnrolled() {
        return notifyEnrolled;
    }

    /**
     * Checks whether cancelled entrants will be notified.
     *
     * @return True if cancelled entrants are notified, otherwise false.
     */
    public boolean isNotifyCancelled() {
        return notifyCancelled;
    }

    /**
     * Checks whether invited entrants will be notified.
     *
     * @return True if invited entrants are notified, otherwise false.
     */
    public boolean isNotifyInvited() {
        return notifyInvited;
    }

    /**
     * Gets the notification message for waitlisted entrants.
     *
     * @return The waitlisted notification message.
     */
    public String getWaitlistedMessage() {
        return waitlistedMessage;
    }

    /**
     * Gets the notification message for enrolled entrants.
     *
     * @return The enrolled notification message.
     */
    public String getEnrolledMessage() {
        return enrolledMessage;
    }

    /**
     * Gets the notification message for cancelled entrants.
     *
     * @return The cancelled notification message.
     */
    public String getCancelledMessage() {
        return cancelledMessage;
    }

    /**
     * Gets the notification message for invited entrants.
     *
     * @return The invited notification message.
     */
    public String getInvitedMessage() {
        return invitedMessage;
    }

    /**
     * Gets the Firestore database instance associated with the event.
     *
     * @return The Firestore database instance.
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Checks if the lottery has been run for the event.
     *
     * @return True if the lottery has been run, otherwise false.
     */
    public Boolean getLotteryRanFlag() {
        return lotteryRanFlag;
    }

    /**
     * Gets the current state of the lottery button.
     *
     * @return True if the lottery button is enabled, otherwise false.
     */
    public Boolean getLotteryButton() {
        return lotteryButton;
    }


    // Setters


    /**
     * Sets whether geolocation is required for the event.
     *
     * @param geolocate True if geolocation is required, otherwise false.
     */
    public void setGeolocate(boolean geolocate) {
        isGeolocate = geolocate;
    }

    /**
     * Sets whether waitlisted entrants should be notified.
     *
     * @param notifyWaitlisted True if waitlisted entrants should be notified, otherwise false.
     */
    public void setNotifyWaitlisted(boolean notifyWaitlisted) {
        this.notifyWaitlisted = notifyWaitlisted;
    }

    /**
     * Sets whether enrolled entrants should be notified.
     *
     * @param notifyEnrolled True if enrolled entrants should be notified, otherwise false.
     */
    public void setNotifyEnrolled(boolean notifyEnrolled) {
        this.notifyEnrolled = notifyEnrolled;
    }

    /**
     * Sets whether cancelled entrants should be notified.
     *
     * @param notifyCancelled True if cancelled entrants should be notified, otherwise false.
     */
    public void setNotifyCancelled(boolean notifyCancelled) {
        this.notifyCancelled = notifyCancelled;
    }

    /**
     * Sets whether invited entrants should be notified.
     *
     * @param notifyInvited True if invited entrants should be notified, otherwise false.
     */
    public void setNotifyInvited(boolean notifyInvited) {
        this.notifyInvited = notifyInvited;
    }

    /**
     * Sets the ID of the organizer for the event.
     *
     * @param organizerID The organizer's unique ID.
     */
    public void setOrganizerID(String organizerID) {
        this.organizerID = organizerID;
    }

    /**
     * Sets the notification message for waitlisted entrants.
     *
     * @param waitlistedMessage The notification message for waitlisted entrants.
     */
    public void setWaitlistedMessage(String waitlistedMessage) {
        this.waitlistedMessage = waitlistedMessage;
    }

    /**
     * Sets the notification message for enrolled entrants.
     *
     * @param enrolledMessage The notification message for enrolled entrants.
     */
    public void setEnrolledMessage(String enrolledMessage) {
        this.enrolledMessage = enrolledMessage;
    }

    /**
     * Sets the notification message for cancelled entrants.
     *
     * @param cancelledMessage The notification message for cancelled entrants.
     */
    public void setCancelledMessage(String cancelledMessage) {
        this.cancelledMessage = cancelledMessage;
    }

    /**
     * Sets the notification message for invited entrants.
     *
     * @param invitedMessage The notification message for invited entrants.
     */
    public void setInvitedMessage(String invitedMessage) {
        this.invitedMessage = invitedMessage;
    }

    /**
     * Sets the flag indicating if the lottery has been run for the event.
     *
     * @param lotteryRanFlag True if the lottery has been run, otherwise false.
     */
    public void setLotteryRanFlag(Boolean lotteryRanFlag) {
        this.lotteryRanFlag = lotteryRanFlag;
    }

    /**
     * Sets the state of the lottery button.
     *
     * @param lotteryButton True to enable the lottery button, otherwise false.
     */
    public void setLotteryButton(Boolean lotteryButton) {
        this.lotteryButton = lotteryButton;
    }

    /**
     * Sets the name of the event.
     *
     * @param name The name of the event.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the date of the event.
     *
     * @param date The date of the event.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Sets the facility where the event will take place.
     *
     * @param facility The facility's identifier.
     */
    public void setFacility(String facility) {
        this.facility = facility;
    }

    /**
     * Sets the registration end date for the event.
     *
     * @param registrationEndDate The registration end date.
     */
    public void setRegistrationEndDate(String registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    /**
     * Sets the description of the event.
     *
     * @param description The event's description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the maximum number of wait-listed entrants for the event.
     *
     * @param maxWaitEntrants The maximum number of wait-listed entrants.
     */
    public void setMaxWaitEntrants(int maxWaitEntrants) {
        this.maxWaitEntrants = maxWaitEntrants;
    }

    /**
     * Sets the maximum number of sample-selected entrants for the event.
     *
     * @param maxSampleEntrants The maximum number of sample-selected entrants.
     */
    public void setMaxSampleEntrants(int maxSampleEntrants) {
        this.maxSampleEntrants = maxSampleEntrants;
    }

    /**
     * Sets the URI of the event's poster.
     *
     * @param posterUri The poster URI as a string.
     */
    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    /**
     * Sets the Firestore database instance for the event.
     *
     * @param db The Firestore database instance.
     */
    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Sets the unique identifier for the event.
     *
     * @param eventId The event ID.
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
                        db.collection("events").document(eventID)
                                .update("lotteryRanFlag", true, "lotteryButton", true)
                                .addOnSuccessListener(aVoid -> Log.d("Lottery", "lotteryRanFlag and lotteryButton reset for event " + eventID))
                                .addOnFailureListener(e -> Log.e("Lottery", "Error resetting flags for event " + eventID, e));
                        return;
                    }


                    db.collection("events").document(eventID)
                            .collection("waitingList")
                            .whereEqualTo("status", "invited")
                            .get()
                            .addOnSuccessListener(invitedSnapshot -> {
                                int invitedCount = invitedSnapshot.size();
                                int slotsAvaliableFinal = slotsAvailable - invitedCount; // Adjust for remaining slots

                                // If there are no slots available, exit early
                                if (slotsAvaliableFinal <= 0) {
                                    db.collection("events").document(eventID)
                                            .update("lotteryRanFlag", true, "lotteryButton", true)
                                            .addOnSuccessListener(aVoid -> Log.d("Lottery", "lotteryRanFlag and lotteryButton reset for event " + eventID))
                                            .addOnFailureListener(e -> Log.e("Lottery", "Error resetting flags for event " + eventID, e));
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
                                        while (selectedEntrants.size() < slotsAvaliableFinal && !eligibleEntrants.isEmpty()) {
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
                                        db.collection("events").document(eventID)
                                                .update("lotteryRanFlag", true, "lotteryButton", true)
                                                .addOnSuccessListener(aVoid -> Log.d("Lottery", "lotteryRanFlag and lotteryButton reset for event " + eventID))
                                                .addOnFailureListener(e -> Log.e("Lottery", "Error resetting flags for event " + eventID, e));

                                        Log.d("Lottery", "Lottery completed. Total invited entrants: " + selectedEntrants.size());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Lottery", "Error retrieving waiting list for event " + eventID, e);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Lottery", "Error retrieving accepted entrants for event " + eventID, e);
                        });

                });
    }
}
