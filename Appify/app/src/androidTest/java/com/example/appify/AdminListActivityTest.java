package com.example.appify;

import android.widget.ListView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.appify.Activities.AdminListActivity;
import com.example.appify.Adapters.CustomEntrantAdminAdapter;
import com.example.appify.Adapters.CustomFacilityAdapter;
import com.example.appify.Adapters.CustomEventAdapter;
import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.example.appify.Model.Facility;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AdminListActivityTest {

    @Rule
    public ActivityScenarioRule<AdminListActivity> activityRule =
            new ActivityScenarioRule<>(AdminListActivity.class);

    /**
     * Test that mock entrants are loaded and displayed in the AdminListActivity.
     */
    @Test
    public void testAdminListActivityLoadsEntrants() {
        activityRule.getScenario().onActivity(activity -> {
            // Create mock entrant data
            List<Entrant> mockEntrants = Arrays.asList(
                    new Entrant("1", "John Doe", "1234567890", "john.doe@example.com", null, true, "facility1"),
                    new Entrant("2", "Jane Smith", "0987654321", "jane.smith@example.com", null, false, "facility2")
            );

            // Set up the adapter with mock data
            CustomEntrantAdminAdapter entrantAdapter = new CustomEntrantAdminAdapter(activity, mockEntrants);
            ListView listView = activity.findViewById(R.id.admin_list);
            listView.setAdapter(entrantAdapter);
        });

        // Verify entrant names are displayed
        onView(withText("John Doe")).check(matches(isDisplayed()));
        onView(withText("Jane Smith")).check(matches(isDisplayed()));
    }

    /**
     * Test that mock facilities are loaded and displayed in the AdminListActivity.
     */
    @Test
    public void testAdminListActivityLoadsFacilities() {
        activityRule.getScenario().onActivity(activity -> {
            // Create mock facility data
            List<Facility> mockFacilities = Arrays.asList(
                    new Facility("1", "Facility A", "Location A", "emailA@example.com", "Description A", 100, "organizer1"),
                    new Facility("2", "Facility B", "Location B", "emailB@example.com", "Description B", 200, "organizer2")
            );

            // Set up the adapter with mock data
            CustomFacilityAdapter facilityAdapter = new CustomFacilityAdapter(activity, mockFacilities);
            ListView listView = activity.findViewById(R.id.admin_list);
            listView.setAdapter(facilityAdapter);
        });

        // Verify facility names are displayed
        onView(withText("Facility A")).check(matches(isDisplayed()));
        onView(withText("Facility B")).check(matches(isDisplayed()));
    }

    /**
     * Test that mock events are loaded and displayed in the AdminListActivity.
     */
    @Test
    public void testAdminListActivityLoadsEvents() {
        activityRule.getScenario().onActivity(activity -> {
            // Create mock event data
            List<Event> mockEvents = Arrays.asList(
                    new Event("Event 1", "2024-12-01", "Facility A", "2024-11-30", 100, 50, "organizer1"),
                    new Event("Event 2", "2024-12-05", "Facility B", "2024-11-29", 150, 75, "organizer2")
            );

            // Set up the adapter with mock data
            CustomEventAdapter eventAdapter = new CustomEventAdapter(activity, mockEvents, false, true);
            ListView listView = activity.findViewById(R.id.admin_list);
            listView.setAdapter(eventAdapter);
        });

        // Verify event names are displayed
        onView(withText("Event 1")).check(matches(isDisplayed()));
        onView(withText("Event 2")).check(matches(isDisplayed()));
    }

    /**
     * Test toggling between different data views (profiles, events, facilities).
     */
    @Test
    public void testToggleBetweenViews() {
        // Click on profiles toggle
        onView(withId(R.id.toggle_profiles)).perform(click());
        onView(withId(R.id.admin_list)).check(matches(isDisplayed()));

        // Click on events toggle
        onView(withId(R.id.toggle_events)).perform(click());
        onView(withId(R.id.admin_list)).check(matches(isDisplayed()));

        // Click on facilities toggle
        onView(withId(R.id.toggle_facilities)).perform(click());
        onView(withId(R.id.admin_list)).check(matches(isDisplayed()));
    }

    /**
     * Test clicking delete on an entrant to trigger confirmation dialog.
     */
    @Test
    public void testDeleteEntrantTriggersDialog() {
        activityRule.getScenario().onActivity(activity -> {
            // Create mock entrant data
            List<Entrant> mockEntrants = Arrays.asList(
                    new Entrant("1", "John Doe", "1234567890", "john.doe@example.com", null, true, "facility1")
            );

            // Set up the adapter with mock data
            CustomEntrantAdminAdapter entrantAdapter = new CustomEntrantAdminAdapter(activity, mockEntrants);
            ListView listView = activity.findViewById(R.id.admin_list);
            listView.setAdapter(entrantAdapter);
        });

        // Click the delete icon
        onView(withId(R.id.x_icon)).perform(click());

        // Verify the confirmation dialog is displayed
        onView(withText("Delete User")).check(matches(isDisplayed()));

        // Confirm deletion
        onView(withText("Confirm")).perform(click());
    }
}
