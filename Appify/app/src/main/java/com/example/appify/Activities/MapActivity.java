package com.example.appify.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * MapActivity displays a map with markers representing the locations of entrants for a specific event.
 * The map uses Google Maps API and retrieves location data from Firestore.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseFirestore db; // Firestore instance for database interactions
    private GoogleMap gglMap; // GoogleMap object for displaying the map
    private FrameLayout map; // Layout containing the map fragment
    private Button backButton; // Button to navigate back to the EventDetailActivity
    private CollectionReference waitingListRef; // Reference to the waiting list collection for an event
    private final List<Marker> markerList = new ArrayList<>(); // List to store markers displayed on the map

    /**
     * Called when the activity is created. Initializes the UI components, sets up the map fragment,
     * and retrieves event details to display entrant locations.
     *
     * @param savedInstanceState The saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Firestore instance
        MyApp app = (MyApp) getApplication();
        db = app.getFirebaseInstance();
        Intent intent = getIntent();

        // Retrieve event details from the intent
        String eventID = intent.getStringExtra("eventID");
        String name = intent.getStringExtra("name");
        String date = intent.getStringExtra("date");
        String facility = intent.getStringExtra("facility");
        String registrationEndDate = intent.getStringExtra("registrationEndDate");
        String description = intent.getStringExtra("description");
        int maxWaitEntrants = intent.getIntExtra("maxWaitEntrants", 0);
        int maxSampleEntrants = intent.getIntExtra("maxSampleEntrants", 0);
        String posterUriString = intent.getStringExtra("posterUri");
        boolean isGeolocate = intent.getBooleanExtra("isGeolocate", false);

        // Set up the back button to navigate to EventDetailActivity
        backButton = findViewById(R.id.buttonBackToEventsDetail);
        backButton.setOnClickListener(v -> {
            Intent sendIntent = new Intent(MapActivity.this, EventDetailActivity.class);
            sendIntent.putExtra("name", name);
            sendIntent.putExtra("date", date);
            sendIntent.putExtra("facility", facility);
            sendIntent.putExtra("registrationEndDate", registrationEndDate);
            sendIntent.putExtra("description", description);
            sendIntent.putExtra("maxWaitEntrants", maxWaitEntrants);
            sendIntent.putExtra("maxSampleEntrants", maxSampleEntrants);
            sendIntent.putExtra("eventID", eventID);
            sendIntent.putExtra("posterUri", posterUriString);
            sendIntent.putExtra("isGeolocate", isGeolocate);
            startActivity(sendIntent);
        });

        map = findViewById(R.id.map);

        // Set up the map fragment and initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }


    /**
     * Called when the Google Map is ready to use. Sets the default camera position and
     * loads entrant data for the specified event.
     *
     * @param googleMap The GoogleMap instance.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gglMap = googleMap;

        // Setting default position for Map Camera to open to, for convenience sakes
        LatLng canadaLatLng = new LatLng(56.1304, -106.3468);
        this.gglMap.moveCamera(CameraUpdateFactory.newLatLngZoom(canadaLatLng, 4.0f));

        // Call reloadData here, now that gglMap is initialized
        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");
        if (eventID != null) {
            reloadData(eventID);
        }
    }

    /**
     * Retrieves entrant data for the specified event and places markers on the map
     * for each entrant with valid location coordinates.
     *
     * @param eventID The ID of the event for which to display entrant locations.
     */
    public void reloadData(String eventID) {
        gglMap.clear();
        markerList.clear();

        // Reference the waiting list collection for the event
        waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Setup the adapter AFTER firebase retrieves all the data, by checking when all tasks are complete
                int[] tasksCompleted = {0};
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // For each user in the waiting list, get their details from the "AndroidID" collection
                    String userID = document.getId();

                    // Access each entrant in the waiting list for this event.
                    db.collection("AndroidID").document(userID).get()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && task2.getResult() != null) {
                                    DocumentSnapshot entrantData = task2.getResult();
                                    String entrantName = entrantData.get("name").toString();
                                    Double latitude = entrantData.getDouble("latitude");
                                    Double longitude = entrantData.getDouble("longitude");

                                    if (latitude != null && longitude != null) {
                                        LatLng entrantLocation = new LatLng(latitude, longitude);
                                        Marker marker = gglMap.addMarker(new MarkerOptions()
                                                .position(entrantLocation)
                                                .title("Entrant: " + entrantName)); // Customize title as needed
                                        markerList.add(marker); // Add the marker to the list
                                    }

                                }
                                tasksCompleted[0]++;
                            });

                }

            }
        });
    }
}

