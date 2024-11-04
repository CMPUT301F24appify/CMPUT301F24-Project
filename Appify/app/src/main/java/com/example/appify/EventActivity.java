package com.example.appify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventActivity extends AppCompatActivity implements AddEventDialogFragment.AddEventDialogListener {
    private FirebaseFirestore db;
    ListView eventListView;
    CustomEventAdapter eventAdapter;
    ArrayList<Event> eventList;

    // Sample events
    Event event1;
    Event event2;
    Event event3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);

        db = FirebaseFirestore.getInstance();

        eventListView = findViewById(R.id.event_list);

        // Sample events
        event1 = new Event(this,"Event 1", "Oct 12", "Here is the event description", 20, 5, null, false);
        event2 = new Event(this,"Event 2", "Nov 23", "Some description", 200, 50, null, true);
        event3 = new Event(this,"Event 3", "Jan 01", "blha blah blha", 3, 1, null, true);


        Event []events = {event1,event2, event3};

        System.out.println("test1");
        eventList = new ArrayList<>();
        eventList.addAll(Arrays.asList(events));
        System.out.println("test2");
        eventAdapter = new CustomEventAdapter(this,eventList);
        eventListView.setAdapter(eventAdapter);

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
            intent.putExtra("description", selectedEvent.getDescription() != null ? selectedEvent.getDescription() : "N/A");
            intent.putExtra("maxWishEntrants", selectedEvent.getMaxWishEntrants());
            intent.putExtra("maxSampleEntrants", selectedEvent.getMaxSampleEntrants());
            intent.putExtra("eventID", selectedEvent.getEventId());


            // Poster URI might be null, so check before passing
            String posterUriString = selectedEvent.getPosterUri() != null ? selectedEvent.getPosterUri() : "";
            intent.putExtra("posterUri", posterUriString);

            intent.putExtra("isGeolocate", selectedEvent.isGeolocate());

            startActivity(intent);
        });

    }

    @Override
    public void onEventAdded(String name, String date, String description, int maxWishEntrants, int maxSampleEntrants, Uri posterUri, boolean isGeolocate) {
        Event newEvent = new Event(this,name, date, description, maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate);
        String eventID = newEvent.getEventId();
        db.collection("events")
                .document(eventID)
                .set(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EventActivity.this, "Event added: " + name, Toast.LENGTH_SHORT).show();
                    eventList.add(newEvent);
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventActivity.this, "Error adding event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}