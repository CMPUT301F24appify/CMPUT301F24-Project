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

                // Check the count of documents in the waitingList collection
                waitingListRef.get().addOnSuccessListener(querySnapshot -> {
                    int currentEntrants = querySnapshot.size();

                    if (currentEntrants >= maxWishEntrants) {
                        Toast.makeText(this, "Waiting list is full.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Step 1: Add user to the event's waitingList subcollection
                        waitingListRef.document(androidId).set(new HashMap<>()) // Use HashMap to create an empty document
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Successfully enlisted!", Toast.LENGTH_SHORT).show();

                                    // Step 2: Add eventId to user's waitListedEvents subcollection
                                    CollectionReference userWaitListedEventsRef = db.collection("Android ID")
                                            .document(androidId)
                                            .collection("waitListedEvents");
                                    userWaitListedEventsRef.document(eventId).set(new HashMap<>()) // Use HashMap for an empty document
                                            .addOnSuccessListener(aVoid2 -> Toast.makeText(this, "Event added to your waitlisted events.", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to add event to your waitlisted events.", Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to enlist in the waiting list. Try again.", Toast.LENGTH_SHORT).show());
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching waiting list data.", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching event data.", Toast.LENGTH_SHORT).show());
    }


    private void leaveEvent(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);
        MyApp app = (MyApp) getApplication();
        String androidId = app.getAndroidId();

        // Step 1: Remove user from the event's waitingList subcollection
        CollectionReference waitingListRef = eventRef.collection("waitingList");

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

}
