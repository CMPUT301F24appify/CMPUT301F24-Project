package com.example.appify.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.signature.ObjectKey;
import com.example.appify.Model.Entrant;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        db.collection("Android ID").document(entrantID).collection("waitListedEvents").document(eventID).get().addOnSuccessListener(DocumentSnapshot -> {
            String status = DocumentSnapshot.getString("status");
            TextView entrantStatusView = finalConvertView.findViewById(R.id.entrant_status_text);

            if (Objects.equals(status, "enrolled")){
                // Set status text to orange if status is enrolled
                entrantStatusView.setTextColor(Color.parseColor("#FFA500"));
                entrantStatusView.setText("Enrolled");
            } else if (Objects.equals(status,"invited")) {
                entrantStatusView.setTextColor(Color.parseColor("#ADD8E6"));
                entrantStatusView.setText("Invited");
            } else if (Objects.equals(status, "rejected")) {
                entrantStatusView.setTextColor(Color.parseColor("#FF0000"));
                entrantStatusView.setText("Rejected");
            } else if (Objects.equals(status, "accepted")) {
                entrantStatusView.setTextColor(Color.parseColor("#008000"));
                entrantStatusView.setText("Accepted");
            }
        });

        TextView entrantNameView = convertView.findViewById(R.id.entrant_name_text);

        entrantNameView.setText(entrant.getName());
        return convertView;
    }

}