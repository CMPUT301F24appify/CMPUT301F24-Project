package com.example.appify.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
 * AddEventDialogFragment is a dialog fragment for adding events.
 * It allows organizers to input event details, perform input validation,
 * upload images to Firebase Storage, and interact with Firestore.
 */
public class AddEventDialogFragment extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image selection
    private String posterUri; // URI of the uploaded poster image
    private AddEventDialogListener listener; // Listener for communicating with parent activity
    private Button uploadPosterButton, buttonEventDate, buttonRegistrationEndDate; // UI buttons
    private boolean isGeolocate = false; // Flag to indicate geolocation status
    private String facilityID; // ID of the facility associated with the event
    private String facilityName; // Name of the facility
    private EditText eventFacility, eventDescription, maxWaitEntrant, maxSampleEntrant; // Input fields
    private Uri selectedImageUri; // URI of the selected image
    private Calendar calendar; // Calendar instance for date selection


    /**
     * Interface for communicating with the parent activity to pass event details.
     */
    public interface AddEventDialogListener {
        /**
         * Callback for adding a new event with the provided details.
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
        void onEventAdded(String name, String date, String facility, String registrationEndDate,
                          String description, int maxWaitEntrants, int maxSampleEntrants,
                          String posterUri, boolean isGeolocate,
                          String waitlistedMessage, String enrolledMessage,
                          String cancelledMessage, String invitedMessage);
    }

    /**
     * Attaches the dialog to the parent activity and checks if the parent implements the callback interface.
     *
     * @param context The context to which the dialog is attached.
     * @throws ClassCastException if the context does not implement AddEventDialogListener.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddEventDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddEventDialogListener");
        }
    }

    /**
     * Creates the dialog, initializes UI components allowing organizer to input details, and sets up event handlers.
     *
     * @param savedInstanceState The saved state of the dialog.
     * @return The created dialog instance.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_event_dialog, null);

        // Initialize Firestore and Calendar
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

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
                .addOnFailureListener(e -> Log.w("MyApp", "Failed to retrieve facilityID", e));;

        // Initialize input fields
        EditText eventName = view.findViewById(R.id.editTextEventName);
        eventFacility = view.findViewById(R.id.editFacility);
        eventDescription = view.findViewById(R.id.editTextEventDescription);
        maxWaitEntrant = view.findViewById(R.id.maxNumberWaitList);
        maxSampleEntrant = view.findViewById(R.id.maxNumberSample);
        buttonEventDate = view.findViewById(R.id.buttonEventDate);
        buttonRegistrationEndDate = view.findViewById(R.id.buttonRegistrationEndDate);
        uploadPosterButton = view.findViewById(R.id.buttonUploadPoster);
        Button reminderGeolocation = view.findViewById(R.id.checkGeolocation);

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
                .setTitle("Add Event")
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
                        // Upload the image
                        uploadImageToFirebase(selectedImageUri, new ImageUploadCallback() {
                            @Override
                            public void onSuccess(String posterUri) {
                                // Proceed after image upload
                                listener.onEventAdded(name, date, facilityID, registrationEndDate, description,
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
                        listener.onEventAdded(name, date, facilityID, registrationEndDate, description,
                                waitMax, sampleMax, null, isGeolocate, "", "", "", "");
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(getContext(), "Please correct the highlighted fields", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return dialog;
    }

    /**
     * Opens a date picker dialog for selecting dates.
     *
     * @param button The button to display the selected date.
     */
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

    /**
     * Validates input fields for completeness and correctness.
     *
     * @param eventName        The event name input field.
     * @param eventFacility    The facility input field.
     * @param eventDescription The description input field.
     * @param maxWaitEntrant   The maximum waitlist entrants input field.
     * @param maxSampleEntrant The maximum sample entrants input field.
     * @return True if all inputs are valid, otherwise false.
     */
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
     * Handles the result of the file chooser intent, capturing the selected image.
     * Updates the UI to indicate that an image has been selected for upload.
     *
     * @param requestCode The request code originally supplied to startActivityForResult.
     * @param resultCode  The result code returned by the file chooser activity.
     * @param data        The intent containing the selected image data.
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
     * Uploads the selected image to Firebase Storage and generates a downloadable URI.
     * Handles success and failure through the provided callback interface.
     *
     * @param imageUri The URI of the image to upload.
     * @param callback The callback to handle success or failure of the upload.
     */
    private void uploadImageToFirebase(Uri imageUri, ImageUploadCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference posterRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

        posterRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> posterRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            posterUri = downloadUri.toString();
                            callback.onSuccess(posterUri);
                        })
                        .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }
}
