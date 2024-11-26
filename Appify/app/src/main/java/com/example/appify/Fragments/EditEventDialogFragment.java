package com.example.appify.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * Fragment dialog for editing an event, allowing users to modify event details.
 */
public class EditEventDialogFragment extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private String posterUri;  // URI of the event poster
    private EditEventDialogListener listener;  // Listener to handle event edit callback
    private Button uploadPosterButton, buttonEventDate, buttonRegistrationEndDate;  // Buttons for selecting dates
    private boolean isGeolocate = false;  // Flag for geolocation status
    private String facilityID;  // Facility ID for the event
    private String facilityName;  // Facility name for the event
    private EditText eventFacility, eventDescription, maxWaitEntrant, maxSampleEntrant;  // EditTexts for other fields

    private Calendar calendar;  // Calendar instance for date pickers

    public interface EditEventDialogListener {
        void onEventEdited(String name, String date, String facility, String registrationEndDate,
                           String description, int maxWaitEntrants, int maxSampleEntrants,
                           String posterUri, boolean isGeolocate,
                           String waitlistedMessage, String enrolledMessage,
                           String cancelledMessage, String invitedMessage);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EditEventDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EditEventDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_event_dialog, null);

        // Initialize Firestore and Calendar
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        // Retrieve event ID from arguments
        Bundle args = getArguments();
        String eventID = args != null ? args.getString("eventID") : null;

        // Get facility info
        MyApp app = (MyApp) requireActivity().getApplication();
        String androidId = app.getAndroidId();
        db.collection("Android ID").document(androidId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    facilityID = documentSnapshot.getString("facilityID");
                    if (facilityID != null && !facilityID.isEmpty()) {
                        db.collection("facilities").document(facilityID)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    facilityName = doc.getString("name");
                                    eventFacility = view.findViewById(R.id.editFacility);
                                    eventFacility.setText(facilityName);
                                    eventFacility.setEnabled(false);
                                    eventFacility.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                });
                    }
                });

        // Initialize form input fields
        EditText eventName = view.findViewById(R.id.editTextEventName);
        eventFacility = view.findViewById(R.id.editFacility);
        eventDescription = view.findViewById(R.id.editTextEventDescription);
        maxWaitEntrant = view.findViewById(R.id.maxNumberWaitList);
        maxSampleEntrant = view.findViewById(R.id.maxNumberSample);
        buttonEventDate = view.findViewById(R.id.buttonEventDate);
        buttonRegistrationEndDate = view.findViewById(R.id.buttonRegistrationEndDate);
        uploadPosterButton = view.findViewById(R.id.buttonUploadPoster);
        Button reminderGeolocation = view.findViewById(R.id.checkGeolocation);

        // Fetch existing event details and populate fields
        if (eventID != null) {
            db.collection("events").document(eventID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            eventName.setText(documentSnapshot.getString("name"));
                            buttonEventDate.setText(documentSnapshot.getString("date"));
                            buttonRegistrationEndDate.setText(documentSnapshot.getString("registrationEndDate"));
                            eventDescription.setText(documentSnapshot.getString("description"));
                            maxWaitEntrant.setText(String.valueOf(documentSnapshot.getLong("maxWaitEntrants")));
                            maxSampleEntrant.setText(String.valueOf(documentSnapshot.getLong("maxSampleEntrants")));
                            isGeolocate = documentSnapshot.getBoolean("isGeolocate") != null &&
                                    documentSnapshot.getBoolean("isGeolocate");
                            updateButtonAppearance(reminderGeolocation, isGeolocate);
                            posterUri = documentSnapshot.getString("posterUri");
                        }
                    })
                    .addOnFailureListener(e -> {
                    });
        }

        // Set up date pickers
        buttonEventDate.setOnClickListener(v -> openDatePicker(buttonEventDate));
        buttonRegistrationEndDate.setOnClickListener(v -> openDatePicker(buttonRegistrationEndDate));

        // Geolocation toggle
        reminderGeolocation.setOnClickListener(v -> {
            isGeolocate = !isGeolocate;
            updateButtonAppearance(reminderGeolocation, isGeolocate);
        });

        // Upload poster button
        uploadPosterButton.setOnClickListener(v -> openFileChooser());

        builder.setView(view)
                .setTitle("Edit Event")
                .setPositiveButton("SAVE", (dialog, id) -> {
                    if (validateInputs(eventName, eventFacility, eventDescription)) {
                        String name = eventName.getText().toString();
                        String date = buttonEventDate.getText().toString();
                        String registrationEndDate = buttonRegistrationEndDate.getText().toString();
                        String description = eventDescription.getText().toString();
                        int waitMax = parseInteger(maxWaitEntrant.getText().toString());
                        int sampleMax = parseInteger(maxSampleEntrant.getText().toString());

                        listener.onEventEdited(name, date, facilityID, registrationEndDate, description,
                                waitMax, sampleMax, posterUri, isGeolocate, "", "", "", "");
                    } else {
                        Toast.makeText(getContext(), "Please correct the highlighted fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void openDatePicker(Button button) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.getTime());
                    button.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private boolean validateInputs(EditText eventName, EditText eventFacility, EditText eventDescription) {
        boolean isValid = true;
        if (eventName.getText().toString().trim().isEmpty()) {
            eventName.setError("Event name is required");
            isValid = false;
        }
        if (eventFacility.getText().toString().trim().isEmpty()) {
            eventFacility.setError("Facility is required");
            isValid = false;
        }
        if (eventDescription.getText().toString().trim().isEmpty()) {
            eventDescription.setError("Description is required");
            isValid = false;
        }
        if (buttonEventDate.getText().toString().equals("Select Event Date")) {
            Toast.makeText(getContext(), "Please select an event date", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (buttonRegistrationEndDate.getText().toString().equals("Select Registration End Date")) {
            Toast.makeText(getContext(), "Please select a registration end date", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }

    private int parseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Poster Image"), PICK_IMAGE_REQUEST);
    }

    private void updateButtonAppearance(Button button, boolean isActive) {
        button.setBackgroundColor(isActive ?
                getResources().getColor(android.R.color.holo_blue_light) :
                getResources().getColor(android.R.color.darker_gray));
    }
}
