package com.example.appify.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

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
 * Fragment dialog for adding an event, allowing organizers to input event details.
 * This fragment performs input validation and uploads images to Firebase storage.
 */
public class AddEventDialogFragment extends DialogFragment {
    private static final int Pick_Image_Request = 1;
    private String posterUri;  // URI for the poster image
    private AddEventDialogListener listener;  // Listener for communicating with parent activity
    private Button uploadPosterButton, buttonEventDate, buttonRegistrationEndDate;  // Buttons for selecting dates
    private boolean isGeolocate = false;  // Flag to indicate geolocation status
    private String facilityID;  // ID for the facility
    private String facilityName;  // Name of the facility
    private EditText eventFacility, eventDescription, maxWaitEntrant, maxSampleEntrant;  // EditTexts for other fields

    private Calendar calendar;  // Calendar instance for date pickers

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
                });

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
                .setPositiveButton("CONFIRM", (dialog, id) -> {
                    if (validateInputs(eventName, eventFacility, eventDescription)) {
                        String name = eventName.getText().toString();
                        String date = buttonEventDate.getText().toString();
                        String registrationEndDate = buttonRegistrationEndDate.getText().toString();
                        String description = eventDescription.getText().toString();
                        int waitMax = parseInteger(maxWaitEntrant.getText().toString());
                        int sampleMax = parseInteger(maxSampleEntrant.getText().toString());
                        listener.onEventAdded(name, date, facilityID, registrationEndDate, description,
                                waitMax, sampleMax, posterUri, isGeolocate, "", "", "", "");
                    } else {
                        //Toast.makeText(getContext(), "Please correct the highlighted fields", Toast.LENGTH_SHORT).show();
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
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private boolean validateInputs(EditText eventName, EditText eventFacility, EditText eventDescription) {
        boolean isValid = true;
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
            isValid = false;
        }
        if (buttonRegistrationEndDate.getText().toString().equals("Registration End Date")) {
            Toast.makeText(getContext(), "Please select a registration end date", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        try {
            Date eventDate = sdf.parse(buttonEventDate.getText().toString());
            Date registrationEndDate = sdf.parse(buttonRegistrationEndDate.getText().toString());

            if (registrationEndDate.after(eventDate)) {
                Toast.makeText(getContext(), "Registration end date must be before or on the event date", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        } catch (ParseException e) {
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
        sdf.setLenient(false); // Strict date parsing
        try {
            Date eventDate = sdf.parse(eventDateStr);
            Date registrationEndDate = sdf.parse(registrationEndDateStr);
            return registrationEndDate.before(eventDate); // Check if registration end date is before event date
        } catch (ParseException e) {
            return false; // Invalid date format
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
        startActivityForResult(Intent.createChooser(intent, "Select Poster Image"), Pick_Image_Request);
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

        if (requestCode == Pick_Image_Request && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
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
                Toast.makeText(getContext(), "Poster uploaded successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to upload poster: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
