package com.example.appify.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.HeaderNavigation;
import com.example.appify.MyApp;
import com.example.appify.R;

import java.util.HashMap;
import java.util.List;

/**
 * The EntrantEnlistActivity class provides the UI and functionality for users
 * to enlist in or leave an event’s waiting list. It displays event details and
 * includes enlist and leave buttons.
 */
public class EntrantEnlistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enlist_page);

        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();

        // Retrieve event details from the intent
        Intent intent = getIntent();
        String eventId = intent.getStringExtra("eventId");
        String name = intent.getStringExtra("name");
        String date = intent.getStringExtra("date");
        String registrationEndDate = intent.getStringExtra("registrationEndDate");
        String facility = intent.getStringExtra("facility");
        String description = intent.getStringExtra("description");
        int maxWishEntrants = intent.getIntExtra("maxWishEntrants", 0);
        int maxSampleEntrants = intent.getIntExtra("maxSampleEntrants", 0);
        String posterUriString = intent.getStringExtra("posterUri");
        boolean isGeolocate = intent.getBooleanExtra("isGeolocate", false);

        // Find views in the layout and set data
        TextView eventName = findViewById(R.id.event_name);
        TextView eventDate = findViewById(R.id.event_date);
        TextView eventDescription = findViewById(R.id.event_description);
        TextView eventFacility = findViewById(R.id.facility_name);
        TextView eventRegistrationEnd = findViewById(R.id.registration_date);
        TextView eventGeolocate = findViewById(R.id.geolocationText);

        eventName.setText(name);
        eventDate.setText(date);
        eventDescription.setText(description);
        eventRegistrationEnd.setText(registrationEndDate);
        eventFacility.setText(facility);

        // Show Geolocation Requirement
        if (isGeolocate) {
            eventGeolocate.setText("IMPORTANT: Registering for this event REQUIRES geolocation.");
        } else {
            eventGeolocate.setText("IMPORTANT: Registering for this event DOES NOT REQUIRE geolocation.");
        }

        // Handle Enlist and Leave buttons
        Button enlistButton = findViewById(R.id.enlist_button);
        Button leaveButton = findViewById(R.id.leave_button);

        enlistButton.setOnClickListener(v -> enlistInEvent(eventId));
        leaveButton.setOnClickListener(v -> leaveEvent(eventId));
    }

    /**
     * Enlists the current user in the specified event’s waiting list.
     * It checks if the user is already enlisted and verifies if the waiting list has capacity.
     *
     * @param eventId The unique ID of the event the user wishes to join.
     */
    private void enlistInEvent(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);
        MyApp app = (MyApp) getApplication();
        String androidId = app.getAndroidId();

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve maxWishEntrants and check the current count of waitingList
                int maxWishEntrants = documentSnapshot.getLong("maxWishEntrants").intValue();
                CollectionReference waitingListRef = eventRef.collection("waitingList");

                // Check if the user is already in the waitingList
                waitingListRef.document(androidId).get().addOnSuccessListener(docSnapshot -> {
                    if (docSnapshot.exists()) {
                        Toast.makeText(this, "Already in the event", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed to add the user if the waiting list is not full
                        waitingListRef.get().addOnSuccessListener(querySnapshot -> {
                            int currentEntrants = querySnapshot.size();

                            if (currentEntrants >= maxWishEntrants) {
                                Toast.makeText(this, "Waiting list is full.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Step 1: Add user to the event's waitingList subcollection with status "enrolled"
                                HashMap<String, Object> waitlistData = new HashMap<>();
                                waitlistData.put("status", "enrolled");

                                waitingListRef.document(androidId).set(waitlistData) // Add status field
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Successfully enlisted!", Toast.LENGTH_SHORT).show();

                                            // Step 2: Add eventId to user's waitListedEvents subcollection with status "enrolled"
                                            CollectionReference userWaitListedEventsRef = db.collection("Android ID")
                                                    .document(androidId)
                                                    .collection("waitListedEvents");

                                            HashMap<String, Object> eventStatusData = new HashMap<>();
                                            eventStatusData.put("status", "enrolled");

                                            userWaitListedEventsRef.document(eventId).set(eventStatusData) // Add status field
                                                    .addOnSuccessListener(aVoid2 -> Toast.makeText(this, "Event added to your waitlisted events.", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add event to your waitlisted events.", Toast.LENGTH_SHORT).show());
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to enlist in the waiting list. Try again.", Toast.LENGTH_SHORT).show());
                            }
                        }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching waiting list data.", Toast.LENGTH_SHORT).show());
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Error checking enrollment status.", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching event data.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Removes the current user from the specified event’s waiting list.
     * It checks if the user is already enlisted before attempting removal.
     *
     * @param eventId The unique ID of the event the user wishes to leave.
     */
    private void leaveEvent(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);
        MyApp app = (MyApp) getApplication();
        String androidId = app.getAndroidId();

        // Step 1: Check if user is in the event's waitingList subcollection
        CollectionReference waitingListRef = eventRef.collection("waitingList");

        waitingListRef.document(androidId).get().addOnSuccessListener(docSnapshot -> {
            if (!docSnapshot.exists()) {
                Toast.makeText(this, "Not enrolled in the waiting list", Toast.LENGTH_SHORT).show();
            } else {
                // Remove user from the event's waitingList subcollection
                waitingListRef.document(androidId).delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Successfully left the event's waiting list.", Toast.LENGTH_SHORT).show();

                            // Step 2: Remove eventId from user's waitListedEvents subcollection
                            CollectionReference userWaitListedEventsRef = db.collection("Android ID")
                                    .document(androidId)
                                    .collection("waitListedEvents");

                            userWaitListedEventsRef.document(eventId).delete()
                                    .addOnSuccessListener(aVoid2 -> Toast.makeText(this, "Event removed from your waitlisted events.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove event from your waitlisted events.", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to leave the event's waiting list. Try again.", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error checking enrollment status.", Toast.LENGTH_SHORT).show());
    }

}
