package com.example.appify;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import android.content.Intent;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.test.core.app.ApplicationProvider;

import com.example.appify.Activities.EventEntrantsActivity;
import com.example.appify.Model.Entrant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EventEntrantsActivityTest {

    @Mock
    FirebaseFirestore mockDb;
    @Mock
    CollectionReference mockWaitingListRef;

    private EventEntrantsActivity activity;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventEntrantsActivity.class);
        intent.putExtra("eventID", "testEventID");
        activity = new EventEntrantsActivity();
        activity.setIntent(intent);

        // Mock Firebase instance in the activity
        activity.db = mockDb;
        when(mockDb.collection("events").document("testEventID").collection("waitingList")).thenReturn(mockWaitingListRef);

        activity.entrantListView = mock(ListView.class);
        activity.waitListedCheckbox = mock(CheckBox.class);
        activity.invitedCheckBox = mock(CheckBox.class);
        activity.acceptedCheckBox = mock(CheckBox.class);
        activity.rejectedCheckBox = mock(CheckBox.class);
    }

    @Test
    public void testReloadData_fetchesEntrantsData() {
        activity.reloadData("testEventID");

        verify(mockWaitingListRef).get(); // Ensure Firebase call is made
    }

    @Test
    public void testCheckboxes_updateEntrantList() {
//        Entrant testEntrant = new Entrant("id1", "Test User", "1234567890", "test@example.com", "url", true);
//        activity.entrantListWaitinglisted.add(testEntrant);
//
//        // Simulate checking waitlisted checkbox
//        activity.waitListedCheckbox.setChecked(true);
//        activity.waitListedCheckbox.getOnCheckedChangeListener().onCheckedChanged(activity.waitListedCheckbox, true);
//        assertTrue(activity.entrantListAll.contains(testEntrant));
//
//        // Simulate unchecking waitlisted checkbox
//        activity.waitListedCheckbox.setChecked(false);
//        activity.waitListedCheckbox.getOnCheckedChangeListener().onCheckedChanged(activity.waitListedCheckbox, false);
//        assertFalse(activity.entrantListAll.contains(testEntrant));
    }
}
