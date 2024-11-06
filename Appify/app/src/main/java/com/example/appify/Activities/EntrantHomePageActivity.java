package com.example.appify.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Adapters.CustomEventAdapter;
import com.example.appify.Model.Event;
import com.example.appify.HeaderNavigation;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * The EntrantHomePageActivity class displays a list of events available to entrants.
 * It fetches event data from Firestore, populates a ListView using a custom adapter,
 * and handles user interactions such as clicking on an event to view details.
 */
public class EntrantHomePageActivity extends AppCompatActivity {
    // Variables
    private FirebaseFirestore db;
    ListView eventListView;
    CustomEventAdapter eventAdapter;
    ArrayList<Event> eventList;


    /**
     * Called when the activity is starting. Initializes UI components and loads event data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, then this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_home_page);

        db = FirebaseFirestore.getInstance();

        // Initialize the ListView and the Adapter
        eventList = new ArrayList<>();
        eventAdapter = new CustomEventAdapter(this, eventList);
        eventListView = findViewById(R.id.home_events);
        eventListView.setAdapter(eventAdapter);

        // HeaderNavigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();

        // Load Events from Firebase
        loadEventsFromFirestore();
        String android_id = getIntent().getStringExtra("Android ID");

        // OnClickListener for the Events in the ListView
        eventListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Event selectedEvent = eventList.get(position);

            Intent intent = new Intent(EntrantHomePageActivity.this, EntrantEnlistActivity.class);

            intent.putExtra("eventId", selectedEvent.getEventId());
            intent.putExtra("name", selectedEvent.getName());
            intent.putExtra("date", selectedEvent.getDate());
            intent.putExtra("registrationEndDate", selectedEvent.getRegistrationEndDate());
            intent.putExtra("facility", selectedEvent.getFacility());
            intent.putExtra("description", selectedEvent.getDescription());
            intent.putExtra("maxWishEntrants", selectedEvent.getMaxWishEntrants());
            intent.putExtra("maxSampleEntrants", selectedEvent.getMaxSampleEntrants());
            intent.putExtra("posterUri", selectedEvent.getPosterUri());
            intent.putExtra("geolocate", selectedEvent.isGeolocate());

            startActivity(intent);
        });
    }

    /**
     * Loads events from the Firestore database and updates the ListView.
     * Retrieves documents from the "events" collection, creates {@link Event} objects,
     * and notifies the adapter to refresh the ListView.
     */
    private void loadEventsFromFirestore() {
        CollectionReference eventsRef = db.collection("events");

        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventList.clear(); // Clear the list to avoid duplicates
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Use the fromFirestore method to create an Event object
                    Event event = Event.fromFirestore(document);
                    eventList.add(event);
                }
                // Notify the adapter that data has changed
                eventAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(EntrantHomePageActivity.this, "Error getting events.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
