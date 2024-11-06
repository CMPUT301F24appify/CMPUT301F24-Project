package com.example.appify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
//        event1 = new Event(this,"Event 1", "Oct 12", "1", "2 days", "Here is the event description", 20, 5, null, false, false, false , false, false);
//        event2 = new Event(this,"Event 2", "Nov 23", "2", "3 days", "Some description", 200, 50, null, true, false, false, false, false);
//        event3 = new Event(this,"Event 3", "Jan 01", "3", "4 days", "blha blah blha", 3, 1, null, true, false, false, false, false);
//
//
//        Event []events = {event1,event2, event3};
//
//        System.out.println("test1");
//        eventList = new ArrayList<>();
//        eventList.addAll(Arrays.asList(events));
//        System.out.println("test2");
        eventAdapter = new CustomEventAdapter(this,eventList);
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

            // Poster URI might be null, so check before passing
            String posterUriString = selectedEvent.getPosterUri() != null ? selectedEvent.getPosterUri() : "";
            intent.putExtra("posterUri", posterUriString);

            intent.putExtra("isGeolocate", selectedEvent.isGeolocate());
            intent.putExtra("notifyWaitlisted", selectedEvent.isNotifyWaitlisted());
            intent.putExtra("notifyEnrolled", selectedEvent.isNotifyEnrolled());
            intent.putExtra("notifyCancelled", selectedEvent.isNotifyCancelled());
            intent.putExtra("notifyInvited", selectedEvent.isNotifyInvited());


            startActivity(intent);
        });

    }

    @Override
    public void onEventAdded(String name, String date, String facility, String registrationEndDate, String description, int maxWishEntrants, int maxSampleEntrants, String posterUri, boolean isGeolocate, boolean notifyWaitlisted, boolean notifyEnrolled, boolean notifyCancelled, boolean notifyInvited) {
        Event newEvent = new Event(this,name, date, facility, registrationEndDate, description, maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate, notifyWaitlisted, notifyEnrolled, notifyCancelled, notifyInvited);



//        // Use the new method in Event
//        newEvent.addToFirestore(event -> {
//            Toast.makeText(EventActivity.this, "Event added: " + event.getName(), Toast.LENGTH_SHORT).show();
//            eventList.add(event);
//            eventAdapter.notifyDataSetChanged();
//        });
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



    private void loadEventsFromFirestore() {

        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("events")
                .whereEqualTo("organizerID", android_id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    eventList.clear();  // Clear existing data to avoid duplicates

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