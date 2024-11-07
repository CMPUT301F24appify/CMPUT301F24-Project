package com.example.appify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.appify.Model.Event;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CustomEventAdapter extends ArrayAdapter<Event> {
    private Context context;
    private List<Event> eventList;

    public CustomEventAdapter(Context context, List<Event> eventList){
        super(context, 0, eventList);
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.event_list_content, parent, false);
        }

        Event event = eventList.get(position);

        TextView eventTitle = convertView.findViewById(R.id.event_title);
        TextView eventDesc = convertView.findViewById(R.id.event_desc);
        TextView eventRegistrationEndDate = convertView.findViewById(R.id.registration_date);
        TextView eventStartDate = convertView.findViewById(R.id.event_date);
        ImageView statusIcon = convertView.findViewById(R.id.statusIcon);

        MyApp app = (MyApp) context.getApplicationContext();
        String entrantID = app.getAndroidId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Android ID").document(entrantID).collection("waitListedEvents").document(event.getEventId()).get().addOnSuccessListener(DocumentSnapshot ->{
            String status = DocumentSnapshot.getString("status");

            statusIcon.setVisibility(View.VISIBLE);
            // Change the icon based off the status
            if (Objects.equals(status, "enrolled")){
                statusIcon.setImageResource(R.drawable.waiting_list_icon);
            } else if (Objects.equals(status, "invited")) {
                statusIcon.setImageResource(R.drawable.invited_icon);
            } else if (Objects.equals(status, "accepted")) {
                statusIcon.setImageResource(R.drawable.accepted_icon);
            } else if (Objects.equals(status, "rejected")) {
                statusIcon.setImageResource(R.drawable.rejected_icon);
            } else {
                statusIcon.setVisibility(View.INVISIBLE);
            }
        });


        eventTitle.setText(event.getName());
        eventDesc.setText(event.getDescription());
        eventRegistrationEndDate.setText(event.getRegistrationEndDate());
        eventStartDate.setText(event.getDate());
        return convertView;
    }
}
