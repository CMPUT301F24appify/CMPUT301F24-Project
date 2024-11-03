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

public class EventActivity extends AppCompatActivity implements AddEventDialogFragment.AddEventDialogListener {
    private ArrayList<Event> eventList = new ArrayList<>();
    private ArrayAdapter<Event> eventAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);

        db = FirebaseFirestore.getInstance();
        ListView eventListView = findViewById(R.id.event_list);
        eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
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

            // Poster URI might be null, so check before passing
            String posterUriString = selectedEvent.getPosterUri() != null ? selectedEvent.getPosterUri() : "";
            intent.putExtra("posterUri", posterUriString);

            intent.putExtra("isGeolocate", selectedEvent.isGeolocate());

            startActivity(intent);
        });

    }

    @Override
    public void onEventAdded(String name, String date, String description, int maxWishEntrants, int maxSampleEntrants, Uri posterUri, boolean isGeolocate) {
        Event newEvent = new Event(name, date, description, maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate);

        db.collection("events")
                .document(name)
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
