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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * The ManageFacilityActivity class allows organizers to manage their facility details.
 * Organizers can view, edit, and delete facility information. This activity interacts with Firestore
 * to retrieve and update facility data and Firebase Storage for deleting associated resources.
 */
public class ManageFacilityActivity extends AppCompatActivity implements AddFacilityDialogFragment.FacilityUpdateListener {

    private String name;
    private String location;
    private String email;
    private Integer capacity;
    private String description;

    private FirebaseFirestore db;
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
        db = app.getFirebaseInstance();
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
     * Loads facility data from Firestore and updates the UI components with the facility details.
     *
     * @param facilityID          The unique ID of the facility.
     * @param facilityName        TextView to display the facility name.
     * @param facilityLocation    TextView to display the facility location.
     * @param facilityEmail       TextView to display the facility contact email.
     * @param facilityCapacity    TextView to display the facility capacity.
     * @param facilityDescription TextView to display the facility description.
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
        // Re-fetch updated facility data from Firestore
        db.collection("facilities").document(facilityID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch the updated facility details
                        String updatedName = documentSnapshot.getString("name");
                        String updatedLocation = documentSnapshot.getString("location");
                        String updatedEmail = documentSnapshot.getString("email");
                        String updatedDescription = documentSnapshot.getString("description");
                        int updatedCapacity = documentSnapshot.getLong("capacity").intValue();

                        // Pass existing facility data to the dialog
                        AddFacilityDialogFragment editDialog = AddFacilityDialogFragment.newInstance(
                                facilityID, updatedName, updatedLocation, updatedEmail, updatedDescription, updatedCapacity);
                        editDialog.setFacilityUpdateListener(this); // Set the listener
                        editDialog.show(getSupportFragmentManager(), "EditFacilityDialog");
                    }
                });
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
                                                                // Delete the Event along with its poster
                                                                db.collection("events").document(eventID)
                                                                        .get()
                                                                        .addOnSuccessListener(documentSnapshot1 -> {
                                                                            if (documentSnapshot1.exists()) {
                                                                                String qrCodeLocationUrl = documentSnapshot1.getString("qrCodeLocationUrl");
                                                                                if (qrCodeLocationUrl != null && !qrCodeLocationUrl.isEmpty()) {
                                                                                    // Delete the qrCode image from Firebase Storage
                                                                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                                                                    StorageReference imageRef = storage.getReferenceFromUrl(qrCodeLocationUrl);
                                                                                    imageRef.delete();
                                                                                }

                                                                                String posterUri = documentSnapshot1.getString("posterUri");

                                                                                if (posterUri != null && !posterUri.isEmpty()) {
                                                                                    // Delete the poster image from Firebase Storage
                                                                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                                                                    StorageReference imageRef = storage.getReferenceFromUrl(posterUri);
                                                                                    imageRef.delete();
                                                                                }
                                                                            }

                                                                            // Delete the Event all together
                                                                            db.collection("events").document(eventID)
                                                                                    .delete();
                                                                        });

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