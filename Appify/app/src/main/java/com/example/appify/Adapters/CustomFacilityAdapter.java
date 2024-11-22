package com.example.appify.Adapters;

import android.app.AlertDialog;
import android.content.Context;
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
                .setMessage("Please confirm that you want to delete: " + facility.getName() + ". This action cannot be undone.")
                .setPositiveButton("Confirm", ((dialog, which) -> {
                    db.collection("facilities").document(facility.getId())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String organizerID = documentSnapshot.getString("organizerID");

                                    db.collection("facilities").document(facility.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                if (organizerID != null) {
                                                    db.collection("Android ID").document(organizerID)
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
