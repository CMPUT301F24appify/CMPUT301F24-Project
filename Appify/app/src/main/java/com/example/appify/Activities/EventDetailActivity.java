package com.example.appify.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appify.Fragments.EditEventDialogFragment;
import com.example.appify.HeaderNavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appify.Model.Event;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity implements EditEventDialogFragment.EditEventDialogListener {
    private FirebaseFirestore db;
    private String eventID;

    // Variables to store notification messages
    private String waitlistedMessage = "";
    private String enrolledMessage = "";
    private String cancelledMessage = "";
    private String invitedMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // HeaderNavigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();
        TextView organizeText = findViewById(R.id.organizeText_navBar);
        organizeText.setTextColor(Color.parseColor("#800080"));
        organizeText.setTypeface(organizeText.getTypeface(), Typeface.BOLD);

        db = FirebaseFirestore.getInstance();


        // Retrieve event ID from the intent
        eventID = getIntent().getStringExtra("eventID");

        // Retrieve data from the Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String date = intent.getStringExtra("date");
        String facility = intent.getStringExtra("facility");
        String registrationEndDate = intent.getStringExtra("registrationEndDate");
        String description = intent.getStringExtra("description");
        int maxWaitEntrants = intent.getIntExtra("maxWaitEntrants", 0);
        int maxSampleEntrants = intent.getIntExtra("maxSampleEntrants", 0);
        String posterUriString = intent.getStringExtra("posterUri");
        boolean isGeolocate = intent.getBooleanExtra("isGeolocate", false);


        Uri posterUri = posterUriString != null && !posterUriString.isEmpty() ? Uri.parse(posterUriString) : null;

        // Bind data to views
        TextView nameTextView = findViewById(R.id.textViewName);
        TextView dateTextView = findViewById(R.id.textViewDate);
        TextView facilityTextView = findViewById(R.id.textViewFacility);
        TextView registrationEndDateTextView = findViewById(R.id.textViewRegistrationEndDate);
        TextView descriptionTextView = findViewById(R.id.textViewDescription);
        TextView maxWaitTextView = findViewById(R.id.textViewMaxWaitEntrants);
        TextView maxSampleTextView = findViewById(R.id.textViewMaxSampleEntrants);
        ImageView posterImageView = findViewById(R.id.imageViewPoster);
        TextView geolocateTextView = findViewById(R.id.textViewGeolocate);
        Button organizerActionsButton = findViewById(R.id.organizerActions);

        // Set up notification button click listeners
        Button notifyWaitlisted = findViewById(R.id.buttonWaitlisted);
        Button notifyEnrolled = findViewById(R.id.buttonEnrolled);
        Button notifyCancelled = findViewById(R.id.buttonCancelled);
        Button notifyInvited = findViewById(R.id.buttonInvited);


        notifyWaitlisted.setOnClickListener(v -> showNotificationInputDialog("Waitlisted Notification", waitlistedMessage, message -> {
            waitlistedMessage = message;
            updateNotificationMessage("waitlistedMessage", message, "notifyWaitlisted");
            updateButtonAppearance(notifyWaitlisted, !message.isEmpty());
        }));


        notifyEnrolled.setOnClickListener(v -> showNotificationInputDialog("Enrolled Notification", enrolledMessage, message -> {
            enrolledMessage = message;
            updateNotificationMessage("enrolledMessage", message, "notifyEnrolled");
            updateButtonAppearance(notifyEnrolled, !message.isEmpty());
        }));


        notifyCancelled.setOnClickListener(v -> showNotificationInputDialog("Cancelled Notification", cancelledMessage, message -> {
            cancelledMessage = message;
            updateNotificationMessage("cancelledMessage", message, "notifyCancelled");
            updateButtonAppearance(notifyCancelled, !message.isEmpty());
        }));


        notifyInvited.setOnClickListener(v -> showNotificationInputDialog("Invited Notification", invitedMessage, message -> {
            invitedMessage = message;
            updateNotificationMessage("invitedMessage", message, "notifyInvited");
            updateButtonAppearance(notifyInvited, !message.isEmpty());
        }));

        nameTextView.setText(name);
        dateTextView.setText(date);
        facilityTextView.setText(facility);
        registrationEndDateTextView.setText(registrationEndDate);
        descriptionTextView.setText(description);
        maxWaitTextView.setText("Max Waitlist Entrants: " + maxWaitEntrants);
        maxSampleTextView.setText("Max Sample Entrants: " + maxSampleEntrants);
        geolocateTextView.setText(isGeolocate ? "Geo-Location Enabled" : "Geo-Location Disabled");

        Button backButton = findViewById(R.id.buttonBackToEvents);
        backButton.setOnClickListener(v -> {
            Intent intent2 = new Intent(EventDetailActivity.this, EventActivity.class);
            intent2.putExtra("eventID", eventID);
            startActivity(intent2);

        });


        organizerActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(EventDetailActivity.this, EventActionsActivity.class);
                    intent.putExtra("name", name );
                    intent.putExtra("date", date);
                    intent.putExtra("facility", facility);
                    intent.putExtra("registrationEndDate", registrationEndDate);
                    intent.putExtra("description",description );
                    intent.putExtra("maxWaitEntrants", maxWaitEntrants);
                    intent.putExtra("maxSampleEntrants", maxSampleEntrants);
                    intent.putExtra("eventID", eventID);
                    startActivity(intent);
                }
            });
        Button entrantListButton = findViewById(R.id.entrant_list_button);


        Button editEventButton = findViewById(R.id.buttonEditEvent);
        editEventButton.setOnClickListener(v -> {
            EditEventDialogFragment dialog = new EditEventDialogFragment();
            dialog.show(getSupportFragmentManager(), "EditEventDialogFragment");
        });


        entrantListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference waitingListRef;
                waitingListRef = db.collection("events").document(eventID).collection("waitingList");

                waitingListRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalTasks = task.getResult().size();
                        if (totalTasks == 0){
                            // Check if there are any entrants on the waiting List.
                            Toast.makeText(getApplicationContext(), "No entrants", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // If there are, switch to the view entrants activity.
                            Intent intent = new Intent(EventDetailActivity.this, EventEntrantsActivity.class);
                            intent.putExtra("eventID", eventID);

                            startActivity(intent);
                        }
                    }
                });
            }
        });

        // Display the image if the URI is valid
        if (posterUri != null) {
            // Use Glide to load the image from the Firebase URL
            Glide.with(this).load(posterUri).into(posterImageView);
        } else {
//            posterImageView.setImageResource(R.drawable.placeholder_image);  // Set a placeholder if no image is available
        }
    }



    @Override
    public void onEventEdited(String name, String date, String facility, String registrationEndDate,
                             String description, int maxWaitEntrants, int maxSampleEntrants,
                             String posterUri, boolean isGeolocate,
                             String waitlistedMessage, String enrolledMessage,
                             String cancelledMessage, String invitedMessage) {

        MyApp app = (MyApp) getApplication();
        String organizerID = app.getAndroidId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("date", date);
        updates.put("facility", facility);
        updates.put("registrationEndDate", registrationEndDate);
        updates.put("description", description);
        updates.put("maxWaitEntrants", maxWaitEntrants);
        updates.put("maxSampleEntrants", maxSampleEntrants);
        updates.put("posterUri", posterUri);
        updates.put("geolocate", isGeolocate);
        updates.put("waitlistedMessage", waitlistedMessage);
        updates.put("enrolledMessage", enrolledMessage);
        updates.put("cancelledMessage", cancelledMessage);
        updates.put("invitedMessage", invitedMessage);

        // Update specific fields in Firestore
        db.collection("events").document(eventID)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    refreshEventUI(name, date, facility, registrationEndDate, description,
                            maxWaitEntrants, maxSampleEntrants, posterUri, isGeolocate);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Create new Event object with notification messages
        Event updatedEvent = new Event(name, date, facility, registrationEndDate, description,
                maxWaitEntrants, maxSampleEntrants, posterUri, isGeolocate,
                false, false, false, false,
                "", "", "", "",
                organizerID);
    }



    private void refreshEventUI(String name, String date, String facility, String registrationEndDate,
                                String description, int maxWaitEntrants, int maxSampleEntrants,
                                String posterUri, boolean isGeolocate) {

        // Bind the updated values to the UI components
        TextView nameTextView = findViewById(R.id.textViewName);
        TextView dateTextView = findViewById(R.id.textViewDate);
        TextView facilityTextView = findViewById(R.id.textViewFacility);
        TextView registrationEndDateTextView = findViewById(R.id.textViewRegistrationEndDate);
        TextView descriptionTextView = findViewById(R.id.textViewDescription);
        TextView maxWaitTextView = findViewById(R.id.textViewMaxWaitEntrants);
        TextView maxSampleTextView = findViewById(R.id.textViewMaxSampleEntrants);
        ImageView posterImageView = findViewById(R.id.imageViewPoster);
        TextView geolocateTextView = findViewById(R.id.textViewGeolocate);

        // Update each component with the new values
        nameTextView.setText(name);
        dateTextView.setText(date);
        facilityTextView.setText(facility);
        registrationEndDateTextView.setText(registrationEndDate);
        descriptionTextView.setText(description);
        maxWaitTextView.setText("Max Waitlist Entrants: " + maxWaitEntrants);
        maxSampleTextView.setText("Max Sample Entrants: " + maxSampleEntrants);
        geolocateTextView.setText(isGeolocate ? "Geo-Location Enabled" : "Geo-Location Disabled");

        // Update the poster image if available
        if (posterUri != null && !posterUri.isEmpty()) {
            Glide.with(this).load(posterUri).into(posterImageView);
        } else {
//            posterImageView.setImageResource(R.drawable.placeholder_image);  // Set a placeholder if no image is available
        }
    }



    private void updateButtonAppearance(Button button, boolean isActive) {
        if (isActive) {
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        } else {
            button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    // Interface for callback after input dialog
    private interface NotificationMessageCallback {
        void onMessageSet(String message);
    }

    private void showNotificationInputDialog(String title, String existingMessage, NotificationMessageCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setText(existingMessage);  // Set existing message if available
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String message = input.getText().toString();
            callback.onMessageSet(message);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Delete", (dialog, which) -> {
            callback.onMessageSet("");
            Toast.makeText(getApplicationContext(), "Notification message deleted", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }



    private void updateNotificationMessage(String messageField, String message, String notifyField) {
        boolean shouldNotify = !message.isEmpty();

        Map<String, Object> updates = new HashMap<>();
        updates.put(messageField, message);
        updates.put(notifyField, shouldNotify);

        db.collection("events").document(eventID)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Notification updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update notification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}


