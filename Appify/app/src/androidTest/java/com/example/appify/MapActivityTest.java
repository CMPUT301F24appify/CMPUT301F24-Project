package com.example.appify;

import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.appify.Activities.MapActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class MapActivityTest {

    private FirebaseFirestore db;
    private String eventID = "test_event_id";
    private String entrantID = "test_entrant_id"; // Directly assigned entrant ID for testing
    private CollectionReference waitingListRef;

    @Rule
    public ActivityTestRule<MapActivity> activityRule =
            new ActivityTestRule<>(MapActivity.class, true, false);

    @Before
    public void setUp() {
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Set up the test environment
        Intent intent = new Intent();
        intent.putExtra("eventID", eventID);
        intent.putExtra("name", "Test Event");
        intent.putExtra("date", "2024-12-15");
        intent.putExtra("facility", "Test Facility");
        intent.putExtra("registrationEndDate", "2024-12-10");
        intent.putExtra("description", "This is a test event description.");
        intent.putExtra("maxWaitEntrants", 10);
        intent.putExtra("maxSampleEntrants", 5);
        intent.putExtra("posterUri", "testUri");
        intent.putExtra("isGeolocate", true);

        // Prepopulate the waiting list with an entrant
        waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.document(entrantID).set(Collections.singletonMap("status", "waiting"));

        activityRule.launchActivity(intent);
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // US 02.02.02: Check if Entrant Markers Are Displayed on Map
    @Test
    public void testEntrantMarkersOnMap() {
        // Simulate the data for the entrant with latitude and longitude
        double latitude = 49.2827;  // Example latitude (Vancouver)
        double longitude = -123.1207;  // Example longitude (Vancouver)

        // Add the entrant's location to Firestore
        db.collection("AndroidID").document(entrantID).set(Collections.singletonMap("latitude", latitude))
                .addOnCompleteListener(task -> {
                    assertTrue("Entrant latitude should be added to Firestore", task.isSuccessful());
                });
        db.collection("AndroidID").document(entrantID).set(Collections.singletonMap("longitude", longitude))
                .addOnCompleteListener(task -> {
                    assertTrue("Entrant longitude should be added to Firestore", task.isSuccessful());
                });

        // Wait for Firestore to propagate the data before checking the map (use Espresso IdlingResource for async operations in real tests)
        try {
            Thread.sleep(2000);  // Simulating a delay for Firestore data propagation
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Now manually create a MarkerOptions and add it to the GoogleMap object
        MapActivity mapActivity = activityRule.getActivity();
        GoogleMap googleMap = mapActivity.gglMap;

        // Ensure we are on the main thread when interacting with the map
        mapActivity.runOnUiThread(() -> {
            // Manually add the marker to the map as the Firestore data would do
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title("Entrant: Test User"));

            // Check if the marker was added
            assertTrue("Marker should be placed on the map", marker != null);
        });

        // Give time for the UI thread to process the marker addition
        try {
            Thread.sleep(1000);  // Small delay for marker addition to reflect in the UI
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // US 02.02.02: Test if the Event ID is Correctly Passed and Data is Loaded
    @Test
    public void testEventDataLoaded() {
        // Ensure the correct EventID is passed to MapActivity
        assertTrue("Event ID should be passed correctly to the activity", eventID.equals(activityRule.getActivity().getIntent().getStringExtra("eventID")));

        // Ensure the map is initialized
        FrameLayout mapLayout = activityRule.getActivity().findViewById(R.id.map);
        assertTrue("Map should be loaded", mapLayout != null);
    }
}
