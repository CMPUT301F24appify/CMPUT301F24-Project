package com.example.appify;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class Entrant {
    private String id;
    private String name;
    private String email;
    private String status;

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

    // Method to join a Waiting List
    public void joinWaitingList(String eventID, FirebaseFirestore db) {
        db.collection("events").document(eventID)
                .collection("waitingList").document(this.id)
                .set(this)  // Save entrant's info in the waiting list
                .addOnSuccessListener(aVoid -> {
                    Log.d("Entrant", "Joined waiting list successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Entrant", "Error joining waiting list", e);
                });
    }

    // Method to leave a Waiting List
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
