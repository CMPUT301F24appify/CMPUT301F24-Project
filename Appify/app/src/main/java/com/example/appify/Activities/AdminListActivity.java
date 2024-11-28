package com.example.appify.Activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Adapters.CustomEntrantAdapter;
import com.example.appify.Adapters.CustomEntrantAdminAdapter;
import com.example.appify.Adapters.CustomEventAdapter;
import com.example.appify.Adapters.CustomFacilityAdapter;
import com.example.appify.HeaderNavigation;
import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.example.appify.Model.Facility;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Displays a list of facilities, events, profiles, or images based on user selection.
 */
public class AdminListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ListView listView;
    private CustomFacilityAdapter  facilityAdapter; // Update to use appropriate adapters for other types
    private ArrayList<Facility> facilityList; // Update to handle other types (events, profiles, images)
    private ArrayList<Event> eventList;
    private CustomEventAdapter eventAdapter;
    private ArrayList<Entrant> entrantList;
    private CustomEntrantAdminAdapter entrantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_page);

        db = FirebaseFirestore.getInstance();

        // Initialize the ListView
        facilityList = new ArrayList<>();
        facilityAdapter = new CustomFacilityAdapter(this, facilityList);
        eventList = new ArrayList<>();
        eventAdapter = new CustomEventAdapter(this, eventList,false);
        entrantList = new ArrayList<>();
        entrantAdapter = new CustomEntrantAdminAdapter(this, entrantList);
        listView = findViewById(R.id.admin_list);

        // HeaderNavigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();
        TextView facilitiesText = findViewById(R.id.adminText_navBar);
        facilitiesText.setTextColor(Color.parseColor("#800080"));
        facilitiesText.setTypeface(facilitiesText.getTypeface(), Typeface.BOLD);

        // Set up RadioGroup for toggling options
        RadioGroup toggleGroup = findViewById(R.id.toggle_group);
        toggleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.toggle_facilities) {
                listView.setAdapter(facilityAdapter);
                loadFacilitiesFromFirestore();
            } else if (checkedId == R.id.toggle_events) {
                listView.setAdapter(eventAdapter);
                loadEventsFromFirestore();
            } else if (checkedId == R.id.toggle_profiles) {
                listView.setAdapter(entrantAdapter);
                loadProfilesFromFirestore();
            } else if (checkedId == R.id.toggle_images) {
                loadImagesFromFirestore();
            } else {
                Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show();
            }
        });

        // Load default selection (e.g., facilities)
        toggleGroup.check(R.id.toggle_facilities); // Default selection
    }

    private void loadFacilitiesFromFirestore() {
        CollectionReference facilitiesRef = db.collection("facilities");

        facilitiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                facilityList.clear(); // Clear the list to avoid duplicates
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String id = doc.getId();
                    String name = doc.getString("name");
                    String location = doc.getString("location");
                    String email = doc.getString("email");
                    String description = doc.getString("description");
                    int capacity = doc.getLong("capacity").intValue();
                    String organizerID = doc.getString("organizerID");

                    Facility facility = new Facility(id, name, location, email, description, capacity, organizerID);
                    facilityList.add(facility);
                }

                facilityAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadEventsFromFirestore() {
        CollectionReference eventsRef = db.collection("events");

        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String id = doc.getId();
                    String name = doc.getString("name");
                    String date = doc.getString("date");
                    String registrationEndDate = doc.getString("registrationEndDate");
                    String facility = doc.getString("facility");
                    int maxSampleEntrants = doc.getLong("maxSampleEntrants").intValue();
                    int maxWaitEntrants = doc.getLong("maxWaitEntrants").intValue();
                    String organizerID = doc.getString("organizerID");

                    Event event = new Event(name,date,facility,registrationEndDate,
                            maxWaitEntrants,maxSampleEntrants,organizerID);
                    event.setEventId(id);
                    eventList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }
        });
        Toast.makeText(this, "Loading events...", Toast.LENGTH_SHORT).show();
    }

    private void loadProfilesFromFirestore() {
        CollectionReference entrantRef = db.collection("AndroidID");

        entrantRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                entrantList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {

                    String id = doc.getId();
                    String name = doc.getString("name");
                    String facilityID = doc.getString("facilityID");
                    String email = doc.getString("email");
                    String phoneNumber = doc.getString("phoneNumber");
                    Boolean notifications = doc.getBoolean("notifications");
                    String profilePictureURL = doc.getString("profilePictureURL");

                    Entrant entrant = new Entrant(id,name,phoneNumber,email,profilePictureURL,notifications);
                    if(facilityID != null)
                        entrantList.add(entrant);
                }
                entrantAdapter.notifyDataSetChanged();
            }
        });
        Toast.makeText(this, "Loading profiles...", Toast.LENGTH_SHORT).show();
    }

    private void loadImagesFromFirestore() {
        // TODO: Implement functionality to load images from Firestore
        Toast.makeText(this, "Loading images...", Toast.LENGTH_SHORT).show();
    }
}
