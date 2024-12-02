package com.example.appify;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.appify.Activities.userProfileActivity;
import com.example.appify.Activities.editUserActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest {

    @Rule
    public ActivityTestRule<userProfileActivity> activityRule =
            new ActivityTestRule<>(userProfileActivity.class, true, false);

    @Before
    public void setUp() {
        Intents.init();
        Intent intent = new Intent();
        activityRule.launchActivity(intent);
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testInformationViews() {
        // Check that the views are displayed
        onView(withId(R.id.nameTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.phoneTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.emailTextView)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileImageDisplayed() {
        // Check that the profile image view is displayed
        onView(withId(R.id.profileImageView)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditButtonNavigatesToEditUserActivity() {
        // Provide a mock Android ID to be passed to editUserActivity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), userProfileActivity.class);
        intent.putExtra("Android ID", "mock_android_id");
        ActivityScenario<userProfileActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.editButton)).perform(click());

        // Check that a view specific to editUserActivity is displayed
        onView(withId(R.id.phoneEditText))
                .check(matches(isDisplayed()));
    }
}
