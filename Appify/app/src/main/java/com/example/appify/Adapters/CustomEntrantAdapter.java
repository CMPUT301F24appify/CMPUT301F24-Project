package com.example.appify.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.signature.ObjectKey;
import com.example.appify.Activities.EventEntrantsActivity;
import com.example.appify.Model.Entrant;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CustomEntrantAdapter extends ArrayAdapter<Entrant> {
    private Context context;
    private List<Entrant> entrantList;
    private String eventID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CustomEntrantAdapter(Context context, List<Entrant> entrantList, String eventID){
        super(context,0,entrantList);
        this.context = context;
        this.entrantList = entrantList;
        this.eventID = eventID;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.entrant_list_content, parent, false);
        }

        Entrant entrant = entrantList.get(position);
        String entrantID = entrant.getId();

        View finalConvertView = convertView;
        ImageView xIcon = finalConvertView.findViewById(R.id.x_icon);

        db.collection("Android ID").document(entrantID).collection("waitListedEvents").document(eventID).get().addOnSuccessListener(DocumentSnapshot -> {
            String status = DocumentSnapshot.getString("status");
            TextView entrantStatusView = finalConvertView.findViewById(R.id.entrant_status_text);

            if (Objects.equals(status, "enrolled")){
                // Set status text to orange if status is enrolled, hides the x button
                entrantStatusView.setTextColor(Color.parseColor("#FFA500"));
                entrantStatusView.setText("Enrolled");
                xIcon.setVisibility(View.GONE);
            } else if (Objects.equals(status,"invited")) {
                // Set status text to blue if status is invited, shows the x button
                entrantStatusView.setTextColor(Color.parseColor("#ADD8E6"));
                entrantStatusView.setText("Invited");
                xIcon.setVisibility(View.VISIBLE);
            } else if (Objects.equals(status, "rejected")) {
                // Set status text to red if status is rejected, hides the x button
                entrantStatusView.setTextColor(Color.parseColor("#FF0000"));
                entrantStatusView.setText("Rejected");
                xIcon.setVisibility(View.GONE);
            } else if (Objects.equals(status, "accepted")) {
                // Set status text to green if status is accepted, hides the x button
                entrantStatusView.setTextColor(Color.parseColor("#008000"));
                entrantStatusView.setText("Accepted");
                xIcon.setVisibility(View.GONE);
            }
        });

        TextView entrantNameView = convertView.findViewById(R.id.entrant_name_text);

        xIcon.setOnClickListener(v -> {
            showCancelUserDialog(entrant);
            System.out.println("press button");
        });

        entrantNameView.setText(entrant.getName());
        return convertView;
    }

    private void showCancelUserDialog(Entrant entrant){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder
                .setTitle("Reject User: " + entrant.getName())
                .setMessage("Confirmation of Entrant Rejection: " + entrant.getName())
                .setPositiveButton("Confirm", ((dialog, which) -> {
                    HashMap<String, Object> newStatusData = new HashMap<>();
                    newStatusData.put("status", "rejected");
                    db.collection("events").document(eventID).collection("waitingList").document(entrant.getId()).set(newStatusData);
                    db.collection("Android ID").document(entrant.getId()).collection("waitListedEvents").document(eventID).set(newStatusData);

                    if (context instanceof EventEntrantsActivity){
                        EventEntrantsActivity activity = (EventEntrantsActivity) context;
                        activity.reloadData(eventID);
                    }

                }))
                .setNegativeButton("Cancel",(((dialog, which) -> {
                    dialog.dismiss();
                })));
        builder.create();
        builder.show();
    }

}
