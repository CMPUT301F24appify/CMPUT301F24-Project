package com.example.appify.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Adapters.CustomEventAdapter;
import com.example.appify.Fragments.AddEventDialogFragment;
import com.example.appify.HeaderNavigation;
import com.example.appify.Model.Event;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * EventActivity manages the displaying and interactions of organizer events.
 * It displays a list of events, allows the organizer to add a new event, and navigate to event management and detail screens.
 */
public class EventActivity extends AppCompatActivity implements AddEventDialogFragment.AddEventDialogListener {

    private FirebaseFirestore db; // Instance of Firebase Firestore for database operations
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1; // Request code for permissions
    ListView eventListView; // ListView to display organizer's events
    CustomEventAdapter eventAdapter; // Custom adapter to populate event list
    ArrayList<Event> eventList = new ArrayList<>(); // List to store Event objects
    String facilityName; // Name of the organizer's facility
    String facilityID; // ID of the organizer's facility
    LinearLayout noCreatedEventsLayout; // Layout displayed when no events are created

    /**
     * Initializes the EventActivity, setting up the user interface, loading events from Firestore,
     * and setting up navigation and click listeners.
     *
     * @param savedInstanceState The previously saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);

        // Setup header navigation and highlight the 'Organize' tab
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();
        TextView organizeText = findViewById(R.id.organizeText_navBar);
        organizeText.setTextColor(Color.parseColor("#000000"));
        organizeText.setTypeface(organizeText.getTypeface(), Typeface.BOLD);

        // Initialize layout components
        noCreatedEventsLayout = findViewById(R.id.noCreatedEventsLayout);
        MyApp app = (MyApp) getApplication();
        db = app.getFirebaseInstance();

        // Setup event ListView and its adapter
        eventListView = findViewById(R.id.event_list);
        eventAdapter = new CustomEventAdapter(this, eventList, true, false);
        eventListView.setAdapter(eventAdapter);

        // Load events from Firestore
        loadEventsFromFirestore();

        // Set up 'Add Event' button listener to open the AddEventDialogFragment
        Button addEventButton = findViewById(R.id.button_add);
        addEventButton.setOnClickListener(v -> {
            AddEventDialogFragment dialog = new AddEventDialogFragment();
            dialog.show(getSupportFragmentManager(), "AddEventDialogFragment");
        });

        // Set up 'Manage Facility' button listener to navigate to ManageFacilityActivity
        Button manageFacilityButton = findViewById(R.id.button_manage);
        manageFacilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventActivity.this, ManageFacilityActivity.class);
            startActivity(intent);
        });

        // Fetch facility details from Firestore
        String androidId = app.getAndroidId();
        db.collection("AndroidID").document(androidId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            facilityID = documentSnapshot.getString("facilityID");
                            db.collection("facilities").document(facilityID).get()
                                    .addOnSuccessListener(documentSnapshot1 ->{
                                        if (documentSnapshot1.exists()) {
                                            facilityName = documentSnapshot1.getString("name");
                                        }
                                    });
                        });

        // Set up item click listener for eventListView to navigate to EventDetailActivity
        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventList.get(position);
            Intent intent = new Intent(EventActivity.this, EventDetailActivity.class);

            // Pass event details with null checks to prevent crashes
            intent.putExtra("facility", facilityName != null ? facilityName : "N/A");
            intent.putExtra("name", selectedEvent.getName() != null ? selectedEvent.getName() : "N/A");
            intent.putExtra("date", selectedEvent.getDate() != null ? selectedEvent.getDate() : "N/A");
            intent.putExtra("registrationEndDate", selectedEvent.getRegistrationEndDate() != null ? selectedEvent.getRegistrationEndDate() : "N/A");
            intent.putExtra("description", selectedEvent.getDescription() != null ? selectedEvent.getDescription() : "N/A");
            intent.putExtra("maxWaitEntrants", selectedEvent.getMaxWaitEntrants());
            intent.putExtra("maxSampleEntrants", selectedEvent.getMaxSampleEntrants());
            intent.putExtra("eventID", selectedEvent.getEventId() != null ? selectedEvent.getEventId() : "N/A");

            // Check for null posterUri before passing
            String posterUriString = selectedEvent.getPosterUri() != null ? selectedEvent.getPosterUri() : "";
            intent.putExtra("posterUri", posterUriString);

            intent.putExtra("isGeolocate", selectedEvent.isGeolocate());
            intent.putExtra("notifyWaitlisted", selectedEvent.isNotifyWaitlisted());
            intent.putExtra("notifyEnrolled", selectedEvent.isNotifyEnrolled());
            intent.putExtra("notifyCancelled", selectedEvent.isNotifyCancelled());
            intent.putExtra("notifyInvited", selectedEvent.isNotifyInvited());

            // Pass notification messages with null checks
            intent.putExtra("waitlistedMessage", selectedEvent.getWaitlistedMessage() != null ? selectedEvent.getWaitlistedMessage() : "");
            intent.putExtra("enrolledMessage", selectedEvent.getEnrolledMessage() != null ? selectedEvent.getEnrolledMessage() : "");
            intent.putExtra("cancelledMessage", selectedEvent.getCancelledMessage() != null ? selectedEvent.getCancelledMessage() : "");
            intent.putExtra("invitedMessage", selectedEvent.getInvitedMessage() != null ? selectedEvent.getInvitedMessage() : "");

            startActivity(intent);
        });
    }

    /**
     * Callback method for adding a new event through the AddEventDialogFragment.
     * Adds the new event to Firestore and updates the event list on success.
     *
     * @param name              Name of the event.
     * @param date              Date of the event.
     * @param facility          Facility where the event takes place.
     * @param registrationEndDate Registration end date for the event.
     * @param description       Description of the event.
     * @param maxWaitEntrants   Maximum number of waitlist entrants allowed.
     * @param maxSampleEntrants Maximum number of sample entrants allowed.
     * @param posterUri         URI of the event's poster.
     * @param isGeolocate       Indicates if geolocation is enabled for the event.
     * @param waitlistedMessage Notification message for waitlisted entrants.
     * @param enrolledMessage   Notification message for enrolled entrants.
     * @param cancelledMessage  Notification message for cancelled entrants.
     * @param invitedMessage    Notification message for invited entrants.
     */
    @Override
    public void onEventAdded(String name, String date, String facility, String registrationEndDate,
                             String description, int maxWaitEntrants, int maxSampleEntrants,
                             String posterUri, boolean isGeolocate,
                             String waitlistedMessage, String enrolledMessage,
                             String cancelledMessage, String invitedMessage) {

        MyApp app = (MyApp) getApplication();
        String organizerID = app.getAndroidId();

        // Create new Event object with details from the dialog
        Event newEvent = new Event(name, date, facility, registrationEndDate, description,
                maxWaitEntrants, maxSampleEntrants, posterUri, isGeolocate,
                false, false, false, false,
                "", "", "", "",
                organizerID);

        // Add the new event to Firestore and update the local list on success
        newEvent.addToFirestore(event -> {
            Toast.makeText(EventActivity.this, "Event added: " + event.getName(), Toast.LENGTH_SHORT).show();
            eventList.add(event);
            noCreatedEventsLayout.setVisibility(View.GONE);
            eventAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Loads events from Firestore based on the organizer's ID and populates the event list.
     * Clears the existing list to prevent duplicate entries before fetching data.
     */
    private void loadEventsFromFirestore() {
        MyApp app = (MyApp) getApplication();
        String organizerID = app.getAndroidId();

        // Clear the existing list to avoid duplicate entries
        eventList.clear();

        db.collection("events")
                .whereEqualTo("organizerID", organizerID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Iterate through Firestore documents and add them to the event list
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                        System.out.println("yo");
                        noCreatedEventsLayout.setVisibility(View.GONE);
                    }

                    // Notify the adapter of data changes
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
