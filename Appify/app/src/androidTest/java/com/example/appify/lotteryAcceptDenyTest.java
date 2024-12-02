package com.example.appify;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
//All tests below were done with the help of ChatGPT, "Make a zero test and 4 test cases for method
//(describes the lottery, acceptEvent and declineEvent)", 2024-11-08
@RunWith(AndroidJUnit4.class)
public class lotteryAcceptDenyTest {

    private FirebaseFirestore db;
    private Event testEvent;
    private String eventID;
    private Entrant testEntrant;
    private String entrantID;

    @Before
    public void setUp() {
        // Initialize Firebase
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();

        // Set up a basic Event instance for the test
        testEvent = new Event();
        eventID = "test_event_id"; // Set a static ID for the test event to avoid collisions

        // Set up a basic Entrant instance for the test
        entrantID = "test_entrant_id"; // Static ID for testing
        //testEntrant = new Entrant(entrantID, "Test User", "1234567890", "testuser@example.com", "profilePicUrl", true);

        // Prepopulate the waiting list with an "invited" status for the test entrant
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.document(entrantID).set(Collections.singletonMap("status", "invited"));
    }

    @Test
    public void zeroTest_lotteryCompletesWithoutErrors() {
        // Run the lottery method on the testEvent instance
        testEvent.lottery(db, eventID);

        // If the test reaches this line, we consider it a success for the zero test
        assertTrue("Zero test completed without errors", true);
    }

