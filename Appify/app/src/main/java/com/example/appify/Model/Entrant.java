package com.example.appify.Model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

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
}

