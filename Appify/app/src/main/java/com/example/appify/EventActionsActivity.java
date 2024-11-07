// EventActionsActivity.java
package com.example.appify;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class EventActionsActivity extends AppCompatActivity {

    private static final String TAG = "LotteryTestActivity";
    private static final String TEST_EVENT_ID = "8b0f2eb9-e96f-48ee-84c0-8002a676f5ca";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_actions);

        db = FirebaseFirestore.getInstance();

        // Set up the event ID text view
        TextView eventIdText = findViewById(R.id.event_id_text);
        eventIdText.setText("Event ID: " + TEST_EVENT_ID);

        // Lottery Button
        Button lotteryButton = findViewById(R.id.lottery_button);
        lotteryButton.setOnClickListener(v -> runLotteryTest());

        // Accept Button
        Button acceptButton = findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(v -> runAcceptStatusTest());

        // Deny Button
        Button denyButton = findViewById(R.id.deny_button);
        denyButton.setOnClickListener(v -> runDenyStatusTest());
    }

    // Runs the lottery for a specific event
    public void runLotteryTest() {
        db.collection("events").document(TEST_EVENT_ID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Manually retrieve fields from DocumentSnapshot to create an Event instance
                        String name = documentSnapshot.getString("name");
                        String date = documentSnapshot.getString("date");
                        String registrationEndDate = documentSnapshot.getString("registrationEndDate");
                        String description = documentSnapshot.getString("description");
                        String facility = documentSnapshot.getString("facility");
                        int maxWishEntrants = documentSnapshot.getLong("maxWishEntrants").intValue();
                        int maxSampleEntrants = documentSnapshot.getLong("maxSampleEntrants").intValue();
                        String posterUri = documentSnapshot.getString("posterUri");
                        boolean isGeolocate = documentSnapshot.getBoolean("isGeolocate") != null
                                && documentSnapshot.getBoolean("isGeolocate");
                        boolean notifyWaitlisted = documentSnapshot.getBoolean("notifyWaitlisted") != null
                                && documentSnapshot.getBoolean("notifyWaitlisted");
                        boolean notifyEnrolled = documentSnapshot.getBoolean("notifyEnrolled") != null
                                && documentSnapshot.getBoolean("notifyEnrolled");
                        boolean notifyCancelled = documentSnapshot.getBoolean("notifyCancelled") != null
                                && documentSnapshot.getBoolean("notifyCancelled");
                        boolean notifyInvited = documentSnapshot.getBoolean("notifyInvited") != null
                                && documentSnapshot.getBoolean("notifyInvited");
                        String organizerID = documentSnapshot.getString("organizerID");

                        // Retrieve notification messages
                        String waitlistedMessage = documentSnapshot.getString("waitlistedMessage");
                        String enrolledMessage = documentSnapshot.getString("enrolledMessage");
                        String cancelledMessage = documentSnapshot.getString("cancelledMessage");
                        String invitedMessage = documentSnapshot.getString("invitedMessage");

                        // Create the Event object
                        Event testEvent = new Event(name, date, facility, registrationEndDate, description,
                                maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate,
                                notifyWaitlisted, notifyEnrolled, notifyCancelled, notifyInvited,
                                waitlistedMessage, enrolledMessage, cancelledMessage, invitedMessage, organizerID);

                        // Run the lottery function for the event
                        testEvent.lottery(db, TEST_EVENT_ID);

                        // Log to confirm the test execution
                        Log.d(TAG, "Lottery test run completed for event ID: " + TEST_EVENT_ID);
                    } else {
                        Log.w(TAG, "No event found with ID: " + TEST_EVENT_ID);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving event with ID: " + TEST_EVENT_ID, e));
    }


    // Accept all invited entrants for a specific event
    public void runAcceptStatusTest() {
        db.collection("events").document(TEST_EVENT_ID)
                .collection("waitingList")
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        // Retrieve entrant details and create an Entrant object
                        Entrant entrant = createEntrantFromDocument(document);

                        // Call acceptEvent for each invited entrant
                        entrant.acceptEvent(db, TEST_EVENT_ID);
                    }
                    Log.d(TAG, "AcceptStatusTest: All invited entrants have been accepted for event ID: " + TEST_EVENT_ID);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving invited entrants for event ID: " + TEST_EVENT_ID, e));
    }

    // Deny all invited entrants for a specific event
    public void runDenyStatusTest() {
        db.collection("events").document(TEST_EVENT_ID)
                .collection("waitingList")
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        // Retrieve entrant details and create an Entrant object
                        Entrant entrant = createEntrantFromDocument(document);

                        // Call declineEvent for each invited entrant
                        entrant.declineEvent(db, TEST_EVENT_ID, new Event());
                    }
                    Log.d(TAG, "DenyStatusTest: All invited entrants have been denied for event ID: " + TEST_EVENT_ID);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving invited entrants for event ID: " + TEST_EVENT_ID, e));
    }

    // Helper method to create an Entrant object from Firestore document data
    private Entrant createEntrantFromDocument(QueryDocumentSnapshot document) {
        String entrantId = document.getId();
        String name = document.getString("name");
        String phoneNumber = document.getString("phoneNumber");
        String email = document.getString("email");
        String profilePictureUrl = document.getString("profilePictureUrl");
        boolean notifications = document.getBoolean("notifications") != null && document.getBoolean("notifications");
        String facilityID = document.getString("facilityID");

        return new Entrant(entrantId, name, phoneNumber, email, profilePictureUrl, notifications, facilityID);
    }
}
