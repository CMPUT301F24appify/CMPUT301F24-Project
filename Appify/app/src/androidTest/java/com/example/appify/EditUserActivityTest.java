package com.example.appify;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.UiAutomation;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.appify.Activities.editUserActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Enforces alphabetical order for test methods
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
        // Allow notifications to be allowed so handle popup
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.executeShellCommand("pm grant " + ApplicationProvider.getApplicationContext().getPackageName() + " android.permission.POST_NOTIFICATIONS");
        activityRule.launchActivity(intent);
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void test1CancelButtonHiddenOnFirstEntry() {
        // Verify that the cancel button is hidden on the first entry
        onView(withId(R.id.cancelButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
    @Test
    public void test2InvalidNameInput() {
        // Clear any text, then input an invalid name to trigger the validation error
        onView(withId(R.id.nameEditText)).perform(clearText(), typeText("InvalidName"), closeSoftKeyboard());
    }

    @Test
    public void test3InvalidPhoneNumberInput() {
        // Clear name and phone fields, input invalid phone number to trigger validation
        onView(withId(R.id.nameEditText)).perform(clearText(), typeText("John"), closeSoftKeyboard());
        onView(withId(R.id.phoneEditText)).perform(clearText(), typeText("1234"), closeSoftKeyboard());
    }

    @Test
    public void test4InvalidEmailInput() {
        // Clear name and email fields, input invalid email to trigger validation
        onView(withId(R.id.nameEditText)).perform(clearText(), typeText("John"), closeSoftKeyboard());
        onView(withId(R.id.phoneEditText)).perform(clearText(), typeText("1234567890"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(clearText(), typeText("invalid_email"), closeSoftKeyboard());
    }
    @Test
    public void test5UploadAndRemoveButtonsExist() {
        // Confirm that the upload and remove buttons are displayed on the screen
        onView(withId(R.id.uploadButton)).check(matches(isDisplayed()));
        onView(withId(R.id.removeButton)).check(matches(isDisplayed()));
    }

    @Test
    public void test6EditTextHintsDisplayed() {
        // Check the hint for each input field to confirm placeholders are correct
        onView(withId(R.id.nameEditText)).check(matches(withHint("John Doe")));
        onView(withId(R.id.phoneEditText)).check(matches(withHint("1234567890")));
        onView(withId(R.id.emailEditText)).check(matches(withHint("John@gmail.com")));
    }
    @Test
    public void test7ValidInputAndSubmit() {
        // Clear fields and enter valid data, then submit
        onView(withId(R.id.nameEditText)).perform(clearText(), typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.phoneEditText)).perform(clearText(), typeText("1234567890"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(clearText(), typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());
    }


}
