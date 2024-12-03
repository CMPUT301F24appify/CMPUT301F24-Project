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

import java.util.ArrayList;
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

            List<Entrant> mockEntrants = Arrays.asList(
                    new Entrant("1", "John Doe", "1234567890", "johndoe@test.com", null, true, "facility1"),
                    new Entrant("2", "John No", "0987654321", "johnno@test.com", null, false, "facility2")
            );

            // Mock Adapter
            CustomEntrantAdminAdapter entrantAdapter = new CustomEntrantAdminAdapter(activity, mockEntrants);
            ListView listView = activity.findViewById(R.id.admin_list);
            listView.setAdapter(entrantAdapter);
        });

        onView(withText("John Doe")).check(matches(isDisplayed()));
        onView(withText("John No")).check(matches(isDisplayed()));
    }

    /**
     * Test that mock facilities are loaded and displayed in the AdminListActivity.
     */
    @Test
    public void testAdminListActivityLoadsFacilities() {
        activityRule.getScenario().onActivity(activity -> {

            List<Facility> mockFacilities = Arrays.asList(
                    new Facility("1", "Facility 1", "Location 1", "email1@test.com", "Description 1", 100, "organizer1"),
                    new Facility("2", "Facility 2", "Location 2", "email2@test.com", "Description 2", 200, "organizer2")
            );

            // Mock Adapter
            CustomFacilityAdapter facilityAdapter = new CustomFacilityAdapter(activity, mockFacilities);
            ListView listView = activity.findViewById(R.id.admin_list);
            listView.setAdapter(facilityAdapter);
        });

        onView(withText("Facility 1")).check(matches(isDisplayed()));
        onView(withText("Facility 2")).check(matches(isDisplayed()));
    }

    /**
     * Test that mock events are loaded and displayed in the AdminListActivity.
     */
    @Test
    public void testAdminListActivityLoadsEvents() {
        activityRule.getScenario().onActivity(activity -> {

            List<Event> mockEvents = Arrays.asList(
                    new Event("Event 1", "2024-12-01", "Facility 1", "2024-11-30", 100, 50, "organizer1"),
                    new Event("Event 2", "2024-12-05", "Facility 2", "2024-11-29", 150, 75, "organizer2")
            );

            // Mock Adapter
            CustomEventAdapter eventAdapter = new CustomEventAdapter(activity, mockEvents, false, true);
            ListView listView = activity.findViewById(R.id.admin_list);
            listView.setAdapter(eventAdapter);
        });

        onView(withText("Event 1")).check(matches(isDisplayed()));
        onView(withText("Event 2")).check(matches(isDisplayed()));
    }

    /**
     * Test toggling between different data views (profiles, events, facilities).
     */
    @Test
    public void testToggleBetweenViews() {
        onView(withId(R.id.toggle_profiles)).perform(click());
        onView(withId(R.id.admin_list)).check(matches(isDisplayed()));

        onView(withId(R.id.toggle_events)).perform(click());
        onView(withId(R.id.admin_list)).check(matches(isDisplayed()));

        onView(withId(R.id.toggle_facilities)).perform(click());
        onView(withId(R.id.admin_list)).check(matches(isDisplayed()));
    }

    /**
     * Test clicking delete on an entrant to trigger confirmation dialog.
     */
    @Test
    public void testDeleteEntrantTriggersDialog() throws InterruptedException {
        Entrant entrant = new Entrant("1", "John Doe", "1234567890", "john.doe@example.com", null, true, "facility1");
        activityRule.getScenario().onActivity(activity -> {
            List<Entrant> mockEntrants = new ArrayList<>(Arrays.asList(
                    entrant
            ));

            //Mock adapter
            CustomEntrantAdminAdapter entrantAdapter = new CustomEntrantAdminAdapter(activity, mockEntrants);
            ListView listView = activity.findViewById(R.id.admin_list);
            listView.setAdapter(entrantAdapter);
        });

        onView(withId(R.id.x_icon)).perform(click());
        Thread.sleep(2000); //Wait to ensure the text can be checked
        onView(withText("Delete User: " + entrant.getName())).check(matches(isDisplayed()));

        onView(withText("Please confirm that you want to delete: " + entrant.getName() + ". User will be removed from all waiting lists. If the user owns a facility, ALL events at that facility as well as the facility will be deleted. This action cannot be undone."))
                .check(matches(isDisplayed()));
        onView(withText("Confirm")).perform(click());
    }

}
