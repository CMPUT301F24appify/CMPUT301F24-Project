package com.example.appify.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.HeaderNavigation;
import com.example.appify.R;

import java.util.Objects;

/**
 * EnlistConfirmationActivity displays a confirmation message to the user
 * after they have enlisted or removed themselves from an event's waitlist.
 * It includes details about the event and provides a navigation button
 * to return to the main events page.
 */
public class EnlistConfirmationActivity extends AppCompatActivity {

    private String confirmation; // The enlistment status of the user (e.g., "Joined" or removed status)
    private String name; // The name of the event
    private String date; // The date of the event
    private String registrationEndDate; // The registration deadline for the event
    private String facility; // The facility where the event is held
    private boolean isGeolocate; // Whether geolocation is enabled for this event
    private Button homeButton; // Button to navigate back to the home page

    /**
     * Initializes the EnlistConfirmationActivity and sets up the UI elements.
     * Displays event details and a confirmation message based on the user's
     * enlistment status for the event.
     *
     * @param savedInstanceState The previously saved instance state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enlist_confirmation_page);

        // Initialize Header Navigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();

        // Retrieve event details from the intent
        Intent intent = getIntent();
        confirmation = intent.getStringExtra("waitingList");
        name = intent.getStringExtra("name");
        date = intent.getStringExtra("date");
        registrationEndDate = intent.getStringExtra("registrationEndDate");
        facility = intent.getStringExtra("facility");
        isGeolocate = intent.getBooleanExtra("geolocate", false);

        // Set up TextViews with event details
        TextView eventName = findViewById(R.id.event_name);
        TextView eventDate = findViewById(R.id.event_date);
        TextView eventFacility = findViewById(R.id.facility_name);
        TextView eventDescription = findViewById(R.id.confirmation_message);
        TextView eventRegistrationEnd = findViewById(R.id.registration_date);

        // Populate TextViews with data from the intent
        eventName.setText(name);
        eventDate.setText(date);
        eventRegistrationEnd.setText(registrationEndDate);
        eventFacility.setText(facility);

        // Display appropriate confirmation message based on enlistment status
        if (Objects.equals(confirmation, "Joined")) {
            eventDescription.setText("You have successfully been enlisted into the wait list for " + name + ". You will be notified if you are selected to register for the event.");
        } else {
            eventDescription.setText("You have successfully been removed from the wait list for " + name + ". You will no longer obtain notifications from this event.");
        }

        // Set up home button to navigate back to the events page
        homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            // Navigate to the EntrantHomePageActivity and close this activity
            Intent intent2 = new Intent(EnlistConfirmationActivity.this, EntrantHomePageActivity.class);
            startActivity(intent2);
            finish();
        });

    }
}
