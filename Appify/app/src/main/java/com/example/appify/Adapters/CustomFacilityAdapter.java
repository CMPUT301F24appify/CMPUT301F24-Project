package com.example.appify.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.appify.Model.Facility;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * Custom adapter to display facilities in a ListView.
 */
public class CustomFacilityAdapter extends ArrayAdapter<Facility> {
    private Context context;
    private List<Facility> facilityList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CustomFacilityAdapter(Context context, List<Facility> facilityList) {
        super(context, 0, facilityList);
        this.context = context;
        this.facilityList = facilityList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.facility_list_content, parent, false);
        }

        Facility facility = facilityList.get(position);

        TextView facilityName = convertView.findViewById(R.id.facility_name);
        TextView facilityLocation = convertView.findViewById(R.id.facility_location);
        TextView facilityCapacity = convertView.findViewById(R.id.facility_capacity);
        ImageView xIcon = convertView.findViewById(R.id.x_icon);

        facilityName.setText(facility.getName());
        facilityLocation.setText(facility.getLocation());
        facilityCapacity.setText(String.format("Capacity: %s", facility.getCapacity()));

        xIcon.setOnClickListener(v -> {
            showCancelFacilityDialog(facility);
        });

        return convertView;
    }

    private void showCancelFacilityDialog(Facility facility) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder
                .setTitle("Delete Facility: " + facility.getName())
                .setMessage("Please confirm that you want to delete: " + facility.getName() + ". ALL EVENTS at this facility will also be deleted. This action cannot be undone.")
                .setPositiveButton("Confirm", ((dialog, which) -> {
                    // Fetch the facility from Firestore
                    db.collection("facilities").document(facility.getId())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String organizerID = documentSnapshot.getString("organizerID");

                                    // Fetch events inside facilities/facilityID/events
                                    db.collection("facilities").document(facility.getId()).collection("events")
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                                                    String eventID = eventDoc.getId();

                                                    // Check for and process the waitingList for the event
                                                    db.collection("events").document(eventID).collection("waitingList")
                                                            .get()
                                                            .addOnSuccessListener(waitingListSnapshot -> {
                                                                for (QueryDocumentSnapshot waitDoc : waitingListSnapshot) {
                                                                    String userID = waitDoc.getId();

                                                                    // Delete the eventID from the user's waitListedEvents collection
                                                                    db.collection("AndroidID").document(userID)
                                                                            .collection("waitListedEvents")
                                                                            .document(eventID)
                                                                            .delete();

                                                                    // Delete the individual waitingList entry
                                                                    db.collection("events").document(eventID).collection("waitingList")
                                                                            .document(userID)
                                                                            .delete();
                                                                }
                                                            })
                                                            .addOnCompleteListener(waitingListTask -> {
                                                                // Delete event from 'events' collection
                                                                db.collection("events").document(eventID)
                                                                        .delete();

                                                                // Delete event document from 'facilities/facilityID/events' collection
                                                                db.collection("facilities").document(facility.getId()).collection("events")
                                                                        .document(eventID)
                                                                        .delete();
                                                            });
                                                }
                                            })
                                            .addOnCompleteListener(task -> {
                                                // Once all events are processed, delete the facility itself
                                                db.collection("facilities").document(facility.getId())
                                                        .delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            if (organizerID != null) {
                                                                // Update organizer's facility ID to null
                                                                db.collection("AndroidID").document(organizerID)
                                                                        .update("facilityID", null)
                                                                        .addOnSuccessListener(aVoid2 -> {
                                                                            facilityList.remove(facility);
                                                                            notifyDataSetChanged();
                                                                        });
                                                            } else {
                                                                facilityList.remove(facility);
                                                                notifyDataSetChanged();
                                                            }
                                                        });
                                            });
                                }
                            });
                }))
                .setNegativeButton("Cancel", ((dialog, which) -> {
                    dialog.dismiss();
                }));
        builder.create();
        builder.show();
    }
}
