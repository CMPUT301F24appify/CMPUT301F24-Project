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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Fragment dialog for editing an event, allowing users to modify event details.
 * This fragment performs input validation and uploads images to Firebase Storage.
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
    private Uri selectedImageUri; // Store the selected image URI

    private Calendar calendar;  // Calendar instance for date pickers

    public interface EditEventDialogListener {
        /**
         * Callback for editing an event with the provided details.
         *
         * @param name              Name of the event.
         * @param date              Date of the event.
         * @param facility          Facility for the event.
         * @param registrationEndDate Registration end date.
         * @param description       Description of the event.
         * @param maxWaitEntrants   Maximum number of waitlist entrants.
         * @param maxSampleEntrants Maximum number of sample entrants.
         * @param posterUri         URI of the uploaded poster image.
         * @param isGeolocate       Geolocation status.
         * @param waitlistedMessage Notification message for waitlisted entrants.
         * @param enrolledMessage   Notification message for enrolled entrants.
         * @param cancelledMessage  Notification message for cancelled entrants.
         * @param invitedMessage    Notification message for invited entrants.
         */
        void onEventEdited(String name, String date, String facility, String registrationEndDate,
                           String description, int maxWaitEntrants, int maxSampleEntrants,
                           String posterUri, boolean isGeolocate,
                           String waitlistedMessage, String enrolledMessage,
                           String cancelledMessage, String invitedMessage);
    }

    /**
     * Attaches the fragment to the parent activity and verifies that the parent implements
     * the EditEventDialogListener callback interface.
     *
     * @param context The context to which the fragment is attached.
     * @throws ClassCastException if the parent context does not implement EditEventDialogListener.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EditEventDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EditEventDialogListener");
        }
    }

    /**
     * Creates the dialog, initializes UI components, retrieves the facility name, and sets up button listeners.
     *
     * @param savedInstanceState The saved state of the fragment.
     * @return The created dialog instance.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_event_dialog, null);



        // Retrieve and set facility name from MyApp
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        // Retrieve event ID from arguments
        Bundle args = getArguments();
        String eventID = args != null ? args.getString("eventID") : null;

        // Get facility info
        MyApp app = (MyApp) requireActivity().getApplication();
        String androidId = app.getAndroidId();
        db.collection("AndroidID").document(androidId)
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
                                    if (facilityName == null) {
                                        Log.w("MyApp", "Facility name not found for facilityID: " + facilityID);
                                        facilityName = "No facility assigned";
                                    }
                                })
                                .addOnFailureListener(e -> Log.w("MyApp", "Failed to retrieve facility name", e));
                    } else {
                        Log.w("MyApp", "No facilityID found for this AndroidID");
                    }
                })
                .addOnFailureListener(e -> Log.w("MyApp", "Failed to retrieve facilityID", e));

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

        if (isGeolocate || !isGeolocate) {
            reminderGeolocation.setVisibility(View.GONE);
        }

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
                            if (documentSnapshot.getLong("maxWaitEntrants") == Integer.MAX_VALUE) {
                                maxWaitEntrant.setText("No Limit");
                            } else {
                                maxWaitEntrant.setText(String.valueOf(documentSnapshot.getLong("maxWaitEntrants")));
                            }
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

        // Build dialog
        builder.setView(view)
                .setTitle("Edit Event")
                .setPositiveButton("CONFIRM", null) // Set to null to override later
                .setNegativeButton("CANCEL", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Override the positive button listener
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (validateInputs(eventName, eventFacility, eventDescription, maxWaitEntrant, maxSampleEntrant)) {
                    String name = eventName.getText().toString();
                    String date = buttonEventDate.getText().toString();
                    String registrationEndDate = buttonRegistrationEndDate.getText().toString();
                    String description = eventDescription.getText().toString();
                    final int waitMax;
                    final int sampleMax;


                    // Parse maxWaitEntrant
                    if (!maxWaitEntrant.getText().toString().trim().isEmpty()) {
                        waitMax = Integer.parseInt(maxWaitEntrant.getText().toString().trim());
                    } else {
                        waitMax = Integer.MAX_VALUE; // Default value if not provided
                    }

                    // Parse maxSampleEntrant
                    if (!maxSampleEntrant.getText().toString().trim().isEmpty()) {
                        sampleMax = Integer.parseInt(maxSampleEntrant.getText().toString().trim());
                    } else {
                        sampleMax = 0; // Default value
                    }

                    positiveButton.setEnabled(false); // Disable to prevent multiple clicks

                    if (selectedImageUri != null) {
                        // Show a progress indicator if desired
                        // Upload the image
                        uploadImageToFirebase(selectedImageUri, new ImageUploadCallback() {
                            @Override
                            public void onSuccess(String posterUri) {
                                // Proceed after image upload
                                listener.onEventEdited(name, date, facilityID, registrationEndDate, description,
                                        waitMax, sampleMax, posterUri, isGeolocate, "", "", "", "");
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                positiveButton.setEnabled(true); // Re-enable on failure
                                Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // No image selected; proceed without posterUri
                        listener.onEventEdited(name, date, facilityID, registrationEndDate, description,
                                waitMax, sampleMax, posterUri, isGeolocate, "", "", "", "");
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(getContext(), "Please correct the highlighted fields", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return dialog;
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
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private boolean validateInputs(EditText eventName, EditText eventFacility, EditText eventDescription, EditText maxWaitEntrant, EditText maxSampleEntrant) {
        boolean isValid = true;
        int waitMax = Integer.MAX_VALUE;
        int sampleMax = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        if (eventName.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Event name is required", Toast.LENGTH_SHORT).show();
            eventName.setError("Event name is required");
            isValid = false;
        }
        if (eventFacility.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Facility is required", Toast.LENGTH_SHORT).show();
            eventFacility.setError("Facility is required");
            isValid = false;
        }
        if (eventDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Description is required", Toast.LENGTH_SHORT).show();
            eventDescription.setError("Description is required");
            isValid = false;
        }
        if (buttonEventDate.getText().toString().equals("Event Date")) {
            Toast.makeText(getContext(), "Please select an event date", Toast.LENGTH_SHORT).show();
            buttonEventDate.setError("Please select an event date");
            isValid = false;
        }
        if (buttonRegistrationEndDate.getText().toString().equals("Registration End Date")) {
            Toast.makeText(getContext(), "Please select a registration end date", Toast.LENGTH_SHORT).show();
            buttonRegistrationEndDate.setError("Please select a registration end date");
            isValid = false;
        }
        // Validate maxSampleEntrant
        if (maxSampleEntrant.getText().toString().trim().isEmpty()) {
            maxSampleEntrant.setError("Please enter max sample entrants");
            isValid = false;
        } else {
            try {
                sampleMax = Integer.parseInt(maxSampleEntrant.getText().toString().trim());
                if (sampleMax <= 0) {
                    maxSampleEntrant.setError("Please enter a positive number");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                maxSampleEntrant.setError("Invalid number");
                isValid = false;
            }
        }
        // Validate maxWaitEntrant
        if (maxWaitEntrant.getText().toString().trim().isEmpty()) {
            waitMax = Integer.MAX_VALUE; // Optional field
        } else {
            try {
                waitMax = Integer.parseInt(maxWaitEntrant.getText().toString().trim());
                if (waitMax <= 0) {
                    maxWaitEntrant.setError("Please enter a positive number");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                maxWaitEntrant.setError("Invalid number");
                isValid = false;
            }
        }
        if (waitMax != Integer.MAX_VALUE) {
            if (sampleMax > waitMax) {
                Toast.makeText(getContext(), "Max sample entrants cannot exceed max waitlist entrants (" + waitMax + ")", Toast.LENGTH_SHORT).show();
                maxSampleEntrant.setError("Cannot exceed max waitlist entrants");
                isValid = false;
            }
        }
        // Validate date order
        if (isValidDate(buttonEventDate.getText().toString()) && isValidDate(buttonRegistrationEndDate.getText().toString())) {
            try {
                Date eventDate = sdf.parse(buttonEventDate.getText().toString());
                Date registrationEndDate = sdf.parse(buttonRegistrationEndDate.getText().toString());
                if (registrationEndDate.after(eventDate)) {
                    Toast.makeText(getContext(), "Registration end date must be before or on the event date", Toast.LENGTH_SHORT).show();
                    buttonEventDate.setError("Event date is before Registration end date");
                    buttonRegistrationEndDate.setError("Registration end date is after Event Date");
                    isValid = false;
                }
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        }
        return isValid;
    }


    /**
     * Validates if a given string is in a valid date format ("MMM dd, yyyy").
     *
     * @param date The date string to validate.
     * @return True if the date is valid, otherwise false.
     */
    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Opens a file chooser to select an image for the event poster.
     * Launches an intent to allow the user to choose an image from their device's storage.
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Poster Image"), PICK_IMAGE_REQUEST);
    }

    /**
     * Updates the button's appearance based on its active state.
     *
     * @param button The button to update.
     * @param isActive True if the button should appear active, otherwise false.
     */
    private void updateButtonAppearance(Button button, boolean isActive) {
        if (isActive) {
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        } else {
            button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    /**
     * Handles the result of the file chooser intent, uploading the selected image to Firebase.
     *
     * @param requestCode The request code originally supplied to startActivityForResult.
     * @param resultCode  The result code returned by the child activity.
     * @param data        The intent containing the result data.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadPosterButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    /**
     * Interface for handling image upload callbacks.
     * Provides methods to handle success or failure when uploading an image to Firebase Storage.
     */
    public interface ImageUploadCallback {

        /**
         * Called when the image upload is successful.
         *
         * @param posterUri The URI of the uploaded image.
         */
        void onSuccess(String posterUri);

        /**
         * Called when the image upload fails.
         *
         * @param e The exception representing the failure.
         */
        void onFailure(Exception e);
    }

    /**
     * Uploads the selected image to Firebase storage and sets the poster URI.
     *
     * @param imageUri The URI of the image to upload.
     */
    private void uploadImageToFirebase(Uri imageUri, ImageUploadCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference posterRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

        posterRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> posterRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String posterUri = downloadUri.toString();
                            callback.onSuccess(posterUri);
                        })
                        .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }
}
