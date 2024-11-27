package com.example.appify.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.appify.Adapters.CustomEntrantAdapter;
import com.example.appify.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseFirestore db;
    private GoogleMap gglMap;
    private FrameLayout map;
    private Button backButton;
    CollectionReference waitingListRef;
//    private String eventID;

    private final List<Marker> markerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Reload data for the map
//        reloadData(eventID);


    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gglMap = googleMap;

        LatLng mapCanada = new LatLng(56.1304, -106.3468);
        Marker marker = this.gglMap.addMarker(new MarkerOptions().position(mapCanada).title("Marker in Canada"));
        this.gglMap.moveCamera(CameraUpdateFactory.newLatLng(mapCanada));
        markerList.add(marker);

        // Call reloadData here, now that gglMap is initialized
        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");
        if (eventID != null) {
            reloadData(eventID);
        }


    }

    public void reloadData(String eventID) {
        gglMap.clear();
        markerList.clear();

        waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Setup the adapter AFTER firebase retrieves all the data, by checking when all tasks are complete
                int[] tasksCompleted = {0};
                int totalTasks = task.getResult().size();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // For each user in the waiting list, get their details from the "AndroidID" collection
                    String userID = document.getId();
                    Object waitingListStatus = document.get("status");

                    // Access each entrant in the waiting list for this event.
                    db.collection("AndroidID").document(userID).get()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && task2.getResult() != null) {
                                    DocumentSnapshot entrantData = task2.getResult();
                                    String entrantID = userID;
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

                                } else {
                                    System.out.println("Error getting AndroidID document for " + userID + ": " + task2.getException());
                                }
                                tasksCompleted[0]++;

//                                if (tasksCompleted[0] == totalTasks) {
                                    // All tasks complete, set up adapter
//                                    return;
//                                }
                            });

                }

            } else {
                System.out.println("Error getting documents: " + task.getException());
            }
        });
    }

    private void adjustCameraToMarkers() {
        if (markerList.isEmpty()) return;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Marker marker : markerList) { // Use gglMap's marker list if available
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();
        int padding = 100; // Adjust padding as needed
        gglMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }


}

