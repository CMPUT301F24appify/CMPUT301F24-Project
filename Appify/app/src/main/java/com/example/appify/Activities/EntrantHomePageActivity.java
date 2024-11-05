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

public class EntrantHomePageActivity extends AppCompatActivity {
    // Variables
    private FirebaseFirestore db;
    ListView eventListView;
    CustomEventAdapter eventAdapter;
    ArrayList<Event> eventList;



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
            intent.putExtra("isGeolocate", selectedEvent.isGeolocate());

            startActivity(intent);
        });
    }

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
