package com.example.appify;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String eventID;
    private Button enrollButton, leaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Initialize Firebase Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get the event ID from the intent
        eventID = getIntent().getStringExtra("eventID");  // Retrieve the event ID passed from MainActivity

        // Find views
        TextView eventName = findViewById(R.id.event_name);
        TextView eventDates = findViewById(R.id.event_dates);
        enrollButton = findViewById(R.id.enroll_button);
        leaveButton = findViewById(R.id.leave_button);

        // Load the event details
        loadEventDetails(eventName, eventDates);

        // Enroll button click listener
        // Inside your EventDetailsActivity
        enrollButton.setOnClickListener(v -> {
            // Add the current user to the event's waiting list
            addEntrantToWaitingList(eventID, db);
        });

        // Leave button click listener
        leaveButton.setOnClickListener(v -> {
            removeEntrantFromWaitingList(eventID, db);
        });
    }

    // Method to load the event details from Firestore using the event ID
    private void loadEventDetails(TextView eventName, TextView eventDates) {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                eventName.setText(documentSnapshot.getString("name"));
                eventDates.setText(documentSnapshot.getString("startDate") + " - " + documentSnapshot.getString("endDate"));
            }
        });
    }

    // Method to handle enrolling in the event waiting list
//    private void enrollInEvent() {
//        // Get the current user details from Firebase Authentication
//        String userID = auth.getCurrentUser().getUid();
//        String userName = auth.getCurrentUser().getDisplayName();
//        String userEmail = auth.getCurrentUser().getEmail();
//
//        // Create an Entrant object
//        Entrant entrant = new Entrant(userID, userName, userEmail);
//
//        // Add the entrant to the event's waiting list
//        entrant.joinWaitingList(eventID, db);
//
//        // Show a Toast message to indicate success
//        Toast.makeText(EventDetailsActivity.this, "You've been added to the waiting list!", Toast.LENGTH_SHORT).show();
//    }

    // Method to handle leaving the event waiting list
//    private void leaveEvent() {
//        String userID = auth.getCurrentUser().getUid();
//        Entrant entrant = new Entrant(userID, null, null);
//
//        // Remove the entrant from the event's waiting list
//        entrant.leaveWaitingList(eventID, db);
//
//        Toast.makeText(EventDetailsActivity.this, "You have left the waiting list.", Toast.LENGTH_SHORT).show();
//    }

    // Test Methods
    private void addEntrantToWaitingList(String eventID, FirebaseFirestore db) {
        // Get current user details (or provide them manually)
        String userID = "1234";
        String userName = "Bob Handsome";
        String userEmail = "Bobhandsome@gmail.com";
        String status = "enrolled";

        // Create the Entrant object
        Entrant entrant = new Entrant(userID, userName, userEmail, status);

        // Reference to the event's waiting list
        db.collection("events").document(eventID)
                .collection("waitingList").document(userID)  // Document ID is the user ID
                .set(entrant)  // Add the entrant to the waiting list
                .addOnSuccessListener(aVoid -> {
                    // Successfully added to the waiting list
                    Log.d("EventDetailsActivity", "Entrant added to waiting list successfully");
                    Toast.makeText(EventDetailsActivity.this, "You have been added to the waiting list!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to add entrant to waiting list
                    Log.e("EventDetailsActivity", "Error adding entrant to waiting list", e);
                    Toast.makeText(EventDetailsActivity.this, "Failed to add to waiting list", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeEntrantFromWaitingList(String eventID, FirebaseFirestore db) {
        // Get current user details (or provide them manually)
        String userID = "1234";
        String userName = "Bob Handsome";
        String userEmail = "Bobhandsome@gmail.com";
        String status = "enrolled";

        // Create the Entrant object
        Entrant entrant = new Entrant(userID, userName, userEmail, status);

        // Reference to the event's waiting list
        db.collection("events").document(eventID)
                .collection("waitingList").document(userID)  // Document ID is the user ID
                .delete()  // Add the entrant to the waiting list
                .addOnSuccessListener(aVoid -> {
                    // Successfully added to the waiting list
                    Log.d("EventDetailsActivity", "Entrant removed to waiting list successfully");
                    Toast.makeText(EventDetailsActivity.this, "You have been removed from the waiting list!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to add entrant to waiting list
                    Log.e("EventDetailsActivity", "Error removing entrant to waiting list", e);
                    Toast.makeText(EventDetailsActivity.this, "Failed to remove from waiting list", Toast.LENGTH_SHORT).show();
                });
    }




}
