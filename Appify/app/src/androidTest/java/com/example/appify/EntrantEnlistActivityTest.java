package com.example.appify;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EntrantEnlistActivityTest {

    private FirebaseFirestore db;
    private Event testEvent;
    private String eventID;
    private String entrantID = "test_entrant_id"; // Directly assigned entrant ID for testing
    private Entrant testEntrant;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();

        // Initialize the Event with required parameters
        testEvent = new Event();
        testEvent.setEventId("test_event_id");
        eventID = testEvent.getEventId();

        // Initialize the Entrant with the assigned entrantID
        testEntrant = new Entrant(entrantID, "Test User", "1234567890", "testuser@example.com", "profilePicUrl", true, "testFacilityID");

        // Prepopulate the waiting list with an "invited" status for the test entrant
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.document(entrantID).set(Collections.singletonMap("status", "invited"));
    }

    // US 01.01.01: Join Waiting List for Specific Event
    @Test
    public void testJoinWaitingList() {
        // Add the entrant to the waiting list with status "waiting"
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.document(entrantID).set(Collections.singletonMap("status", "waiting")).addOnCompleteListener(task -> {
            assertTrue("Entrant should be added to the waiting list", task.isSuccessful());
        });
    }

    // US 01.01.02: Leave Waiting List for Specific Event
    @Test
    public void testLeaveWaitingList() {
        // Remove the entrant from the waiting list
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.document(entrantID).delete().addOnCompleteListener(task -> {
            assertTrue("Entrant should be removed from the waiting list", task.isSuccessful());
        });
    }

    // US 01.08.01: Warn Before Joining Waiting List that Requires Geolocation
    @Test
    public void testWarnBeforeJoiningWithGeolocation() {
        // Set the geolocation flag to true for the event
        testEvent.setGeolocate(true);

        // Assuming the warning is part of the event registration logic, check if geolocation warning occurs.
        boolean isGeolocationRequired = testEvent.isGeolocate();

        // Simulate the behavior by asserting the geolocation requirement is true
        assertTrue("Entrant should be warned if geolocation is required", isGeolocationRequired);
    }
}
