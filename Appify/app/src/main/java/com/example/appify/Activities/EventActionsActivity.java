// EventActionsActivity.java
package com.example.appify.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class EventActionsActivity extends AppCompatActivity {

    private static final String TAG = "LotteryTestActivity";
    private String eventID;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_actions);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();

        eventID = intent.getStringExtra("eventID");
        String name = intent.getStringExtra("name");
        String date = intent.getStringExtra("date");
        String facility = intent.getStringExtra("facility");
        String registrationEndDate = intent.getStringExtra("registrationEndDate");
        String description = intent.getStringExtra("description");
        int maxWaitEntrants = intent.getIntExtra("maxWaitEntrants", 0);
        int maxSampleEntrants = intent.getIntExtra("maxSampleEntrants", 0);
        String posterUriString = intent.getStringExtra("posterUri");
        boolean isGeolocate = intent.getBooleanExtra("isGeolocate", false);



        // Lottery Button
        Button lotteryButton = findViewById(R.id.lottery_button);
        lotteryButton.setOnClickListener(v -> runLotteryTest());

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent sendIntent = new Intent(EventActionsActivity.this, EventDetailActivity.class);
            sendIntent.putExtra("name", name );
            sendIntent.putExtra("date", date);
            sendIntent.putExtra("facility", facility);
            sendIntent.putExtra("registrationEndDate", registrationEndDate);
            sendIntent.putExtra("description",description );
            sendIntent.putExtra("maxWaitEntrants", maxWaitEntrants);
            sendIntent.putExtra("maxSampleEntrants", maxSampleEntrants);
            sendIntent.putExtra("eventID", eventID);
            sendIntent.putExtra("posterUri", posterUriString);
            sendIntent.putExtra("isGeolocate", isGeolocate);
            startActivity(sendIntent);
        });
    }

    // Runs the lottery for a specific event
    public void runLotteryTest() {
        db.collection("events").document(eventID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Manually retrieve fields from DocumentSnapshot to create an Event instance
                        String name = documentSnapshot.getString("name");
                        String date = documentSnapshot.getString("date");
                        String registrationEndDate = documentSnapshot.getString("registrationEndDate");
                        String description = documentSnapshot.getString("description");
                        String facility = documentSnapshot.getString("facility");
                        int maxWaitEntrants = documentSnapshot.getLong("maxWaitEntrants").intValue();
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
                                maxWaitEntrants, maxSampleEntrants, posterUri, isGeolocate,
                                notifyWaitlisted, notifyEnrolled, notifyCancelled, notifyInvited,
                                waitlistedMessage, enrolledMessage, cancelledMessage, invitedMessage, organizerID);

                        // Run the lottery function for the event
                        testEvent.lottery(db, eventID);

                        // Log to confirm the test execution
                        Log.d(TAG, "Lottery test run completed for event ID: " + eventID);
                    } else {
                        Log.w(TAG, "No event found with ID: " + eventID);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving event with ID: " + eventID, e));
    }
}
