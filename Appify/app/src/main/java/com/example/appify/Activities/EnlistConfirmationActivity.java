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
    // Attributes
    private String confirmation;
    private String name;
    private String date;
    private String registrationEndDate;
    private String facility;
    private boolean isGeolocate;
    private Button homeButton;

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
        TextView eventGeolocate = findViewById(R.id.geolocationText);

        eventName.setText(name);
        eventDate.setText(date);
        eventRegistrationEnd.setText(registrationEndDate);
        eventFacility.setText(facility);

        if (isGeolocate) {
            eventGeolocate.setText("IMPORTANT: Registering for this event REQUIRES geolocation.");
        } else {
            eventGeolocate.setText("IMPORTANT: Registering for this event DOES NOT REQUIRE geolocation.");
        }

        // Display appropriate confirmation message based on enlistment status
        if (Objects.equals(confirmation, "Joined")) {
            eventDescription.setText("You have successfully been enlisted into the wait list for " + name + ". You will be notified if you are selected to register for the event.");
        } else {
            eventDescription.setText("You have successfully been removed from the wait list for " + name + ". You will no longer obtain notifications from this event.");
        }

        // Set up home button to navigate back to the events page
        homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            Intent intent2 = new Intent(EnlistConfirmationActivity.this, EntrantHomePageActivity.class);
            startActivity(intent2);
            finish();
        });

    }
}
