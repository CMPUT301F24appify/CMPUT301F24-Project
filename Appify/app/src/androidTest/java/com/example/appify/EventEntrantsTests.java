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

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EventEntrantsTests {

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
        testEntrant = new Entrant(entrantID, "Test User", "1234567890", "testuser@example.com", "profilePicUrl", true);

        // Prepopulate the waiting list with an "invited" status for the test entrant
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.document(entrantID).set(Collections.singletonMap("status", "invited"));
    }

    // US 02.02.01: View Waiting List Entrants
    @Test
    public void testViewWaitingListEntrants() {
        ArrayList<Entrant> waitingListEntrants = new ArrayList<>();
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.whereEqualTo("status", "waitingList").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    Entrant entrant = document.toObject(Entrant.class);
                    waitingListEntrants.add(entrant);
                }
                assertTrue("Waiting list should contain entrants", waitingListEntrants.size() > 0);
            }
        });
    }

    // US 02.06.01: View Invited Entrants
    @Test
    public void testViewInvitedEntrants() {
        ArrayList<Entrant> invitedEntrants = new ArrayList<>();
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.whereEqualTo("status", "invited").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    Entrant entrant = document.toObject(Entrant.class);
                    invitedEntrants.add(entrant);
                }
                assertTrue("Invited list should contain entrants", invitedEntrants.size() > 0);
            }
        });
    }

    // US 02.06.02: View Cancelled Entrants
    @Test
    public void testViewCancelledEntrants() {
        ArrayList<Entrant> cancelledEntrants = new ArrayList<>();
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.whereEqualTo("status", "cancelled").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    Entrant entrant = document.toObject(Entrant.class);
                    cancelledEntrants.add(entrant);
                }
                assertTrue("Cancelled list should contain entrants", cancelledEntrants.size() > 0);
            }
        });
    }

    // US 02.06.03: View Enrolled Entrants
    @Test
    public void testViewEnrolledEntrants() {
        ArrayList<Entrant> enrolledEntrants = new ArrayList<>();
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.whereEqualTo("status", "enrolled").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    Entrant entrant = document.toObject(Entrant.class);
                    enrolledEntrants.add(entrant);
                }
                assertTrue("Enrolled list should contain entrants", enrolledEntrants.size() > 0);
            }
        });
    }

    // US 02.06.04: Cancel Non-Signup Entrants
    @Test
    public void testCancelNonSignupEntrants() {
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.whereEqualTo("status", "pending").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    document.getReference().update("status", "cancelled");
                }
                waitingListRef.whereEqualTo("status", "cancelled").get().addOnCompleteListener(task2 -> {
                    assertEquals("All non-signup entrants should be cancelled", task2.getResult().size(), task.getResult().size());
                });
            }
        });
    }

    // US 01.06.01: View Event Details via QR Code
    @Test
    public void testViewEventDetailsViaQRCode() {
        String testQRData = "test_event_id"; // Mock QR code scan data

        db.collection("events").document(testQRData).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Event event = task.getResult().toObject(Event.class);
                assertNotNull("Event details should be retrieved from QR code", event);
            }
        });
    }

}