    @Test
    public void testCase1_noEligibleEntrants() {
        // Set up the waiting list with no "enrolled" status entrants
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        // Create entrants with statuses other than "enrolled" (e.g., "waitlisted" or "declined")
        waitingListRef.document("entrant1").set(Collections.singletonMap("status", "waitlisted"));
        waitingListRef.document("entrant2").set(Collections.singletonMap("status", "declined"));
        waitingListRef.document("entrant3").set(Collections.singletonMap("status", "waitlisted"));

        // Run the lottery method on the testEvent instance
        testEvent.lottery(db, eventID);

        // Check the results after the lottery completes
        waitingListRef.whereEqualTo("status", "invited").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Assert that no entrants have been invited
                    assertEquals("No entrants should be invited since there are no eligible entrants", 0, task.getResult().size());
                }
            }
        });
    }

    @Test
    public void testCase2_fewerEligibleEntrantsThanMaxSample() {
        // Set up the waiting list with fewer eligible entrants than maxSampleEntrants
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        // Add 3 "enrolled" entrants (less than maxSampleEntrants)
        waitingListRef.document("entrant1").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant2").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant3").set(Collections.singletonMap("status", "enrolled"));

        // Run the lottery method on the testEvent instance
        testEvent.lottery(db, eventID);

        // Check the results after the lottery completes
        waitingListRef.whereEqualTo("status", "invited").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Assert that all eligible entrants were invited
                    assertEquals("All eligible entrants should be invited", 3, task.getResult().size());
                }
            }
        });
    }

    @Test
    public void testCase3_equalEligibleEntrantsToMaxSample() {
        // Set up the waiting list with exactly maxSampleEntrants eligible entrants
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        // Add 5 "enrolled" entrants (equal to maxSampleEntrants)
        waitingListRef.document("entrant1").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant2").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant3").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant4").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant5").set(Collections.singletonMap("status", "enrolled"));

        // Run the lottery method on the testEvent instance
        testEvent.lottery(db, eventID);

        // Check the results after the lottery completes
        waitingListRef.whereEqualTo("status", "invited").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Assert that all eligible entrants were invited
                    assertEquals("Exactly maxSampleEntrants should be invited", 5, task.getResult().size());
                }
            }
        });
    }

    @Test
    public void testCase4_moreEligibleEntrantsThanMaxSample() {
        // Set up the waiting list with more eligible entrants than maxSampleEntrants
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");

        // Add 8 "enrolled" entrants (more than maxSampleEntrants)
        waitingListRef.document("entrant1").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant2").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant3").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant4").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant5").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant6").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant7").set(Collections.singletonMap("status", "enrolled"));
        waitingListRef.document("entrant8").set(Collections.singletonMap("status", "enrolled"));

        // Run the lottery method on the testEvent instance
        testEvent.lottery(db, eventID);

        // Check the results after the lottery completes
        waitingListRef.whereEqualTo("status", "invited").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Assert that exactly maxSampleEntrants entrants were invited
                    assertEquals("Only maxSampleEntrants eligible entrants should be invited", 5, task.getResult().size());
                }
            }
        });
    }

    @Test
    public void zeroTest_acceptEventCompletesWithoutErrors() {
        // Run the acceptEvent method on the testEntrant instance
        testEntrant.acceptEvent(db, eventID);

        // If the test reaches this line, we consider it a success for the zero test
        assertTrue("Zero test for acceptEvent completed without errors", true);
    }

    @Test
    public void testCase1_successfulStatusUpdateInWaitingList() {
        // Run the acceptEvent method to update status to "accepted"
        testEntrant.acceptEvent(db, eventID);

        // Verify the status update in the waiting list
        db.collection("events").document(eventID)
                .collection("waitingList").document(entrantID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String status = task.getResult().getString("status");
                            assertEquals("The entrant's status should be updated to 'accepted'", "accepted", status);
                        } else {
                            assertEquals("The test failed due to a missing or incorrect document", true, false);
                        }
                    }
                });
    }

    @Test
    public void testCase2_successfulStatusUpdateInAndroidIDCollection() {
        // Run the acceptEvent method to update status to "accepted"
        testEntrant.acceptEvent(db, eventID);

        // Verify the status update in the Android ID collection
        db.collection("Android ID").document(entrantID)
                .collection("waitListedEvents").document(eventID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String status = task.getResult().getString("status");
                            assertEquals("The entrant's status should be updated to 'accepted'", "accepted", status);
                        } else {
                            assertEquals("The test failed due to a missing or incorrect document", true, false);
                        }
                    }
                });
    }

    @Test
    public void testCase3_errorHandlingWhenEntrantMissingInWaitingList() {
        // Run the acceptEvent method to attempt to update status to "accepted"
        testEntrant.acceptEvent(db, eventID);

        // Verify that the entrant document still does not exist in the waiting list
        db.collection("events").document(eventID)
                .collection("waitingList").document(entrantID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            // Check that the document is missing, as it was not prepopulated
                            assertTrue("The entrant document should not exist in the waiting list", document == null || !document.exists());
                        } else {
                            // Log an error if the query itself failed unexpectedly
                            assertTrue("The test failed due to an unexpected error", false);
                        }
                    }
                });
    }

    @Test
    public void testCase4_errorHandlingWhenEntrantMissingInAndroidIDCollection() {
        // Run the acceptEvent method to attempt to update status to "accepted"
        testEntrant.acceptEvent(db, eventID);

        // Verify that the entrant document still does not exist in the Android ID collection
        db.collection("Android ID").document(entrantID)
                .collection("waitListedEvents").document(eventID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            // Check that the document is missing, as it was not prepopulated
                            assertTrue("The entrant document should not exist in the Android ID collection", document == null || !document.exists());
                        } else {
                            // Log an error if the query itself failed unexpectedly
                            assertTrue("The test failed due to an unexpected error", false);
                        }
                    }
                });
    }

    @Test
    public void zeroTest_declineEventCompletesWithoutErrors() {
        // Run the declineEvent method on the testEntrant instance
        testEntrant.declineEvent(db, eventID, testEvent);

        // If the test reaches this line, we consider it a success for the zero test
        assertTrue("Zero test for declineEvent completed without errors", true);
    }

    @Test
    public void testCase1_successfulStatusUpdateToDeclinedInWaitingList() {
        // Prepopulate the waiting list with "invited" status
        db.collection("events").document(eventID)
                .collection("waitingList").document(entrantID)
                .set(Collections.singletonMap("status", "invited"));

        // Run the declineEvent method
        testEntrant.declineEvent(db, eventID, testEvent);

        // Verify the status update in the waiting list
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
    public void testCase2_successfulStatusUpdateToDeclinedInAndroidIDCollection() {
        // Prepopulate the Android ID collection with "invited" status
        db.collection("Android ID").document(entrantID)
                .collection("waitListedEvents").document(eventID)
                .set(Collections.singletonMap("status", "invited"));

        // Run the declineEvent method
        testEntrant.declineEvent(db, eventID, testEvent);

        // Verify the status update in the Android ID collection
        db.collection("Android ID").document(entrantID)
                .collection("waitListedEvents").document(eventID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String status = task.getResult().getString("status");
                        assertEquals("The entrant's status should be updated to 'declined'", "declined", status);
                    }
                });
    }

    @Test
    public void testCase3DeclineEvent_errorHandlingWhenEntrantMissingInWaitingList() {
        // Do NOT prepopulate the waiting list (entrant does not exist)

        // Run the declineEvent method
        testEntrant.declineEvent(db, eventID, testEvent);

        // Verify that the entrant document is still missing
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
    public void testCase4DeclineEvent_errorHandlingWhenEntrantMissingInAndroidIDCollection() {
        // Do NOT prepopulate the Android ID collection (entrant does not exist)

        // Run the declineEvent method
        testEntrant.declineEvent(db, eventID, testEvent);

        // Verify that the entrant document is still missing in the Android ID collection
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
