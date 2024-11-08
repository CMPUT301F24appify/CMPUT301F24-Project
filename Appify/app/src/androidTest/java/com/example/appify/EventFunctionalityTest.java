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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EventFunctionalityTest {

    private FirebaseFirestore db;
    private Event testEvent;
    private String eventID;
    private Entrant testEntrant;
    private String entrantID;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();

        // Initialize the Event with required parameters
        testEvent = new Event();
        testEvent.setEventId("test_event_id");
        eventID = testEvent.getEventId();
        entrantID = "test_entrant_id";
        testEntrant = new Entrant(entrantID, "Test User", "1234567890", "testuser@example.com", "profilePicUrl", true);
        // Prepopulate the waiting list with an "invited" status for the test entrant
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.document(entrantID).set(Collections.singletonMap("status", "invited"));
    }

    // User Story: Limit Entrants on Waiting List
    @Test
    public void testAddEntrantsBeyondLimit() {
        testEvent.setMaxWaitEntrants(3);

        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.document("entrant1").set(Collections.singletonMap("status", "waitlisted"));
        waitingListRef.document("entrant2").set(Collections.singletonMap("status", "waitlisted"));
        waitingListRef.document("entrant3").set(Collections.singletonMap("status", "waitlisted"));
        waitingListRef.document("entrant4").set(Collections.singletonMap("status", "waitlisted"));

        waitingListRef.whereEqualTo("status", "waitlisted").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        assertTrue("Entrants should not exceed maxWaitEntrants", task.getResult().size() <= 3);
                    }
                });
    }

    // User Story: Upload and Update Event Poster
    @Test
    public void testPosterUriIsRequired() {
        testEvent.setPosterUri(null);

        testEvent.addToFirestore(event -> {
            assertNull("Poster URI should be required and cannot be null", event.getPosterUri());
        });
    }

    @Test
    public void testEventPosterUpdate() {
        String initialPosterUri = "https://example.com/initial_poster.jpg";
        String updatedPosterUri = "https://example.com/updated_poster.jpg";
        testEvent.setPosterUri(initialPosterUri);

        testEvent.addToFirestore(event -> {
            assertNotNull("Event ID should not be null", event.getEventId());

            event.setPosterUri(updatedPosterUri);
            db.collection("events").document(event.getEventId()).update("posterUri", updatedPosterUri)
                    .addOnSuccessListener(aVoid -> {
                        db.collection("events").document(event.getEventId()).get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String savedUri = task.getResult().getString("posterUri");
                                        assertEquals("Poster URI should be updated in Firestore", updatedPosterUri, savedUri);
                                    }
                                });
                    });
        });
    }

    // User Story: Sample Specified Number of Attendees
    @Test
    public void testConsistentSamplingWithLottery() {
        testEvent.setMaxSampleEntrants(3);

        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.document("entrant1").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant2").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant3").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant4").set(Collections.singletonMap("status", "enrolled"));

        for (int i = 0; i < 5; i++) {
            testEvent.lottery(db, eventID);
        }

        waitingListRef.whereEqualTo("status", "invited").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        assertTrue("Sample size should not exceed maxSampleEntrants", task.getResult().size() <= 3);
                    }
                });
    }

    @Test
    public void testNoEntrantsAddedIfLimitIsZero() {
        testEvent.setMaxWaitEntrants(0);

        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        waitingListRef.document("entrant1").set(Collections.singletonMap("status", "waitlisted"));

        waitingListRef.whereEqualTo("status", "waitlisted").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        assertEquals("No entrants should be added if maxWaitEntrants is set to zero", 0, task.getResult().size());
                    }
                });
    }

    // Tests for accept/decline functionality for Entrants
    @Test
    public void testAcceptEventUpdatesStatus() {
        testEntrant.acceptEvent(db, eventID);

        db.collection("events").document(eventID)
                .collection("waitingList").document(entrantID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String status = task.getResult().getString("status");
                        assertEquals("The entrant's status should be updated to 'accepted'", "accepted", status);
                    }
                });
    }

    @Test
    public void testDeclineEventUpdatesStatus() {
        db.collection("events").document(eventID)
                .collection("waitingList").document(entrantID)
                .set(Collections.singletonMap("status", "invited"));

        testEntrant.declineEvent(db, eventID, testEvent);

        db.collection("events").document(eventID)
                .collection("waitingList").document(entrantID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String status = task.getResult().getString("status");
                        assertEquals("The entrant's status should be updated to 'declined'", "declined", status);
                    }
                });
    }

    @Test
    public void testErrorHandlingForMissingEntrantInWaitingList() {
        testEntrant.acceptEvent(db, eventID);

        db.collection("events").document(eventID)
                .collection("waitingList").document(entrantID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assertTrue("The entrant document should not exist in the waiting list", document == null || !document.exists());
                    }
                });
    }

    @Test
    public void testErrorHandlingForMissingEntrantInAndroidIDCollection() {
        testEntrant.acceptEvent(db, eventID);

        db.collection("Android ID").document(entrantID)
                .collection("waitListedEvents").document(eventID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assertTrue("The entrant document should not exist in the Android ID collection", document == null || !document.exists());
                    }
                });
    }
}
