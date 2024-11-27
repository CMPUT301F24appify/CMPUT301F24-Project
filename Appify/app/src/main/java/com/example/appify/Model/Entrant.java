package com.example.appify.Model;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents an entrant with personal details and preferences.
 * An entrant can have various attributes such as name, contact information,
 * profile picture, notification preferences, and a list of events they are associated with.
 */
public class Entrant {
    // Attributes
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private boolean notifications;
    private String facilityID;


    /**
     * Constructs an Entrant with the specified attributes.
     *
     * @param id                the unique ID of the entrant
     * @param name              the name of the entrant
     * @param phoneNumber       the phone number of the entrant
     * @param email             the email address of the entrant
     * @param profilePicture    the URL of the entrant's profile picture
     * @param notifications     the notification preference of the entrant
     */
    public Entrant(String id, String name, String phoneNumber, String email, String profilePicture, boolean notifications) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.profilePictureUrl = profilePicture;
        this.notifications = notifications;
        this.facilityID = facilityID;
    }


    public void setFacilityID(String facilityID) {
        this.facilityID = facilityID;
    }
// Getters

    /**
     * Gets the unique ID of the entrant.
     *
     * @return the entrant's ID
     */
    public String getId() {
        return id;
    }
    /**
     * Gets the name of the entrant.
     *
     * @return the entrant's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the phone number of the entrant.
     *
     * @return the entrant's phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number of the entrant.
     *
     * @param phoneNumber the entrant's new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the email address of the entrant.
     *
     * @return the entrant's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the URL of the entrant's profile picture.
     *
     * @return the URL of the profile picture
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * Sets the URL of the entrant's profile picture.
     *
     * @param profilePictureUrl the new URL of the profile picture
     */
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * Checks whether notifications are enabled for the entrant.
     *
     * @return true if notifications are enabled, false otherwise
     */
    public boolean isNotifications() {
        return notifications;
    }

    /**
     * Sets the notification preference for the entrant.
     *
     * @param notifications true to enable notifications, false to disable
     */
    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }


    /**
     * Gets the Facility ID of the Entrant's Facility
     *
     * @return the Facility ID
     */
    public String getFacilityID() { return facilityID; }


    //The 2 functions below were done with major assistance from chatGPT, "Help make accept and
    //decline functions (provided explanation of how the database is structured), (explained that
    //need to update in both waitlists)", 2024-11-05
    /**
     * Updates the entrant's status to "accepted" for a specific event in both the event's waiting list
     * and the AndroidID collection.
     *
     * @param db      The Firestore database instance used to access and update the database.
     * @param eventID The unique identifier of the event for which the entrant's status is being updated.
     *
     */
    public void acceptEvent(FirebaseFirestore db, String eventID) {
        // Update status to "accepted" in the event's waiting list
        db.collection("events").document(eventID)
                .collection("waitingList").document(this.id)
                .update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    Log.d("Entrant", "Status updated to 'accepted' in waiting list for entrant " + this.id);

                    // Update status to "accepted" in the AndroidID collection for the specific event
                    db.collection("AndroidID").document(this.id)
                            .collection("waitListedEvents").document(eventID)
                            .update("status", "accepted")
                            .addOnSuccessListener(innerVoid -> {
                                Log.d("Entrant", "Status updated to 'accepted' in AndroidID collection for entrant " + this.id);
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Entrant", "Error updating status in AndroidID collection for entrant " + this.id, e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("Entrant", "Error updating status in waiting list for entrant " + this.id, e);
                });
    }
    /**
     * Updates the entrant's status to "declined" for a specified event in both the event's waiting list
     * and the AndroidID collection, and re-runs the lottery to select a replacement entrant
     *
     * @param db      The Firestore database instance used to access and update the database.
     * @param eventID The unique identifier of the event for which the entrant's status is being updated.
     * @param event   The Event instance on which to call the lottery method if the decline is successful.
     *

     */
    public void declineEvent(FirebaseFirestore db, String eventID, Event event) {
        // Update status to "declined" in the event's waiting list
        db.collection("events").document(eventID)
                .collection("waitingList").document(this.id)
                .update("status", "rejected")
                .addOnSuccessListener(aVoid -> {
                    Log.d("Entrant", "Status updated to 'declined' in waiting list for entrant " + this.id);

                    // Update status to "declined" in the AndroidID collection for the specific event
                    db.collection("AndroidID").document(this.id)
                            .collection("waitListedEvents").document(eventID)
                            .update("status", "rejected")
                            .addOnSuccessListener(innerVoid -> {
                                Log.d("Entrant", "Status updated to 'rejected' in AndroidID collection for entrant " + this.id);

                                // Run the lottery again to select a replacement entrant
                                event.lottery(db, eventID);
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Entrant", "Error updating status in AndroidID collection for entrant " + this.id, e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("Entrant", "Error updating status in waiting list for entrant " + this.id, e);
                });
    }
}


