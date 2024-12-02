package com.example.appify;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;

import com.example.appify.Activities.EventDetailActivity;
import com.example.appify.Activities.MapActivity;
import com.example.appify.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Instrumented test for MapActivity.
 * This test verifies that MapActivity launches correctly with the provided intent extras
 * and that key UI components are displayed. It also tests the back button functionality.
 */
@RunWith(AndroidJUnit4.class)
public class MapActivityTest {

    /**
     * Helper method to create an explicit intent for MapActivity with required extras.
     *
     * @return An explicit intent targeting MapActivity.
     */
    private Intent createMapActivityIntent() {
        // Obtain the application context
        Intent intent = new Intent(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext(), MapActivity.class);
        // Add the required extras
        intent.putExtra("eventID", "test_event_id");
        intent.putExtra("name", "Test Event");
        intent.putExtra("date", "2024-12-31");
        intent.putExtra("facility", "Test Facility");
        intent.putExtra("registrationEndDate", "2024-11-30");
        intent.putExtra("description", "This is a test event description.");
        intent.putExtra("maxWaitEntrants", 100);
        intent.putExtra("maxSampleEntrants", 50);
        intent.putExtra("posterUri", "http://example.com/poster.jpg");
        intent.putExtra("isGeolocate", true);
        return intent;
    }

    @Before
    public void setUp() {
        // Initialize Espresso Intents before each test
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release Espresso Intents after each test
        Intents.release();
    }

    /**
     * Tests that MapActivity launches with the correct intent extras
     * and that the map and back button are displayed.
     */
    @Test
    public void testMapActivityLaunchWithIntentExtras() {
        // Create an explicit intent targeting MapActivity with required extras
        Intent intent = createMapActivityIntent();

        // Launch the MapActivity with the explicit intent
        try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if the map view is displayed
            Espresso.onView(withId(R.id.map)).check(matches(isDisplayed()));

            // Check if the back button is displayed
            Espresso.onView(withId(R.id.buttonBackToEventsDetail)).check(matches(isDisplayed()));
        }
    }

    /**
     * Tests the functionality of the back button in MapActivity.
     * Ensures that clicking the back button launches EventDetailActivity.
     */
    @Test
    public void testBackButtonNavigation() {
        // Create an explicit intent targeting MapActivity with required extras
        Intent intent = createMapActivityIntent();

        // Launch the MapActivity with the explicit intent
        try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(intent)) {
            // Perform a click on the back button
            Espresso.onView(withId(R.id.buttonBackToEventsDetail)).perform(ViewActions.click());

            // Verify that EventDetailActivity is launched
            intended(hasComponent(EventDetailActivity.class.getName()));
        }
    }
}
