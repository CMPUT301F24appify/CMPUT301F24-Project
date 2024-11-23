package com.example.appify.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Fragments.AddFacilityDialogFragment;
import com.example.appify.HeaderNavigation;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;


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

        // Obtain the Current Android ID
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
            db.collection("Android ID").document(androidId).get()
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
                .setMessage("Are you sure you want to delete this facility? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Proceed with deletion
                    db.collection("facilities").document(facilityID).delete()
                            .addOnSuccessListener(aVoid -> {
                                // Update user's facilityID to null
                                String androidId = ((MyApp) getApplication()).getAndroidId();
                                db.collection("Android ID").document(androidId)
                                        .update("facilityID", null)
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(this, "Facility deleted.", Toast.LENGTH_SHORT).show();
                                            // Redirect to the EntrantHomePageActivity after deletion
                                            Intent intent = new Intent(ManageFacilityActivity.this, EntrantHomePageActivity.class);
                                            startActivity(intent);
                                            finish();
                                        });
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
