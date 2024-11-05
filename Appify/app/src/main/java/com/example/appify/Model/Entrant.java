package com.example.appify.Model;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Entrant {
    // Attributes
    private String id;
    private String name;
    private String email;
    private String status;


    // Constructor
    public Entrant(String id, String name, String email, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    // Methods

    // Join the WaitingList of an event
    public void joinWaitingList(String eventID, FirebaseFirestore db) {
        db.collection("events").document(eventID)
                .collection("waitingList").document(this.id)
                .set(this)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Entrant", "Joined waiting list successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Entrant", "Error joining waiting list", e);
                });
    }

    // Leave the waitingList of an event
    public void leaveWaitingList(String eventID, FirebaseFirestore db) {
        db.collection("events").document(eventID)
                .collection("waitingList").document(this.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Entrant", "Left waiting list successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Entrant", "Error leaving waiting list", e);
                });
    }
    public void acceptEvent(FirebaseFirestore db, String eventID) {
        //General logic - grab ids of event and user. Find the user in the waiting list by id, update
        db.collection("events").document(eventID)
                .collection("waitingList").document(this.id)
                .update("status", "accepted") // Update the "status" field to the new status
                .addOnSuccessListener(aVoid -> {
                    Log.d("Entrant", "Status updated successfully for entrant " + this.id);
                })
                .addOnFailureListener(e -> {
                    Log.w("Entrant", "Error updating status for entrant " + this.id, e);
                });
    }
    public void declineEvent(FirebaseFirestore db, String eventID, Event event) {
        // Update Firestore to mark the entrant's status as "declined"
        db.collection("events").document(eventID)
                .collection("waitingList").document(this.id)
                .update("status", "declined")
                .addOnSuccessListener(aVoid -> {
                    Log.d("Entrant", "Status updated to 'declined' for entrant " + this.id);

                    // Trigger lottery rerun to select a replacement entrant
                    event.lottery(db, eventID);
                })
                .addOnFailureListener(e -> {
                    Log.w("Entrant", "Error updating status for entrant " + this.id, e);
                });
    }
}


