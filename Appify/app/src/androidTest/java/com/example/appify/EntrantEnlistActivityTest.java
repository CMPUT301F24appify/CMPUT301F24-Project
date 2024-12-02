// EntrantEnlistActivityTest.java
package com.example.appify;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.appify.Activities.EntrantEnlistActivity;
import com.example.appify.Activities.EnlistConfirmationActivity;
import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import static org.hamcrest.Matchers.anyOf;

@RunWith(AndroidJUnit4.class)
public class EntrantEnlistActivityTest {

    private FirebaseFirestore db;
    private Event testEvent;
    private String eventID;
    private Entrant testEntrant;
    private String entrantID;

    @Rule
    public ActivityTestRule<EntrantEnlistActivity> activityRule =
            new ActivityTestRule<>(EntrantEnlistActivity.class, true, false);

    @Before
    public void setUp() throws InterruptedException {
        // Initialize Intents to monitor for Activity launches
        Intents.init();

        // Initialize Firebase
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();

        // Define consistent eventID and entrantID
        eventID = "testEventId"; // Must match the intent extra
        entrantID = "test_entrant_id"; // Static ID for testing

        // Set up a basic Event instance for the test
        testEvent = new Event(
                "Test Event Name",
                "Test Event Date",
                "Test Facility ID",
                "Test Registration Date",
                "Test Description",
                100, // maxWaitEntrants
                50,  // maxSampleEntrants
                "testPosterUri",
                true, // isGeolocate
                true, // notifyWaitlisted
                true, // notifyEnrolled
                true, // notifyCancelled
                true, // notifyInvited
                "You have been waitlisted for {event}.",
                "You have been accepted for {event}.",
                "Your registration for {event} has been cancelled.",
                "You have been invited to {event}.",
                "organizer123"
        );

        // Use CountDownLatch to wait for Firestore operations to complete
        CountDownLatch latch = new CountDownLatch(2); // One for event, one for entrant

        // Add the test event to Firestore
        db.collection("events").document(eventID).set(testEvent)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TestSetup", "Test event added successfully.");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestSetup", "Failed to add test event.", e);
                    latch.countDown(); // Proceed even if failed to prevent hanging
                });

        // Set up the Entrant in Firestore
        testEntrant = new Entrant(
                entrantID,
                "Test User",
                "1234567890",
                "testuser@example.com",
                "profilePicUrl",
                true,
                "facility123",
                0.0,
                0.0
        );

        // Add the entrant to "AndroidID" collection
        db.collection("AndroidID").document(entrantID).set(testEntrant)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TestSetup", "Test entrant added successfully.");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestSetup", "Failed to add test entrant.", e);
                    latch.countDown(); // Proceed even if failed to prevent hanging
                });

        // Wait for Firestore setup to complete or timeout after 10 seconds
        if (!latch.await(10, TimeUnit.SECONDS)) {
            Log.e("TestSetup", "Firestore setup timed out.");
        }

        // Prepopulate the waiting list with an "enrolled" status for the test entrant
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        HashMap<String, Object> waitlistData = new HashMap<>();
        waitlistData.put("status", "enrolled");
        waitlistData.put("inviteNotificationSent", false);
        waitlistData.put("notSelectedNotificationSent", false);
        waitlistData.put("latitude", 0.0);
        waitlistData.put("longitude", 0.0);

        CountDownLatch waitlistLatch = new CountDownLatch(1);

        waitingListRef.document(entrantID).set(waitlistData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TestSetup", "Test entrant added to waiting list.");
                    waitlistLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestSetup", "Failed to add test entrant to waiting list.", e);
                    waitlistLatch.countDown();
                });

        // Wait for waiting list setup to complete or timeout after 10 seconds
        if (!waitlistLatch.await(10, TimeUnit.SECONDS)) {
            Log.e("TestSetup", "Waiting list setup timed out.");
        }

        // Launch the Activity with the correct intent
        Intent intent = new Intent();
        intent.putExtra("eventId", eventID);
        activityRule.launchActivity(intent);
    }

    @After
    public void tearDown() throws InterruptedException {
        // Clean up Firestore data after tests
        CountDownLatch latch = new CountDownLatch(2);

        // Delete the test event
        db.collection("events").document(eventID).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TestTeardown", "Test event deleted successfully.");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestTeardown", "Failed to delete test event.", e);
                    latch.countDown();
                });

        // Delete the entrant from "AndroidID" collection
        db.collection("AndroidID").document(entrantID).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TestTeardown", "Test entrant deleted successfully.");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestTeardown", "Failed to delete test entrant.", e);
                    latch.countDown();
                });

        // Wait for Firestore cleanup to complete or timeout after 10 seconds
        if (!latch.await(10, TimeUnit.SECONDS)) {
            Log.e("TestTeardown", "Firestore cleanup timed out.");
        }

        // Release Intents to clean up after tests
        Intents.release();
    }

    @Test
    public void testEnlistButtonInitialText_Enrolled() throws InterruptedException {
        // This test verifies that the enlist button displays "Leave" when the entrant is enrolled

        // Wait briefly to ensure the Activity has loaded data
        Thread.sleep(2000); // Ideally, use IdlingResource instead of sleep

        onView(withId(R.id.enlist_leave_button)).check(matches(withText("Leave")));
    }

    @Test
    public void testEnlistButtonInitialText_NotEnrolled() throws InterruptedException {
        // This test verifies that the enlist button displays "Enlist" when the entrant is not enrolled

        // First, remove the entrant from the waiting list
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        CountDownLatch latch = new CountDownLatch(1);

        waitingListRef.document(entrantID).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TestSetup", "Test entrant removed from waiting list.");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestSetup", "Failed to remove test entrant from waiting list.", e);
                    latch.countDown();
                });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            Log.e("TestSetup", "Removal from waiting list timed out.");
        }

        // Restart the Activity to reflect changes
        activityRule.finishActivity();
        Intent intent = new Intent();
        intent.putExtra("eventId", eventID);
        activityRule.launchActivity(intent);

        // Wait briefly to ensure the Activity has loaded data
        Thread.sleep(2000); // Ideally, use IdlingResource instead of sleep

        onView(withId(R.id.enlist_leave_button)).check(matches(withText("Enlist")));
    }

    @Test
    public void testEventDetailsDisplayed() throws InterruptedException {
        // This test checks if event details are displayed correctly

        // Wait briefly to ensure the Activity has loaded data
        Thread.sleep(2000); // Ideally, use IdlingResource instead of sleep

        onView(withId(R.id.event_name)).check(matches(withText("Test Event Name")));
        onView(withId(R.id.event_date)).check(matches(withText("Test Event Date")));
        onView(withId(R.id.event_description)).check(matches(withText("Test Description")));
        onView(withId(R.id.facility_name)).check(matches(withText("Test Facility ID")));
        onView(withId(R.id.registration_date)).check(matches(withText("Test Registration Date")));
    }

    @Test
    public void testGeolocationRequirementText() throws InterruptedException {
        // This test verifies the geolocation requirement text based on the event's isGeolocate flag

        // Wait briefly to ensure the Activity has loaded data
        Thread.sleep(2000); // Ideally, use IdlingResource instead of sleep

        onView(withId(R.id.geolocationText)).check(matches(withText("IMPORTANT: Registering for this event REQUIRES geolocation.")));
    }

    @Test
    public void testEnlistAndLeaveFunctionality() throws InterruptedException {
        // This test performs enlist and then leave actions, verifying the corresponding behaviors

        // Wait briefly to ensure the Activity has loaded data
        Thread.sleep(2000); // Ideally, use IdlingResource instead of sleep

        // Perform click on "Leave" button
        onView(withId(R.id.enlist_leave_button)).perform(click());

        // Verify that EnlistConfirmationActivity is launched with "Left" status
        intended(hasComponent(EnlistConfirmationActivity.class.getName()));
    }

    @Test
    public void testAcceptInviteButtonVisibilityAndFunction() throws InterruptedException {
        // This test simulates the entrant being invited and verifies the accept/decline buttons

        // First, update the entrant's status to "invited" in the waiting list
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        HashMap<String, Object> inviteData = new HashMap<>();
        inviteData.put("status", "invited");

        CountDownLatch latch = new CountDownLatch(1);

        waitingListRef.document(entrantID).update(inviteData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TestSetup", "Test entrant status updated to invited.");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestSetup", "Failed to update test entrant status to invited.", e);
                    latch.countDown();
                });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            Log.e("TestSetup", "Updating status to invited timed out.");
        }

        // Restart the Activity to reflect changes
        activityRule.finishActivity();
        Intent intent = new Intent();
        intent.putExtra("eventId", eventID);
        activityRule.launchActivity(intent);

        // Wait briefly to ensure the Activity has loaded data
        Thread.sleep(2000); // Ideally, use IdlingResource instead of sleep

        // Check if accept and decline buttons are displayed
        onView(withId(R.id.accept_invite_button)).check(matches(isDisplayed()));
        onView(withId(R.id.decline_invite_button)).check(matches(isDisplayed()));

        // Perform click on accept invite button
        onView(withId(R.id.accept_invite_button)).perform(click());

        // Verify that EntrantHomePageActivity is launched
        intended(hasComponent("com.example.appify.Activities.EntrantHomePageActivity"));
    }
}
