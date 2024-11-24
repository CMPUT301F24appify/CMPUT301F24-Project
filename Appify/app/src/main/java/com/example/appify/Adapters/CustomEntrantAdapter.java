package com.example.appify.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.appify.Activities.EventEntrantsActivity;
import com.example.appify.Model.Entrant;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The {@code CustomEntrantAdapter} is a custom ArrayAdapter for displaying entrant information
 * in a ListView. It manages each entrant's data and status for a specific event and handles status
 * changes through a Firebase Firestore database.
 */
public class CustomEntrantAdapter extends ArrayAdapter<Entrant> {
    private Context context;
    private List<Entrant> entrantList;
    private String eventID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * Constructs a new {@code CustomEntrantAdapter} instance.
     *
     * @param context     the context in which the adapter is used.
     * @param entrantList the list of entrants to display.
     * @param eventID     the ID of the event associated with the entrants.
     */
    public CustomEntrantAdapter(Context context, List<Entrant> entrantList, String eventID){
        super(context,0,entrantList);
        this.context = context;
        this.entrantList = entrantList;
        this.eventID = eventID;

    }

    /**
     * Returns a view for each entrant in the list, setting the entrant's name and status and configuring
     * an 'X' icon that allows the user to reject the entrant when clicked, if their status is invited.
     *
     * @param position    the position of the item within the adapter's data set.
     * @param convertView the old view to reuse.
     * @param parent      the parent view that this view will be attached to.
     * @return the view for the specific position in the list.
     */
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
        LinearLayout entrantCard = finalConvertView.findViewById(R.id.entrant_card);

        db.collection("Android ID").document(entrantID).collection("waitListedEvents").document(eventID).get().addOnSuccessListener(DocumentSnapshot -> {
            String status = DocumentSnapshot.getString("status");
            TextView entrantStatusView = finalConvertView.findViewById(R.id.entrant_status_text);
            if (Objects.equals(status, "enrolled")){
                // Set status text to orange if status is enrolled, hides the x button
                entrantStatusView.setTextColor(Color.parseColor("#99431f"));
                entrantStatusView.setText("Enrolled");
                entrantCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFDBBB")));
                xIcon.setVisibility(View.GONE);
            } else if (Objects.equals(status,"invited")) {
                // Set status text to blue if status is invited, shows the x button
                entrantStatusView.setTextColor(Color.parseColor("#00008b"));
                entrantStatusView.setText("Invited");
                entrantCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#add8e6")));
                xIcon.setVisibility(View.VISIBLE);
            } else if (Objects.equals(status, "rejected")) {
                // Set status text to red if status is rejected, hides the x button
                entrantStatusView.setTextColor(Color.parseColor("#8b0000"));
                entrantStatusView.setText("Rejected");
                entrantCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffc0c0")));
                xIcon.setVisibility(View.GONE);
            } else if (Objects.equals(status, "accepted")) {
                // Set status text to green if status is accepted, hides the x button
                entrantStatusView.setTextColor(Color.parseColor("#06402B"));
                entrantStatusView.setText("Accepted");
                entrantCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#a6ffa6")));
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

    /**
     * Displays a dialog to confirm the rejection of an entrant.
     * Upon confirmation, updates the entrant's status to "rejected" in the Firestore database
     * and refreshes the {@code EventEntrantsActivity} to reflect the change.
     *
     * @param entrant the entrant to be rejected.
     */
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
