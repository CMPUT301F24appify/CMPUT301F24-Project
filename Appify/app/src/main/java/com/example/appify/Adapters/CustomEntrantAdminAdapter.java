
package com.example.appify.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.appify.Model.Event;
import com.example.appify.Model.Entrant;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class CustomEntrantAdminAdapter extends ArrayAdapter<Entrant> {
    private Context context;
    private List<Entrant> entrantList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CustomEntrantAdminAdapter(Context context, List<Entrant> entrantList) {
        super(context, 0, entrantList);
        this.context = context;
        this.entrantList = entrantList;
    }


    /**
     * Provides a view for an AdapterView (ListView) for each event.
     * Sets event details in the view and fetches status data from Firebase for display.
     *
     * @param position    Position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return The View corresponding to the data at the specified position.
     */
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.entrant_admin_list, parent, false);
        }

        MyApp app = (MyApp) context.getApplicationContext();

        Entrant entrant = entrantList.get(position);
        String entrantID = entrant.getId();
        TextView email = convertView.findViewById(R.id.email);
        TextView phoneNumber = convertView.findViewById(R.id.phone);
        TextView name = convertView.findViewById(R.id.entrant_name_admin);
        ImageView statusIcon = convertView.findViewById(R.id.statusIcon);
        ConstraintLayout eventCard = convertView.findViewById(R.id.event_information);
        ImageView xIcon = convertView.findViewById(R.id.x_icon);


        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + entrantID + ".jpg");
        long size = 1024 * 1024;
        storageRef.getBytes(size).addOnSuccessListener(bytes -> {
            // Convert the byte array to a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            statusIcon.setImageBitmap(bitmap);
        });
        email.setText("Email: " + entrant.getEmail());
        name.setText(entrant.getName());
        if (entrant.getPhoneNumber() != "") {
            phoneNumber.setVisibility(View.VISIBLE);
            phoneNumber.setText("Phone Number: " + entrant.getPhoneNumber());
        }
        statusIcon.setVisibility(View.VISIBLE);

        // Delete Button
        xIcon.setOnClickListener(v -> {
            showCancelProfileDialog(entrant);

        });

        return convertView;
    }

    private void showCancelProfileDialog(Entrant entrant) {
        // Check if the user is an admin before proceeding
        db.collection("AndroidID").document(entrant.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getBoolean("isAdmin") != null && documentSnapshot.getBoolean("isAdmin")) {
                        // Make the Toast that the user is an Admin and cannot be deleted
                        Toast.makeText(context, entrant.getName() + " is an admin and cannot be deleted.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Delete the Profile
                        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                        builder.setTitle("Delete User: " + entrant.getName())
                                .setMessage("Please confirm that you want to delete: " + entrant.getName() + ". Entrant will be removed from all waiting lists. If the entrant owns a facility, ALL events at that facility as well as the facility will be deleted. This action cannot be undone.")
                                .setPositiveButton("Confirm", (dialog, which) -> {
                                    // 1. Determine if the user is enrolled in any waitingList, if so delete
                                    db.collection("AndroidID").document(entrant.getId()).collection("waitListedEvents")
                                            .get()
                                            .addOnSuccessListener(waitListedSnapshot -> {
                                                for (QueryDocumentSnapshot eventDoc : waitListedSnapshot) {
                                                    String eventID = eventDoc.getId();

                                                    // Enter the event, and delete the user from the waitingList
                                                    db.collection("events").document(eventID).collection("waitingList")
                                                            .document(entrant.getId())
                                                            .delete();

                                                    // Delete the event from the users waitListedEvents
                                                    db.collection("AndroidID").document(entrant.getId()).collection("waitListedEvents")
                                                            .document(eventID)
                                                            .delete();
                                                }
                                            });
                                    // 2. Determine if the user has a facility, if so cascade delete
                                    if (entrant.getFacilityID() != null) {
                                        db.collection("facilities").document(entrant.getFacilityID())
                                                .get()
                                                .addOnSuccessListener(documentSnapshot2 -> {
                                                    if (documentSnapshot2.exists()) {

                                                        // Search if the Facility has any events
                                                        db.collection("facilities").document(entrant.getFacilityID()).collection("events")
                                                                .get()
                                                                .addOnSuccessListener(querySnapshot -> {
                                                                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                                                                        String eventID = eventDoc.getId();

                                                                        // Check for and process the waitingList for the event
                                                                        db.collection("events").document(eventID).collection("waitingList")
                                                                                .get()
                                                                                .addOnSuccessListener(waitingListSnapshot -> {
                                                                                    for (QueryDocumentSnapshot userDoc : waitingListSnapshot) {
                                                                                        String userID = userDoc.getId();

                                                                                        // Delete the eventID from the user's waitListedEvents collection
                                                                                        db.collection("AndroidID").document(userID)
                                                                                                .collection("waitListedEvents")
                                                                                                .document(eventID)
                                                                                                .delete();

                                                                                        // Delete the individual from the waitingList entry
                                                                                        db.collection("events").document(eventID)
                                                                                                .collection("waitingList")
                                                                                                .document(userID)
                                                                                                .delete();
                                                                                    }
                                                                                })
                                                                                .addOnCompleteListener(waitingListTask -> {
                                                                                    // Delete event from 'events' collection
                                                                                    db.collection("events").document(eventID)
                                                                                            .delete();

                                                                                    // Delete event document from 'facilities/facilityID/events' collection
                                                                                    db.collection("facilities").document(entrant.getFacilityID()).collection("events")
                                                                                            .document(eventID)
                                                                                            .delete();
                                                                                });
                                                                    }
                                                                })
                                                                .addOnCompleteListener(task -> {
                                                                    // Once all events are processed, delete the facility itself
                                                                    db.collection("facilities").document(entrant.getFacilityID())
                                                                            .delete();
                                                                });
                                                    }
                                                });

                                    }
                                    // 3. Delete the user
                                    db.collection("AndroidID").document(entrant.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                entrantList.remove(entrant);
                                                notifyDataSetChanged();
                                            });
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                        builder.create();
                        builder.show();
                    }
                });
    }
}

