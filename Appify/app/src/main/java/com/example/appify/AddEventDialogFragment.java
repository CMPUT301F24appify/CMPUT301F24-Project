package com.example.appify;

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

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddEventDialogFragment extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private String posterUri;  // Store as String URL
    private AddEventDialogListener listener;

    // State variables for buttons
    private boolean isGeolocate = false;

//    // Notification message variables
//    private String waitlistedMessage = "";
//    private String enrolledMessage = "";
//    private String cancelledMessage = "";
//    private String invitedMessage = "";

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

        EditText eventName = view.findViewById(R.id.editTextEventName);
        EditText eventDate = view.findViewById(R.id.editDate);
        EditText eventFacility = view.findViewById(R.id.editFacility);
        EditText eventregistrationEndDate = view.findViewById(R.id.editRegistrationEndDate);
        EditText eventDescription = view.findViewById(R.id.editTextEventDescription);

        // Update variable type and ID
        Button reminderGeolocation = view.findViewById(R.id.checkGeolocation);

        Button uploadPosterButton = view.findViewById(R.id.buttonUploadPoster);
        EditText maxWishEntrant = view.findViewById(R.id.maxNumberWishList);
        EditText maxSampleEntrant = view.findViewById(R.id.maxNumberSample);

//        // Update variable types and IDs
//        Button notifyWaitlisted = view.findViewById(R.id.buttonWaitlisted);
//        Button notifyEnrolled = view.findViewById(R.id.buttonEnrolled);
//        Button notifyCancelled = view.findViewById(R.id.buttonCancelled);
//        Button notifyInvited = view.findViewById(R.id.buttonInvited);

        // Set OnClickListeners for buttons
        reminderGeolocation.setOnClickListener(v -> {
            isGeolocate = !isGeolocate;
            updateButtonAppearance(reminderGeolocation, isGeolocate);
        });

//        notifyWaitlisted.setOnClickListener(v -> {
//            showNotificationInputDialog("Waitlisted Notification", waitlistedMessage, message -> {
//                waitlistedMessage = message;
//                updateButtonAppearance(notifyWaitlisted, !message.isEmpty());
//            });
//        });
//
//        notifyEnrolled.setOnClickListener(v -> {
//            showNotificationInputDialog("Enrolled Notification", enrolledMessage, message -> {
//                enrolledMessage = message;
//                updateButtonAppearance(notifyEnrolled, !message.isEmpty());
//            });
//        });
//
//        notifyCancelled.setOnClickListener(v -> {
//            showNotificationInputDialog("Cancelled Notification", cancelledMessage, message -> {
//                cancelledMessage = message;
//                updateButtonAppearance(notifyCancelled, !message.isEmpty());
//            });
//        });
//
//        notifyInvited.setOnClickListener(v -> {
//            showNotificationInputDialog("Invited Notification", invitedMessage, message -> {
//                invitedMessage = message;
//                updateButtonAppearance(notifyInvited, !message.isEmpty());
//            });
//        });

        uploadPosterButton.setOnClickListener(v -> openFileChooser());

        builder.setView(view)
                .setTitle("Add Event")
                .setPositiveButton("CONFIRM", (dialog, id) -> {
                    String name = eventName.getText().toString();
                    String date = eventDate.getText().toString();
                    String facility = eventFacility.getText().toString();
                    String registrationEndDate = eventregistrationEndDate.getText().toString();
                    String description = eventDescription.getText().toString();

                    int wish_max = parseInteger(maxWishEntrant.getText().toString());
                    int sample_max = parseInteger(maxSampleEntrant.getText().toString());

                    listener.onEventAdded(name, date, facility, registrationEndDate, description,
                            wish_max, sample_max, posterUri, isGeolocate,
                            "", "", "", "");
                })
                .setNegativeButton("CANCEL", (dialog, id) -> dialog.dismiss());

        return builder.create();
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

    // Interface for callback after input dialog
    public interface NotificationMessageCallback {
        void onMessageSet(String message);
    }

    private void showNotificationInputDialog(String title, String existingMessage, NotificationMessageCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setText(existingMessage);  // Pre-fill with existing message if any
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String message = input.getText().toString();
            callback.onMessageSet(message);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Delete", (dialog, which) -> {
            callback.onMessageSet("");
            Toast.makeText(getContext(), "Notification message deleted", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }
}
