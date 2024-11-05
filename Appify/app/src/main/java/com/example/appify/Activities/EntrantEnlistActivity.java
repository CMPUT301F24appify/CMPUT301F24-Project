package com.example.appify.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.HeaderNavigation;
import com.example.appify.R;

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

    // Placeholder for enlist functionality
    private void enlistInEvent(String eventId) {
        // Add your code to enlist the user in the event (e.g., Firestore update)
        Toast.makeText(this, "Enlisted in event: " + eventId, Toast.LENGTH_SHORT).show();
    }

    // Placeholder for leave functionality
    private void leaveEvent(String eventId) {
        // Add your code to remove the user from the event's waiting list
        Toast.makeText(this, "Left the event: " + eventId, Toast.LENGTH_SHORT).show();
    }
}
