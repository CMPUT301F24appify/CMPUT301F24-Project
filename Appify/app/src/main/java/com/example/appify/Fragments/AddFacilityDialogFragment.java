package com.example.appify.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.appify.HeaderNavigation;
import com.example.appify.Model.Facility;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

/**
 * AddFacilityDialogFragment is a dialog fragment that allows users to add a new facility
 * or edit an existing one. It supports fields for the facility's name, location, capacity,
 * email, and description. If in edit mode, it pre-populates the fields with existing data.
 */
public class AddFacilityDialogFragment extends DialogFragment {

    private FirebaseFirestore db; // Firestore database instance
    private EditText facilityName; // EditText for the facility name
    private EditText facilityLocation; // EditText for the facility location
    private EditText facilityCapacity; // EditText for the facility capacity
    private EditText facilityEmail; // EditText for the facility email
    private EditText facilityDescription; // EditText for the facility description

    private boolean isEditMode = false; // Flag to check if in edit mode
    private String facilityID; // Facility ID for the facility being edited
    private FacilityUpdateListener updateListener; // Listener for facility updates

    /**
     * Creates a new instance of AddFacilityDialogFragment pre-filled with facility details.
     *
     * @param facilityID  The ID of the facility.
     * @param name        The name of the facility.
     * @param location    The location of the facility.
     * @param email       The contact email of the facility.
     * @param description The description of the facility.
     * @param capacity    The maximum capacity of the facility.
     * @return A new instance of AddFacilityDialogFragment with the provided details.
     */
    public static AddFacilityDialogFragment newInstance(String facilityID, String name, String location,
                                                        String email, String description, int capacity) {
        AddFacilityDialogFragment fragment = new AddFacilityDialogFragment();
        Bundle args = new Bundle();
        args.putString("facilityID", facilityID);
        args.putString("name", name);
        args.putString("location", location);
        args.putString("email", email);
        args.putString("description", description);
        args.putInt("capacity", capacity);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to create the dialog's view and initialize UI components.
     * If in edit mode, pre-populates the fields with existing facility details.
     *
     * @param inflater           The LayoutInflater object for inflating views in the fragment.
     * @param container          The parent view that this fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-created from a previous state.
     * @return The constructed view for the dialog.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_facility_dialog, container, false);

        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        facilityName = view.findViewById(R.id.facility_name);
        facilityLocation = view.findViewById(R.id.facility_location);
        facilityCapacity = view.findViewById(R.id.facility_capacity);
        facilityEmail = view.findViewById(R.id.facility_email);
        facilityDescription = view.findViewById(R.id.facility_description);
        Button addButton = view.findViewById(R.id.add_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        // Check if editing mode and populate fields if so
        if (getArguments() != null) {
            isEditMode = true;
            facilityID = getArguments().getString("facilityID");
            facilityName.setText(getArguments().getString("name"));
            facilityLocation.setText(getArguments().getString("location"));
            facilityEmail.setText(getArguments().getString("email"));
            facilityDescription.setText(getArguments().getString("description"));
            facilityCapacity.setText(String.valueOf(getArguments().getInt("capacity")));
            addButton.setText("Update"); // Change button text for clarity
        }

        addButton.setOnClickListener(v -> {
            addOrUpdateFacility();

        });
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
     * Sets the width and height of the dialog to 95% and 90% of the screen dimensions, respectively.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.90);
            getDialog().getWindow().setLayout(width, height);
        }
    }

    /**
     * Adds or updates a facility based on whether the dialog is in edit mode or not.
     * If in edit mode, it updates the existing facility. Otherwise, it adds a new facility to Firestore.
     */
    private void addOrUpdateFacility() {
        String name = facilityName.getText().toString().trim();
        String location = facilityLocation.getText().toString().trim();
        String email = facilityEmail.getText().toString().trim();
        String description = facilityDescription.getText().toString().trim();
        String capacityStr = facilityCapacity.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(location) || TextUtils.isEmpty(email) || TextUtils.isEmpty(description) || TextUtils.isEmpty(capacityStr)) {
            Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email validation using regex
        if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            Toast.makeText(getContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid capacity value.", Toast.LENGTH_SHORT).show();
            return;
        }

        MyApp app = (MyApp) requireActivity().getApplication();
        String organizerID = app.getAndroidId();

        if (isEditMode) {
            // Update existing facility
            db.collection("facilities").document(facilityID)
                    .update("name", name, "location", location, "email", email, "description", description, "capacity", capacity)
                    .addOnSuccessListener(aVoid -> {
                        // Notify the listener with updated details
                        if (updateListener != null) {
                            updateListener.onFacilityUpdated(name, location, email, description, capacity);
                        }
                        dismiss();
                    });
        } else {
            // Add new facility
            String newFacilityID = UUID.randomUUID().toString();
            Facility facility = new Facility(newFacilityID, name, location, email, description, capacity, organizerID);

            db.collection("facilities").document(newFacilityID)
                    .set(facility)
                    .addOnSuccessListener(aVoid -> {
                        db.collection("AndroidID").document(organizerID).update("facilityID", newFacilityID)
                                .addOnSuccessListener(aVoid2 -> {
                                    // Navigate to organizer page after adding the facility
                                    if (getActivity() != null) {
                                        HeaderNavigation headerNavigation = new HeaderNavigation(getActivity());
                                        headerNavigation.navigateToOrganize();
                                    }

                                    dismiss();
                                });
                    });
        }
    }

    /**
     * An interface for notifying the listener when facility data is updated.
     */
    public interface FacilityUpdateListener {
        /**
         * Called when the facility details are updated.
         *
         * @param name        The updated name of the facility.
         * @param location    The updated location of the facility.
         * @param email       The updated contact email of the facility.
         * @param description The updated description of the facility.
         * @param capacity    The updated maximum capacity of the facility.
         */
        void onFacilityUpdated(String name, String location, String email, String description, int capacity);
    }

    /**
     * Sets the FacilityUpdateListener to handle facility updates.
     *
     * @param listener The listener to be set.
     */
    public void setFacilityUpdateListener(FacilityUpdateListener listener) {
        this.updateListener = listener;
    }
}