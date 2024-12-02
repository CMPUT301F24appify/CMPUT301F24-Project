package com.example.appify.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
import com.example.appify.Adapters.CustomFolderAdapter;
import com.example.appify.HeaderNavigation;
import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.example.appify.Model.Facility;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private ArrayList<StorageReference> folderList;
    private CustomFolderAdapter folderAdapter;
    private LinearLayout noFacilityView;
    private LinearLayout noEventsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_page);

        MyApp app = (MyApp) getApplication();
        db = app.getFirebaseInstance();

        // Initialize the ListView
        facilityList = new ArrayList<>();
        facilityAdapter = new CustomFacilityAdapter(this, facilityList);
        eventList = new ArrayList<>();
        eventAdapter = new CustomEventAdapter(this, eventList,false, true);
        entrantList = new ArrayList<>();
        entrantAdapter = new CustomEntrantAdminAdapter(this, entrantList);
        folderList = new ArrayList<>();
        folderAdapter = new CustomFolderAdapter(this, folderList);
        listView = findViewById(R.id.admin_list);
        noFacilityView = findViewById(R.id.noFacilityView);
        noEventsView = findViewById(R.id.noEventsView);

        // HeaderNavigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();
        TextView facilitiesText = findViewById(R.id.adminText_navBar);
        facilitiesText.setTextColor(Color.parseColor("#000000"));
        facilitiesText.setTypeface(facilitiesText.getTypeface(), Typeface.BOLD);

        // Set up RadioGroup for toggling options
        RadioGroup toggleGroup = findViewById(R.id.toggle_group);
        toggleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.toggle_facilities) {
                listView.setAdapter(facilityAdapter);
                noEventsView.setVisibility(View.GONE);
                loadFacilitiesFromFirestore();
            } else if (checkedId == R.id.toggle_events) {
                listView.setAdapter(eventAdapter);
                noFacilityView.setVisibility(View.GONE);
                loadEventsFromFirestore();
            } else if (checkedId == R.id.toggle_profiles) {
                listView.setAdapter(entrantAdapter);
                noFacilityView.setVisibility(View.GONE);
                noEventsView.setVisibility(View.GONE);
                loadProfilesFromFirestore();
            } else if (checkedId == R.id.toggle_images) {
                listView.setAdapter(folderAdapter);
                noFacilityView.setVisibility(View.GONE);
                noEventsView.setVisibility(View.GONE);
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
                    noFacilityView.setVisibility(View.GONE);
                }
                facilityAdapter.notifyDataSetChanged();

                if (facilityList.isEmpty()){
                    noFacilityView.setVisibility(View.VISIBLE);
                }
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
                    String posterURI = doc.getString("posterUri");


                    Event event = new Event(name,date,facility,registrationEndDate,
                            maxWaitEntrants,maxSampleEntrants,organizerID);
                    event.setEventId(id);
                    event.setDescription(doc.getString("description"));
                    event.setPosterUri(posterURI);
                    event.setEventId(id);
                    eventList.add(event);
                    noEventsView.setVisibility(View.GONE);
                }
                eventAdapter.notifyDataSetChanged();

                if (eventList.isEmpty()){
                    noEventsView.setVisibility(View.VISIBLE);
                }
            }
        });
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
                    Double latitude = doc.getDouble("latitude");
                    Double longitude = doc.getDouble("longitude");

                    Entrant entrant = new Entrant(id,name,phoneNumber,email,profilePictureURL,notifications, facilityID);

                    if(latitude != null && longitude != null){
                        entrant.setLongitude(longitude);
                        entrant.setLatitude(latitude);
                    }
                    entrantList.add(entrant);
                }
                entrantAdapter.notifyDataSetChanged();
            }
        });
    }
    private void loadImagesFromFirestore() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference();
        ref.listAll()
                .addOnSuccessListener(result -> {
                    folderList.clear();
                    for (StorageReference folder : result.getPrefixes()) {
                        if(folder.getName().contains("qrcode") || folder.getName().contains("generated")) {
                            continue;
                        }
                        folderList.add(folder);
                    }
                    folderAdapter.notifyDataSetChanged();
                });
    }
}
