package com.example.appify.Fragments;

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
    private Button uploadPosterButton;  // Button to trigger poster image upload
    private boolean isGeolocate = false;  // Flag for geolocation status
    private String facilityID;  // Facility ID for the event
    private String facilityName;  // Facility name for the event
    private EditText eventFacility;  // EditText for facility input

    /**
     * Interface for communicating the edited event details to the parent activity.
     */
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
        MyApp app = (MyApp) requireActivity().getApplication();
        String androidId = app.getAndroidId();
        db.collection("AndroidID").document(androidId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    facilityID = documentSnapshot.getString("facilityID");
                    if (facilityID != null && !facilityID.isEmpty()) {
                        db.collection("facilities").document(facilityID)
                                .get()
                                .addOnSuccessListener(documentSnapshot2 -> {
                                    facilityName = documentSnapshot2.getString("name");
                                    eventFacility = view.findViewById(R.id.editFacility);
                                    eventFacility.setText(facilityName);
                                    eventFacility.setEnabled(false);  // Disable editing
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
        EditText eventDate = view.findViewById(R.id.editDate);
        EditText eventregistrationEndDate = view.findViewById(R.id.editRegistrationEndDate);
        EditText eventDescription = view.findViewById(R.id.editTextEventDescription);
        Button reminderGeolocation = view.findViewById(R.id.checkGeolocation);
        uploadPosterButton = view.findViewById(R.id.buttonUploadPoster);
        EditText maxWaitEntrant = view.findViewById(R.id.maxNumberWaitList);
        EditText maxSampleEntrant = view.findViewById(R.id.maxNumberSample);

        // Set OnClickListeners for buttons
        reminderGeolocation.setOnClickListener(v -> {
            isGeolocate = !isGeolocate;
            updateButtonAppearance(reminderGeolocation, isGeolocate);
        });

        uploadPosterButton.setOnClickListener(v -> openFileChooser());

        builder.setView(view)
                .setTitle("Edit Event")
                .setPositiveButton("SAVE", (dialog, id) -> {
                    // Validate inputs before saving
                    if (validateInputs(eventName, eventDate, eventFacility, eventregistrationEndDate, maxWaitEntrant, maxSampleEntrant)) {
                        String name = eventName.getText().toString();
                        String date = eventDate.getText().toString();
                        String facility = facilityName;
                        String registrationEndDate = eventregistrationEndDate.getText().toString();
                        String description = eventDescription.getText().toString();

                        int wait_max = parseInteger(maxWaitEntrant.getText().toString());
                        int sample_max = parseInteger(maxSampleEntrant.getText().toString());

                        listener.onEventEdited(name, date, facility, registrationEndDate, description,
                                wait_max, sample_max, posterUri, isGeolocate,
                                "", "", "", "");
                    } else {
                        Toast.makeText(getContext(), "Please correct the highlighted fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    /**
     * Validates input fields to ensure they meet required formats and values.
     *
     * @param eventName            Event name field.
     * @param eventDate            Event date field.
     * @param eventFacility        Facility field.
     * @param eventRegistrationEndDate Registration end date field.
     * @param maxWaitEntrant       Maximum waitlist entrants field.
     * @param maxSampleEntrant     Maximum sample entrants field.
     * @return True if inputs are valid, otherwise false.
     */
    private boolean validateInputs(EditText eventName, EditText eventDate, EditText eventFacility, EditText eventRegistrationEndDate, EditText maxWaitEntrant, EditText maxSampleEntrant) {
        boolean isValid = true;

        // Check for non-empty event name
        if (eventName.getText().toString().trim().isEmpty()) {
            eventName.setError("Event name is required");
            Toast.makeText(getContext(), "Event name is required.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Check for non-empty facility
        if (eventFacility.getText().toString().trim().isEmpty()) {
            eventFacility.setError("Facility name is required");
            Toast.makeText(getContext(), "Event facility is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate event date format
        if (!isValidDate(eventDate.getText().toString())) {
            eventDate.setError("Enter date in 'MMM dd, yyyy' format (e.g., Nov 10, 2022)");
            Toast.makeText(getContext(), "Enter date in 'MMM dd, yyyy' format for the event date.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate registration end date format
        if (!isValidDate(eventRegistrationEndDate.getText().toString())) {
            eventRegistrationEndDate.setError("Enter date in 'MMM dd, yyyy' format (e.g., Nov 10, 2022)");
            Toast.makeText(getContext(), "Enter date in 'MMM dd, yyyy' format for the registration end date.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Ensure registration end date is before the event date
        if (!isRegistrationEndDateBeforeEventDate(eventDate.getText().toString(), eventRegistrationEndDate.getText().toString())) {
            eventRegistrationEndDate.setError("Registration end date must be before the event date");
            Toast.makeText(getContext(), "Registration end date must be before the event date.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate max entrants fields as positive integers
        if (!isPositiveInteger(maxWaitEntrant.getText().toString())) {
            maxWaitEntrant.setError("Enter a positive number");
            Toast.makeText(getContext(), "Max wait entrants must be a positive number.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isPositiveInteger(maxSampleEntrant.getText().toString())) {
            maxSampleEntrant.setError("Enter a positive number");
            Toast.makeText(getContext(), "Max sample entrants must be a positive number.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Checks if the registration end date is before the event date.
     *
     * @param eventDateStr The event date as a string in "MMM dd, yyyy" format.
     * @param registrationEndDateStr The registration end date as a string in "MMM dd, yyyy" format.
     * @return True if the registration end date is before the event date, otherwise false.
     */
    private boolean isRegistrationEndDateBeforeEventDate(String eventDateStr, String registrationEndDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        sdf.setLenient(false);  // Strict date parsing
        try {
            Date eventDate = sdf.parse(eventDateStr);
            Date registrationEndDate = sdf.parse(registrationEndDateStr);
            return registrationEndDate.before(eventDate);
        } catch (ParseException e) {
            return false;  // Invalid date format
        }
    }

    /**
     * Checks if a given string represents a positive integer.
     *
     * @param text The string to check.
     * @return True if the string is a positive integer, otherwise false.
     */
    private boolean isPositiveInteger(String text) {
        try {
            return Integer.parseInt(text) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
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
     * Parses a string to an integer. Shows a toast message if parsing fails.
     *
     * @param text The text to parse.
     * @return The integer value if parsing is successful, otherwise 0.
     */
    private int parseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid number for max entrants", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    /**
     * Opens a file chooser to select an image for the event poster.
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Poster Image"), PICK_IMAGE_REQUEST);
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
            uploadImageToFirebase(data.getData());
            uploadPosterButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    /**
     * Uploads the selected image to Firebase storage and sets the poster URI.
     *
     * @param imageUri The URI of the image to upload.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference posterRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

        posterRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            posterRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                posterUri = downloadUri.toString();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Poster uploaded successfully!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to upload poster: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
