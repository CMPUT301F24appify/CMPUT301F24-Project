package com.example.appify.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Adapters.CustomEventAdapter;
import com.example.appify.AddEventDialogFragment;
import com.example.appify.HeaderNavigation;
import com.example.appify.Model.Event;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventActivity extends AppCompatActivity implements AddEventDialogFragment.AddEventDialogListener {
    private FirebaseFirestore db;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    ListView eventListView;
    CustomEventAdapter eventAdapter;
    ArrayList<Event> eventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);


        // HeaderNavigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();
        TextView organizeText = findViewById(R.id.organizeText_navBar);
        organizeText.setTextColor(Color.parseColor("#800080"));
        organizeText.setTypeface(organizeText.getTypeface(), Typeface.BOLD);


        db = FirebaseFirestore.getInstance();

        eventListView = findViewById(R.id.event_list);
        eventAdapter = new CustomEventAdapter(this, eventList);
        eventListView.setAdapter(eventAdapter);

        loadEventsFromFirestore();

        Button addEventButton = findViewById(R.id.button_add);
        addEventButton.setOnClickListener(v -> {
            AddEventDialogFragment dialog = new AddEventDialogFragment();
            dialog.show(getSupportFragmentManager(), "AddEventDialogFragment");
        });

        // Set an item click listener to open the EventDetailActivity
        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventList.get(position);

            Intent intent = new Intent(EventActivity.this, EventDetailActivity.class);

            // Add extra checks for null values to prevent crashes
            intent.putExtra("name", selectedEvent.getName() != null ? selectedEvent.getName() : "N/A");
            intent.putExtra("date", selectedEvent.getDate() != null ? selectedEvent.getDate() : "N/A");
            intent.putExtra("facility", selectedEvent.getFacility() != null ? selectedEvent.getFacility() : "N/A");
            intent.putExtra("registrationEndDate", selectedEvent.getRegistrationEndDate() != null ? selectedEvent.getRegistrationEndDate() : "N/A");
            intent.putExtra("description", selectedEvent.getDescription() != null ? selectedEvent.getDescription() : "N/A");
            intent.putExtra("maxWishEntrants", selectedEvent.getMaxWishEntrants());
            intent.putExtra("maxSampleEntrants", selectedEvent.getMaxSampleEntrants());
            intent.putExtra("eventID", selectedEvent.getEventId());

            // Poster URI might be null, so check before passing
            String posterUriString = selectedEvent.getPosterUri() != null ? selectedEvent.getPosterUri() : "";
            intent.putExtra("posterUri", posterUriString);

            intent.putExtra("isGeolocate", selectedEvent.isGeolocate());
            intent.putExtra("notifyWaitlisted", selectedEvent.isNotifyWaitlisted());
            intent.putExtra("notifyEnrolled", selectedEvent.isNotifyEnrolled());
            intent.putExtra("notifyCancelled", selectedEvent.isNotifyCancelled());
            intent.putExtra("notifyInvited", selectedEvent.isNotifyInvited());

            // Pass notification messages
            intent.putExtra("waitlistedMessage", selectedEvent.getWaitlistedMessage());
            intent.putExtra("enrolledMessage", selectedEvent.getEnrolledMessage());
            intent.putExtra("cancelledMessage", selectedEvent.getCancelledMessage());
            intent.putExtra("invitedMessage", selectedEvent.getInvitedMessage());

            startActivity(intent);
        });
    }

    @Override
    public void onEventAdded(String name, String date, String facility, String registrationEndDate,
                             String description, int maxWishEntrants, int maxSampleEntrants,
                             String posterUri, boolean isGeolocate,
                             String waitlistedMessage, String enrolledMessage,
                             String cancelledMessage, String invitedMessage) {

        MyApp app = (MyApp) getApplication();
        String organizerID = app.getAndroidId();

        // Determine notification booleans based on message presence
//        boolean notifyWaitlisted = waitlistedMessage != null && !waitlistedMessage.isEmpty();
//        boolean notifyEnrolled = enrolledMessage != null && !enrolledMessage.isEmpty();
//        boolean notifyCancelled = cancelledMessage != null && !cancelledMessage.isEmpty();
//        boolean notifyInvited = invitedMessage != null && !invitedMessage.isEmpty();

        // Create new Event object with notification messages
        Event newEvent = new Event(name, date, facility, registrationEndDate, description,
                maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate,
                false, false, false, false,
                "", "", "", "",
                organizerID);

        // Use the addToFirestore method in Event
        newEvent.addToFirestore(event -> {
            Toast.makeText(EventActivity.this, "Event added: " + event.getName(), Toast.LENGTH_SHORT).show();
            eventList.add(event);
            eventAdapter.notifyDataSetChanged();
        });
    }

    private void loadEventsFromFirestore() {
        MyApp app = (MyApp) getApplication();
        String organizerID = app.getAndroidId();

        db.collection("events")
                .whereEqualTo("organizerID", organizerID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // eventList.clear();  // Clear existing data to avoid duplicates

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Convert Firestore document to an Event object
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                    }

                    eventAdapter.notifyDataSetChanged();  // Notify the adapter of data changes
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
