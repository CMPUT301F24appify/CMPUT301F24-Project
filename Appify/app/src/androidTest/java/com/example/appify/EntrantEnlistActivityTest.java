package com.example.appify;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.appify.Activities.EntrantEnlistActivity;
import com.example.appify.Activities.EnlistConfirmationActivity;
import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import static org.hamcrest.Matchers.anyOf;

import java.util.Collections;

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
    public void setUp() {
        Intents.init();
        Intent intent = new Intent();
        intent.putExtra("eventId", "testEventId");
        activityRule.launchActivity(intent);

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


    }

    @Test
    public void testEnlistButtonInitialText() {
        // Prepopulate the waiting list with an "invited" status for the test entrant
        CollectionReference waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.document(entrantID).set(Collections.singletonMap("status", "enrolled"));

        onView(withId(R.id.enlist_leave_button)).check(matches(withText("Leave")));
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testEventDetailsDisplayed() {
        // Check if event details are displayed correctly on the UI
        onView(withId(R.id.event_name)).check(matches(withText("Test Event Name")));
        onView(withId(R.id.event_date)).check(matches(withText("Test Event Date")));
        onView(withId(R.id.event_description)).check(matches(withText("Test Description")));
        onView(withId(R.id.facility_name)).check(matches(withText("Test Facility")));
        onView(withId(R.id.registration_date)).check(matches(withText("Test Registration Date")));
    }

    @Test
    public void testEnlistButtonInitialText1() {
        // Verify initial text of enlist button based on enrollment status

        onView(withId(R.id.enlist_leave_button)).check(matches(withText("Enlist")));
    }

    @Test
    public void testGeolocationRequirementText() {
        // Verify geolocation message based on `isGeolocate` flag
        onView(withId(R.id.geolocationText)).check(matches(anyOf(
                withText("IMPORTANT: Registering for this event REQUIRES geolocation."),
                withText("IMPORTANT: Registering for this event DOES NOT REQUIRE geolocation.")
        )));
    }

    @Test
    public void testEnlistAndLeaveFunctionality() {
        // Perform click on enlist button to enlist in the event
        onView(withId(R.id.enlist_leave_button)).perform(click());

        // Verify the EnlistConfirmationActivity is started with correct intent data
        intended(hasComponent(EnlistConfirmationActivity.class.getName()));
    }

    @Test
    public void testAcceptInviteButtonVisibilityAndFunction() {
        // Simulate user being invited, making accept/decline buttons visible
        onView(withId(R.id.accept_invite_button)).check(matches(isDisplayed()));
        onView(withId(R.id.decline_invite_button)).check(matches(isDisplayed()));

        // Click on accept invite button and check navigation to EntrantHomePageActivity
        onView(withId(R.id.accept_invite_button)).perform(click());
        intended(hasComponent("com.example.appify.Activities.EntrantHomePageActivity"));
    }
}
