package com.example.appify.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.HeaderNavigation;
import com.example.appify.MyApp;
import com.example.appify.R;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The EntrantEnlistActivity class provides the UI and functionality for users
 * to enlist in or leave an event’s waiting list. It displays event details and
 * includes enlist and leave buttons.
 */
public class EntrantEnlistActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private Button enlistLeaveButton;
    private Button acceptInviteButton;
    private Button declineInviteButton;
    private String name;
    private String date;
    private String registrationEndDate;
    private String facility;
    private boolean isGeolocate;
    private String description;

    /**
     * Initializes the activity, sets up the navigation header, retrieves event details from
     * the incoming intent, and populates the UI with these details. Configures the enlistment
     * or leave button based on the user’s enrollment status in the event’s waiting list.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle). Note: Otherwise, it is null.
     */
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enlist_page);

         Intent intent = getIntent();

         db = FirebaseFirestore.getInstance();

         Uri data = intent.getData();
         if (data != null && "myapp".equals(data.getScheme())) {
             List<String> params = data.getPathSegments();
             String eventId = params.get(0);

             // Fetch event details using eventId
             FirebaseFirestore db = FirebaseFirestore.getInstance();
             db.collection("events").document(eventId)
                     .get()
                     .addOnSuccessListener(documentSnapshot -> {
                         if (documentSnapshot.exists()) {

                             HeaderNavigation headerNavigation = new HeaderNavigation(this);
                             headerNavigation.setupNavigation();

                             // Get the users Android ID
                             MyApp app = (MyApp) getApplication();
                             String androidId = app.getAndroidId();

                             // Find Views in the layout
                             TextView eventName = findViewById(R.id.event_name);
                             TextView eventDate = findViewById(R.id.event_date);
                             TextView eventDescription = findViewById(R.id.event_description);
                             TextView eventFacility = findViewById(R.id.facility_name);
                             TextView eventRegistrationEnd = findViewById(R.id.registration_date);
                             TextView eventGeolocate = findViewById(R.id.geolocationText);

                             enlistLeaveButton = findViewById(R.id.enlist_leave_button);
                             acceptInviteButton = findViewById(R.id.accept_invite_button);
                             declineInviteButton = findViewById(R.id.decline_invite_button);

                             db.collection("events").document(eventId).get().addOnCompleteListener(task ->{
                                 if (task.isSuccessful()){

                                     DocumentSnapshot eventData = task.getResult();
                                     name = eventData.getString("name");

                                     date = eventData.getString("date");
                                     description = eventData.getString("description");
                                     facility = eventData.getString("facility");
                                     registrationEndDate = eventData.getString("registrationEndDate");
                                     isGeolocate = eventData.getBoolean("geolocate");

                                     eventName.setText(name);
                                     eventDate.setText(date);
                                     eventDescription.setText(description);
                                     eventRegistrationEnd.setText(registrationEndDate);
                                     eventFacility.setText(facility);


                                     // Show Geolocation Requirement
                                     if (isGeolocate) {
                                         eventGeolocate.setText("IMPORTANT: Registering for this event REQUIRES geolocation.");
                                     } else {
                                         eventGeolocate.setText("IMPORTANT: Registering for this event DOES NOT REQUIRE geolocation.");
                                     }

                                     // Handle Enlist and Leave buttons
                                     enlistLeaveButton = findViewById(R.id.enlist_leave_button);

                                     // Check if user is already enlisted in the waiting list

                                     checkUserEnrollmentStatus(eventId, androidId);

                                 }
                             });

                         }
                     });
         }
         else {
             HeaderNavigation headerNavigation = new HeaderNavigation(this);
             headerNavigation.setupNavigation();

             // Retrieve event details from the intent
             String eventId = intent.getStringExtra("eventId");
             name = intent.getStringExtra("name");
             date = intent.getStringExtra("date");
             registrationEndDate = intent.getStringExtra("registrationEndDate");
             facility = intent.getStringExtra("facility");
             String description = intent.getStringExtra("description");
             int maxWishEntrants = intent.getIntExtra("maxWishEntrants", 0);
             int maxSampleEntrants = intent.getIntExtra("maxSampleEntrants", 0);
             String posterUriString = intent.getStringExtra("posterUri");
             isGeolocate = intent.getBooleanExtra("geolocate", false);

             // Find views in the layout and set data
             TextView eventName = findViewById(R.id.event_name);
             TextView eventDate = findViewById(R.id.event_date);
             TextView eventDescription = findViewById(R.id.event_description);
             TextView eventFacility = findViewById(R.id.facility_name);
             TextView eventRegistrationEnd = findViewById(R.id.registration_date);
             TextView eventGeolocate = findViewById(R.id.geolocationText);

             enlistLeaveButton = findViewById(R.id.enlist_leave_button);
             acceptInviteButton = findViewById(R.id.accept_invite_button);
             declineInviteButton = findViewById(R.id.decline_invite_button);

             eventName.setText(name);
             eventDate.setText(date);
             eventDescription.setText(description);
             eventRegistrationEnd.setText(registrationEndDate);
             eventFacility.setText(facility);

             // Show Geolocation Requirement
             if (isGeolocate) {
                 eventGeolocate.setText("IMPORTANT: Registering for this event REQUIRES geolocation.");
             } else {
                 eventGeolocate.setText("IMPORTANT: Registering for this event DOES NOT REQUIRE geolocation.");
             }

             // Handle Enlist and Leave buttons
             enlistLeaveButton = findViewById(R.id.enlist_leave_button);

             db = FirebaseFirestore.getInstance();
             MyApp app = (MyApp) getApplication();
             String androidId = app.getAndroidId();
             // Check if user is already enlisted in the waiting list
             checkUserEnrollmentStatus(eventId,androidId);
         }
    }

    /**
     * Checks if the user is already enlisted in the event's waiting list and updates
     * the enlistLeaveButton text and action accordingly.
     */
    //This function was done with major assistance from chatGPT, "Update to show the accept/deny buttons and call the respective methods", 2024-11-06
    private void checkUserEnrollmentStatus(String eventId, String androidId) {

        DocumentReference eventRef = db.collection("events").document(eventId);
        CollectionReference waitingListRef = eventRef.collection("waitingList");
        Button acceptInviteButton = findViewById(R.id.accept_invite_button);;
        Button declineInviteButton = findViewById(R.id.decline_invite_button);


        // Check the current status of the waiting list
        eventRef.get().addOnSuccessListener(documentSnapshot -> {

            if (documentSnapshot.exists()) {
                int maxWaitEntrants = documentSnapshot.getLong("maxWaitEntrants").intValue();
                boolean isGeolocate = documentSnapshot.getBoolean("geolocate") != null && documentSnapshot.getBoolean("geolocate");

                waitingListRef.get().addOnSuccessListener(querySnapshot -> {

                    int currentEntrants = querySnapshot.size();

                    if (currentEntrants < maxWaitEntrants) {

                        waitingListRef.document(androidId).get().addOnSuccessListener(DocumentSnapshot ->{

                            String status = DocumentSnapshot.getString("status");

                            if(Objects.equals(status, "enrolled")){
                                enlistLeaveButton.setText("Leave");
                                enlistLeaveButton.setOnClickListener(v -> leaveEvent(eventId));
                            } else if (Objects.equals(status, "accepted")) {
                                enlistLeaveButton.setText("Accepted");
                                enlistLeaveButton.setOnClickListener(null);
                                enlistLeaveButton.setBackgroundColor(Color.parseColor("#00FF00"));
                            } else if (Objects.equals(status, "rejected")) {
                                enlistLeaveButton.setText("Rejected");
                                enlistLeaveButton.setOnClickListener(null);
                                enlistLeaveButton.setBackgroundColor(Color.parseColor("#FF0000"));
                                enlistLeaveButton.setTextColor(Color.parseColor("#FFFFFF"));
                            } else if (Objects.equals(status, "invited")) {
                                enlistLeaveButton.setText("Invited");
                                enlistLeaveButton.setOnClickListener(null);

                                // Display accept and decline buttons for invited users
                                acceptInviteButton.setVisibility(View.VISIBLE);
                                declineInviteButton.setVisibility(View.VISIBLE);

                                // Set up actions for the accept and decline buttons
                                acceptInviteButton.setOnClickListener(v -> {
                                    db.collection("Android ID").document(androidId).get().addOnSuccessListener(entrantDoc -> {
                                        if (entrantDoc.exists()) {
                                            Entrant entrant = new Entrant(
                                                    entrantDoc.getString("id"),
                                                    entrantDoc.getString("name"),
                                                    entrantDoc.getString("phoneNumber"),
                                                    entrantDoc.getString("email"),
                                                    entrantDoc.getString("profilePictureUrl"),
                                                    entrantDoc.getBoolean("notifications") != null && entrantDoc.getBoolean("notifications")
                                            );
                                            entrant.acceptEvent(db, eventId);
                                        }
                                    });
                                });

                                declineInviteButton.setOnClickListener(v -> {
                                    db.collection("Android ID").document(androidId).get().addOnSuccessListener(entrantDoc -> {
                                        if (entrantDoc.exists()) {
                                            Entrant entrant = new Entrant(
                                                    entrantDoc.getString("id"),
                                                    entrantDoc.getString("name"),
                                                    entrantDoc.getString("phoneNumber"),
                                                    entrantDoc.getString("email"),
                                                    entrantDoc.getString("profilePictureUrl"),
                                                    entrantDoc.getBoolean("notifications") != null && entrantDoc.getBoolean("notifications")
                                            );

                                            eventRef.get().addOnSuccessListener(eventDoc -> {
                                                if (eventDoc.exists()) {
                                                    Event event = new Event(
                                                            eventDoc.getString("name"),
                                                            eventDoc.getString("date"),
                                                            eventDoc.getString("facility"),
                                                            eventDoc.getString("registrationEndDate"),
                                                            eventDoc.getString("description"),
                                                            eventDoc.getLong("maxWaitEntrants").intValue(),
                                                            eventDoc.getLong("maxSampleEntrants").intValue(),
                                                            eventDoc.getString("posterUri"),
                                                            eventDoc.getBoolean("geolocate") != null && eventDoc.getBoolean("geolocate"),
                                                            eventDoc.getBoolean("notifyWaitlisted") != null && eventDoc.getBoolean("notifyWaitlisted"),
                                                            eventDoc.getBoolean("notifyEnrolled") != null && eventDoc.getBoolean("notifyEnrolled"),
                                                            eventDoc.getBoolean("notifyCancelled") != null && eventDoc.getBoolean("notifyCancelled"),
                                                            eventDoc.getBoolean("notifyInvited") != null && eventDoc.getBoolean("notifyInvited"),
                                                            eventDoc.getString("waitlistedMessage"),
                                                            eventDoc.getString("enrolledMessage"),
                                                            eventDoc.getString("cancelledMessage"),
                                                            eventDoc.getString("invitedMessage"),
                                                            eventDoc.getString("organizerID")
                                                    );

                                                    entrant.declineEvent(db, eventId, event);
                                                }
                                            });
                                        }
                                    });
                                });

                                // Set up actions for the accept and decline buttons
                                acceptInviteButton.setOnClickListener(v -> {
                                    db.collection("Android ID").document(androidId).get().addOnSuccessListener(entrantDoc -> {
                                        if (entrantDoc.exists()) {
                                            Entrant entrant = new Entrant(
                                                    entrantDoc.getString("id"),
                                                    entrantDoc.getString("name"),
                                                    entrantDoc.getString("phoneNumber"),
                                                    entrantDoc.getString("email"),
                                                    entrantDoc.getString("profilePictureUrl"),
                                                    entrantDoc.getBoolean("notifications") != null && entrantDoc.getBoolean("notifications")
                                            );
                                            entrant.acceptEvent(db, eventId);
                                        }
                                    });
                                });

                                declineInviteButton.setOnClickListener(v -> {
                                    db.collection("Android ID").document(androidId).get().addOnSuccessListener(entrantDoc -> {
                                        if (entrantDoc.exists()) {
                                            Entrant entrant = new Entrant(
                                                    entrantDoc.getString("id"),
                                                    entrantDoc.getString("name"),
                                                    entrantDoc.getString("phoneNumber"),
                                                    entrantDoc.getString("email"),
                                                    entrantDoc.getString("profilePictureUrl"),
                                                    entrantDoc.getBoolean("notifications") != null && entrantDoc.getBoolean("notifications")
                                            );

                                            eventRef.get().addOnSuccessListener(eventDoc -> {
                                                if (eventDoc.exists()) {
                                                    Event event = new Event(
                                                            eventDoc.getString("name"),
                                                            eventDoc.getString("date"),
                                                            eventDoc.getString("facility"),
                                                            eventDoc.getString("registrationEndDate"),
                                                            eventDoc.getString("description"),
                                                            eventDoc.getLong("maxWaitEntrants").intValue(),
                                                            eventDoc.getLong("maxSampleEntrants").intValue(),
                                                            eventDoc.getString("posterUri"),
                                                            eventDoc.getBoolean("geolocate") != null && eventDoc.getBoolean("geolocate"),
                                                            eventDoc.getBoolean("notifyWaitlisted") != null && eventDoc.getBoolean("notifyWaitlisted"),
                                                            eventDoc.getBoolean("notifyEnrolled") != null && eventDoc.getBoolean("notifyEnrolled"),
                                                            eventDoc.getBoolean("notifyCancelled") != null && eventDoc.getBoolean("notifyCancelled"),
                                                            eventDoc.getBoolean("notifyInvited") != null && eventDoc.getBoolean("notifyInvited"),
                                                            eventDoc.getString("waitlistedMessage"),
                                                            eventDoc.getString("enrolledMessage"),
                                                            eventDoc.getString("cancelledMessage"),
                                                            eventDoc.getString("invitedMessage"),
                                                            eventDoc.getString("organizerID")
                                                    );

                                                    entrant.declineEvent(db, eventId, event);
                                                }
                                            });
                                        }
                                    });
                                });
                            } else {

                                // User is not enlisted
                                enlistLeaveButton.setText("Enlist");
                                enlistLeaveButton.setOnClickListener(v -> {
                                    if (isGeolocate) {
                                        showGeolocationConfirmationDialog(() -> enlistInEvent(eventId, name, date, registrationEndDate, facility, isGeolocate, androidId));
                                    } else {
                                        enlistInEvent(eventId, name, date, registrationEndDate, facility, isGeolocate, androidId);
                                    }
                                });
                            }
                        });
                    } else {
                        // Waiting list is full
                        enlistLeaveButton.setText("Full");
                        enlistLeaveButton.setOnClickListener(null); // Disable button
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching waiting list data.", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching event data.", Toast.LENGTH_SHORT).show());
    }




    /**
     * Shows a confirmation dialog to inform the user that the event requires geolocation.
     * If the user agrees, proceeds with the enlist action.
     *
     * @param onConfirm Callback to execute if the user confirms the geolocation requirement.
     */
    private void showGeolocationConfirmationDialog(Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle("Geolocation Required")
                .setMessage("Registering for this event REQUIRES geolocation. Do you want to proceed?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User confirmed - proceed with enlistment
                    onConfirm.run();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    /**
     * Enlists the current user in the specified event’s waiting list.
     * It checks if the user is already enlisted and verifies if the waiting list has capacity.
     *
     * @param eventId The unique ID of the event the user wishes to join.
     */
    private void enlistInEvent(String eventId, String name, String date, String registrationEndDate, String facility, boolean isGeolocate, String androidId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        CollectionReference waitingListRef = eventRef.collection("waitingList");

        // Add user to waiting list
        HashMap<String, Object> waitlistData = new HashMap<>();
        waitlistData.put("status", "enrolled");

        waitingListRef.document(androidId).set(waitlistData)
                .addOnSuccessListener(aVoid -> {
                    // Add event to user's waitListedEvents with status "enrolled"
                    CollectionReference userWaitListedEventsRef = db.collection("Android ID")
                            .document(androidId)
                            .collection("waitListedEvents");

                    HashMap<String, Object> eventStatusData = new HashMap<>();
                    eventStatusData.put("status", "enrolled");

                    userWaitListedEventsRef.document(eventId).set(eventStatusData)
                            .addOnSuccessListener(aVoid2 -> {
                                // Navigate to EnlistConfirmationActivity
                                Intent intent = new Intent(EntrantEnlistActivity.this, EnlistConfirmationActivity.class);
                                intent.putExtra("waitingList", "Joined");
                                intent.putExtra("name", name);
                                intent.putExtra("date", date);
                                intent.putExtra("registrationEndDate", registrationEndDate);
                                intent.putExtra("facility", facility);
                                intent.putExtra("geolocate", isGeolocate);
                                startActivity(intent);
//                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to add event to your waitlisted events.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to enlist in the waiting list. Try again.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Removes the current user from the specified event’s waiting list.
     * It checks if the user is already enlisted before attempting removal.
     *
     * @param eventId The unique ID of the event the user wishes to leave.
     */
    private void leaveEvent(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        CollectionReference waitingListRef = eventRef.collection("waitingList");

        // Remove user from waiting list
        MyApp app = (MyApp) getApplication();
        String androidId = app.getAndroidId();
        waitingListRef.document(androidId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove event from user's waitListedEvents
                    CollectionReference userWaitListedEventsRef = db.collection("Android ID")
                            .document(androidId)
                            .collection("waitListedEvents");

                    userWaitListedEventsRef.document(eventId).delete()
                            .addOnSuccessListener(aVoid2 -> {
                                // Navigate to EnlistConfirmationActivity
                                Intent intent = new Intent(EntrantEnlistActivity.this, EnlistConfirmationActivity.class);
                                intent.putExtra("waitingList", "Left");
                                intent.putExtra("name", name);
                                intent.putExtra("date", date);
                                intent.putExtra("registrationEndDate", registrationEndDate);
                                intent.putExtra("facility", facility);
                                intent.putExtra("geolocate", isGeolocate);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove event from your waitlisted events.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to leave the event's waiting list. Try again.", Toast.LENGTH_SHORT).show());
    }

}
