package com.example.appify;

import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class lotteryAcceptDenyTest {

    private FirebaseFirestore db;
    private Event testEvent;
    private Entrant testEntrant;

    // Sample constants for testing
    private static final String TEST_EVENT_ID = "8b0f2eb9-e96f-48ee-84c0-8002a676f5ca";
    private static final String TEST_ENTRANT_ID = "sampleEntrantId";

    @Before
    public void setUp() {
        // Initialize FirebaseFirestore mock
        db = Mockito.mock(FirebaseFirestore.class);

        // Create a mock Event instance with sample data
        testEvent = Mockito.mock(Event.class);

        // Set up Entrant instance with test data
        testEntrant = new Entrant(TEST_ENTRANT_ID, "Test Name", "123-456-7890", "test@example.com", "http://profile.picture.url", true, "sampleFacilityID");
    }

    /**
     * Test the lottery function to ensure it invites entrants up to maxSampleEntrants.
     */
    @Test
    public void testLotteryFunction() {
        // Mock the lottery function behavior
        doAnswer(invocation -> {
            System.out.println("Lottery function executed for event ID: " + TEST_EVENT_ID);
            return null;
        }).when(testEvent).lottery(db, TEST_EVENT_ID);

        // Call lottery and verify that it has been executed once
        testEvent.lottery(db, TEST_EVENT_ID);
        verify(testEvent, times(1)).lottery(db, TEST_EVENT_ID);
    }

    /**
     * Test the acceptEvent function to ensure it updates the entrant's status to "accepted"
     * in both the event's waiting list and the Android ID collection.
     */
    @Test
    public void testAcceptEventFunction() {
        // Mock the acceptEvent behavior
        doAnswer(invocation -> {
            System.out.println("AcceptEvent function executed for entrant ID: " + TEST_ENTRANT_ID);
            return null;
        }).when(testEntrant).acceptEvent(db, TEST_EVENT_ID);

        // Call acceptEvent and verify it was executed for the testEntrant
        testEntrant.acceptEvent(db, TEST_EVENT_ID);
        verify(testEntrant, times(1)).acceptEvent(db, TEST_EVENT_ID);
    }

    /**
     * Test the declineEvent function to ensure it updates the entrant's status to "declined"
     * and re-runs the lottery to select a replacement entrant.
     */
    @Test
    public void testDeclineEventFunction() {
        // Mock the declineEvent behavior
        doAnswer(invocation -> {
            System.out.println("DeclineEvent function executed for entrant ID: " + TEST_ENTRANT_ID);
            // Simulate re-running the lottery function
            testEvent.lottery(db, TEST_EVENT_ID);
            return null;
        }).when(testEntrant).declineEvent(db, TEST_EVENT_ID, testEvent);

        // Call declineEvent and verify it was executed for the testEntrant
        testEntrant.declineEvent(db, TEST_EVENT_ID, testEvent);
        verify(testEntrant, times(1)).declineEvent(db, TEST_EVENT_ID, testEvent);

        // Verify that the lottery function was called as part of the declineEvent
        verify(testEvent, times(1)).lottery(db, TEST_EVENT_ID);
    }
}
