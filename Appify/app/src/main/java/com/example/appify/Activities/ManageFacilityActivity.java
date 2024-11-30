package com.example.appify.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Fragments.AddFacilityDialogFragment;
import com.example.appify.HeaderNavigation;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


/**
 * The ManageFacilityActivity class provides the functionality for organizers
 * to view, edit, and delete their facility details. This class interfaces with
 * Firestore to load facility data, enables editing through a dialog, and allows
 * deletion of the facility from the database.
 */
public class ManageFacilityActivity extends AppCompatActivity implements AddFacilityDialogFragment.FacilityUpdateListener {

    private String name;
    private String location;
    private String email;
    private Integer capacity;
    private String description;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String facilityID;

    private TextView facilityName, facilityLocation, facilityEmail, facilityCapacity, facilityDescription;


    /**
     * Initializes the activity, sets up the navigation header, retrieves the facility ID, and loads facility details.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_facility);

        // Header Navigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();

        // Obtain the Current AndroidID
        MyApp app = (MyApp) getApplication();
        String androidId = app.getAndroidId();

        // The Values in the UI
        facilityName = findViewById(R.id.facility_name);
        facilityLocation = findViewById(R.id.location_value);
        facilityEmail = findViewById(R.id.email_value);
        facilityCapacity = findViewById(R.id.capacity_value);
        facilityDescription = findViewById(R.id.facility_description);
        Button editButton = findViewById(R.id.edit_button);
        Button deleteButton = findViewById(R.id.delete_button);

        // Retrieve facilityID and then the facility details

        Intent intent = getIntent();
        facilityID = intent.getStringExtra("facilityID");

        if (facilityID != null && !facilityID.isEmpty()) {
            loadFacilityData(facilityID, facilityName, facilityLocation, facilityEmail, facilityCapacity, facilityDescription);
        } else {
            db.collection("AndroidID").document(androidId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            facilityID = documentSnapshot.getString("facilityID");
                            if (facilityID != null) {
                                loadFacilityData(facilityID, facilityName, facilityLocation, facilityEmail, facilityCapacity, facilityDescription);
                            }
                        }
                    });
        }

        // Button onClickListeners
        editButton.setOnClickListener(v -> editFacility(facilityID));
        deleteButton.setOnClickListener(v -> deleteFacility(facilityID));
    }

    /**
     * Updates the UI with new facility details after the facility has been edited.
     *
     * @param name        The updated name of the facility.
     * @param location    The updated location of the facility.
     * @param email       The updated contact email for the facility.
     * @param description The updated description of the facility.
     * @param capacity    The updated maximum capacity of the facility.
     */
    @Override
    public void onFacilityUpdated(String name, String location, String email, String description, int capacity) {
        // Update UI with the new facility details
        facilityName.setText(name);
        facilityLocation.setText(location);
        facilityEmail.setText(email);
        facilityCapacity.setText(String.valueOf(capacity));
        facilityDescription.setText(description);
    }


    /**
     * Loads facility data from Firestore and populates the UI elements with the facility details.
     *
     * @param facilityID       The ID of the facility.
     * @param facilityName     The TextView to display the facility's name.
     * @param facilityLocation The TextView to display the facility's location.
     * @param facilityEmail    The TextView to display the facility's contact email.
     * @param facilityCapacity The TextView to display the facility's capacity.
     * @param facilityDescription The TextView to display the facility's description.
     */
    private void loadFacilityData(String facilityID, TextView facilityName, TextView facilityLocation,
                                  TextView facilityEmail, TextView facilityCapacity, TextView facilityDescription) {
        db.collection("facilities").document(facilityID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        name = documentSnapshot.getString("name");
                        location = documentSnapshot.getString("location");
                        email = documentSnapshot.getString("email");
                        capacity = documentSnapshot.getLong("capacity").intValue();
                        description = documentSnapshot.getString("description");

                        facilityName.setText(name);
                        facilityLocation.setText(location);
                        facilityEmail.setText(email);
                        facilityCapacity.setText(String.valueOf(capacity));
                        facilityDescription.setText(description);
                    }
                });
    }

    /**
     * Opens the AddFacilityDialogFragment to edit the facility details, pre-populating
     * the dialog fields with the current facility data.
     *
     * @param facilityID The ID of the facility to edit.
     */
    private void editFacility(String facilityID) {
        // Pass existing facility data to the dialog
        AddFacilityDialogFragment editDialog = AddFacilityDialogFragment.newInstance(
                facilityID, name, location, email, description, capacity);
        editDialog.setFacilityUpdateListener(this); // Set the listener
        editDialog.show(getSupportFragmentManager(), "EditFacilityDialog");
    }

    /**
     * Deletes the facility from Firestore and removes the facility ID from the user's record,
     * then navigates back to the EntrantHomePageActivity.
     *
     * @param facilityID The ID of the facility to delete.
     */
    private void deleteFacility(String facilityID) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Facility")
                .setMessage("Are you sure you want to delete this facility? ALL EVENTS associated with the facility will also be deleted. This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Fetch the facility from Firestore
                    db.collection("facilities").document(facilityID)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String organizerID = documentSnapshot.getString("organizerID");

                                    // Fetch events inside facilities/facilityID/events
                                    db.collection("facilities").document(facilityID).collection("events")
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                                                    String eventID = eventDoc.getId();

                                                    // Check for and process the waitingList for the event
                                                    db.collection("events").document(eventID).collection("waitingList")
                                                            .get()
                                                            .addOnSuccessListener(waitingListSnapshot -> {
                                                                for (QueryDocumentSnapshot waitDoc : waitingListSnapshot) {
                                                                    String userID = waitDoc.getId();

                                                                    // Delete the eventID from the user's waitListedEvents collection
                                                                    db.collection("AndroidID").document(userID)
                                                                            .collection("waitListedEvents")
                                                                            .document(eventID)
                                                                            .delete();

                                                                    // Delete the individual waitingList entry
                                                                    db.collection("events").document(eventID).collection("waitingList")
                                                                            .document(userID)
                                                                            .delete();
                                                                }
                                                            })
                                                            .addOnCompleteListener(waitingListTask -> {
                                                                // Delete event from 'events' collection
                                                                db.collection("events").document(eventID)
                                                                        .delete();

                                                                // Delete event document from 'facilities/facilityID/events' collection
                                                                db.collection("facilities").document(facilityID).collection("events")
                                                                        .document(eventID)
                                                                        .delete();
                                                            });
                                                }
                                            })
                                            .addOnCompleteListener(task -> {
                                                // Once all events are processed, delete the facility itself
                                                db.collection("facilities").document(facilityID)
                                                        .delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            if (organizerID != null) {
                                                                // Update organizer's facility ID to null
                                                                db.collection("AndroidID").document(organizerID)
                                                                        .update("facilityID", null)
                                                                        .addOnSuccessListener(aVoid1 -> {
                                                                            // Navigate to EntrantHomePageActivity after facility deletion
                                                                            Intent intent = new Intent(this, EntrantHomePageActivity.class);
                                                                            startActivity(intent);
                                                                            finish(); // Close the current activity
                                                                        });
                                                            } else {
                                                                // Navigate directly if no organizerID exists
                                                                Intent intent = new Intent(this, EntrantHomePageActivity.class);
                                                                startActivity(intent);
                                                                finish(); // Close the current activity
                                                            }
                                                        });
                                            });
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
