package com.example.appify;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.widget.CheckBox;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.appify.Activities.editUserActivity;
import com.example.appify.Activities.userProfileActivity;

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
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;

public class EditUserActivityTest {

    @Rule
    public ActivityTestRule<editUserActivity> activityRule =
            new ActivityTestRule<>(editUserActivity.class, true, false);

    @Before
    public void setUp() {
        Intents.init();
        Intent intent = new Intent();
        intent.putExtra("Android ID", "TestAndroidID");
        intent.putExtra("firstEntry", true);
        activityRule.launchActivity(intent);
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testEditTextHintsDisplayed() {
        // Check the hint for each input field to confirm placeholders are correct
        onView(withId(R.id.nameEditText)).check(matches(withHint("John Doe")));
        onView(withId(R.id.phoneEditText)).check(matches(withHint("1234567890")));
        onView(withId(R.id.emailEditText)).check(matches(withHint("John@gmail.com")));
    }

    @Test
    public void testCancelButtonHiddenOnFirstEntry() {
        // Verify that the cancel button is hidden on the first entry
        onView(withId(R.id.cancelButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testUploadAndRemoveButtonsExist() {
        // Confirm that the upload and remove buttons are displayed on the screen
        onView(withId(R.id.uploadButton)).check(matches(isDisplayed()));
        onView(withId(R.id.removeButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidNameInput() {
        // Clear any text, then input an invalid name to trigger the validation error
        onView(withId(R.id.nameEditText)).perform(clearText(), typeText("InvalidName"), closeSoftKeyboard());
    }

    @Test
    public void testInvalidPhoneNumberInput() {
        // Clear name and phone fields, input invalid phone number to trigger validation
        onView(withId(R.id.nameEditText)).perform(clearText(), typeText("John"), closeSoftKeyboard());
        onView(withId(R.id.phoneEditText)).perform(clearText(), typeText("1234"), closeSoftKeyboard());

    }

    @Test
    public void testInvalidEmailInput() {
        // Clear name and email fields, input invalid email to trigger validation
        onView(withId(R.id.nameEditText)).perform(clearText(), typeText("John"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(clearText(), typeText("invalid_email"), closeSoftKeyboard());

    }

    @Test
    public void testValidInputAndSubmit() throws InterruptedException {
        // Clear fields and enter valid data, then submit
        onView(withId(R.id.nameEditText)).perform(clearText(), typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.phoneEditText)).perform(clearText(), typeText("1234567890"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(clearText(), typeText("john@example.com"), closeSoftKeyboard());
        

        onView(withId(R.id.submitButton)).perform(click());
        Thread.sleep(2000); // Wait for 2 seconds
        onView(withId(R.id.nameTextView)).check(matches(isDisplayed()));
    }
}
