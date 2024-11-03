package com.example.appify;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class AddEventDialogFragment extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri posterUri;
    private AddEventDialogListener listener;

    public interface AddEventDialogListener {
        void onEventAdded(String name, String date, String description, int maxWishEntrants, int maxSampleEntrants, Uri posterUri, boolean isGeolocate);
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
        EditText eventDate = view.findViewById(R.id.editFacility);
        EditText eventDescription = view.findViewById(R.id.editTextEventDescription);
        CheckBox reminderGeolocation = view.findViewById(R.id.checkGeolocation);
        Button uploadPosterButton = view.findViewById(R.id.buttonUploadPoster);
        EditText maxWishEntrant = view.findViewById(R.id.maxNumberWishList);
        EditText maxSampleEntrant = view.findViewById(R.id.maxNumberSample);


        uploadPosterButton.setOnClickListener(v -> openFileChooser());

        builder.setView(view)
                .setTitle("Add Event")
                .setPositiveButton("CONFIRM", (dialog, id) -> {
                    // Get event details from the input fields
                    String name = eventName.getText().toString();
                    String date = eventDate.getText().toString();
                    String description = eventDescription.getText().toString();
                    boolean isGeolocate = reminderGeolocation.isChecked();

                    String maxEntrantWishText = maxWishEntrant.getText().toString();
                    int wish_max = 0; // Default value if no valid input

                    String maxEntrantSampleText = maxSampleEntrant.getText().toString();
                    int sample_max = 0; // Default value if no valid input

                    if (!maxEntrantWishText.isEmpty()) {
                        try {
                            wish_max = Integer.parseInt(maxEntrantWishText);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Please enter a valid number for max entrants", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (!maxEntrantSampleText.isEmpty()) {
                        try {
                            sample_max = Integer.parseInt(maxEntrantSampleText);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Please enter a valid number for max entrants", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Pass data to MainActivity via the listener
                    listener.onEventAdded(name, date, description, wish_max, sample_max, posterUri, isGeolocate);

                    if (posterUri != null) {
                        Toast.makeText(getContext(), "Poster selected: " + posterUri.getPath(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No poster selected", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", (dialog, id) -> dialog.dismiss());

        return builder.create();
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
            posterUri = data.getData();  // Get the Uri of the selected image
            Toast.makeText(getContext(), "Poster file selected!", Toast.LENGTH_SHORT).show();
        }
    }
}
