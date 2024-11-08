package com.example.appify.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

public class AddEventDialogFragment extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private String posterUri;
    private AddEventDialogListener listener;

    // State variables for buttons
    private boolean isGeolocate = false;

    public interface AddEventDialogListener {
        void onEventAdded(String name, String date, String facility, String registrationEndDate,
                          String description, int maxWishEntrants, int maxSampleEntrants,
                          String posterUri, boolean isGeolocate,
                          String waitlistedMessage, String enrolledMessage,
                          String cancelledMessage, String invitedMessage);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddEventDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddEventDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_event_dialog, null);

        // Retrieve and set facility name from MyApp
        EditText eventFacility = view.findViewById(R.id.editFacility);
        MyApp app = (MyApp) requireActivity().getApplication();
        String facilityName = app.getFacilityName();
        eventFacility.setText(facilityName);
        eventFacility.setEnabled(false);


        EditText eventName = view.findViewById(R.id.editTextEventName);
        EditText eventDate = view.findViewById(R.id.editDate);
        EditText eventregistrationEndDate = view.findViewById(R.id.editRegistrationEndDate);
        EditText eventDescription = view.findViewById(R.id.editTextEventDescription);

        // Update variable type and ID
        Button reminderGeolocation = view.findViewById(R.id.checkGeolocation);

        Button uploadPosterButton = view.findViewById(R.id.buttonUploadPoster);
        EditText maxWishEntrant = view.findViewById(R.id.maxNumberWishList);
        EditText maxSampleEntrant = view.findViewById(R.id.maxNumberSample);

        // Set OnClickListeners for buttons
        reminderGeolocation.setOnClickListener(v -> {
            isGeolocate = !isGeolocate;
            updateButtonAppearance(reminderGeolocation, isGeolocate);
        });

        uploadPosterButton.setOnClickListener(v -> openFileChooser());

        builder.setView(view)
                .setTitle("Add Event")
                .setPositiveButton("CONFIRM", (dialog, id) -> {
                    if (validateInputs(eventName, eventDate, eventFacility, eventregistrationEndDate, maxWishEntrant, maxSampleEntrant)) {
                        String name = eventName.getText().toString();
                        String date = eventDate.getText().toString();
                        String facility = app.getFacilityName();
                        String registrationEndDate = eventregistrationEndDate.getText().toString();
                        String description = eventDescription.getText().toString();

                        int wish_max = parseInteger(maxWishEntrant.getText().toString());
                        int sample_max = parseInteger(maxSampleEntrant.getText().toString());

                        listener.onEventAdded(name, date, facility, registrationEndDate, description,
                                wish_max, sample_max, posterUri, isGeolocate,
                                "", "", "", "");
                        } else {
                            Toast.makeText(getContext(), "Please correct the highlighted fields", Toast.LENGTH_SHORT).show();
                        }
                    })
                .setNegativeButton("CANCEL", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    // Method to validate inputs
    private boolean validateInputs(EditText eventName, EditText eventDate, EditText eventFacility, EditText eventRegistrationEndDate, EditText maxWishEntrant, EditText maxSampleEntrant) {
        boolean isValid = true;

        // Check for non-empty event name
        if (eventName.getText().toString().trim().isEmpty()) {
            eventName.setError("Event name is required");
            isValid = false;
        }

        // Validate date format (assuming yyyy-MM-dd format)
        if (!isValidDate(eventDate.getText().toString())) {
            eventDate.setError("Enter date in 'MMM dd, yyyy' format (e.g., Nov 10, 2022)");
            isValid = false;
        }

        // Validate registration end date format
        if (!isValidDate(eventRegistrationEndDate.getText().toString())) {
            eventRegistrationEndDate.setError("Enter date in 'MMM dd, yyyy' format (e.g., Nov 10, 2022)");
            isValid = false;
        }

        // Validate max entrants as integers within a reasonable range
        if (!isPositiveInteger(maxWishEntrant.getText().toString())) {
            maxWishEntrant.setError("Enter a positive number");
            isValid = false;
        }

        if (!isPositiveInteger(maxSampleEntrant.getText().toString())) {
            maxSampleEntrant.setError("Enter a positive number");
            isValid = false;
        }

        return isValid;
    }

    // Utility to check if a string is a valid positive integer
    private boolean isPositiveInteger(String text) {
        try {
            return Integer.parseInt(text) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Utility to validate date in yyyy-MM-dd format
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

    private void updateButtonAppearance(Button button, boolean isActive) {
        if (isActive) {
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        } else {
            button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private int parseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid number for max entrants", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Poster Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            uploadImageToFirebase(data.getData());
        }
    }

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
