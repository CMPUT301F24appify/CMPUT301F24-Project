/**
 * CustomEventAdapter.java
 *
 * This adapter is used to populate the event list view with event data.
 * It changes the display icon of each event based on its status and visibility requirements.
 * It uses Firebase to retrieve the user's status in relation to each event (e.g., enrolled, invited).
 *
 * Outstanding Issues:
 * 1. Performance: Repeated Firebase calls in `getView()` may lead to performance issues in large lists.
 *    Consider caching status data or using a listener to improve efficiency.
 * 2. Null Safety: Ensure null handling for event details, especially in Firebase responses, to avoid crashes.
 */

package com.example.appify.Adapters;

import android.content.Context;
import android.content.Intent;
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
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.appify.Activities.EventDetailActivity;
import com.example.appify.Model.Event;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * CustomEventAdapter extends ArrayAdapter to handle the display of events in a ListView.
 * It binds event data to a custom layout and retrieves status information for each event.
 */
public class CustomEventAdapter extends ArrayAdapter<Event> {
    private Context context;
    private List<Event> eventList;
    private boolean isOrganizePage;

    /**
     * Constructor for CustomEventAdapter.
     *
     * @param context       The current context.
     * @param eventList     The list of events to be displayed.
     * @param isOrganizePage Boolean indicating if the adapter is used on the organizer's page.
     */
    public CustomEventAdapter(Context context, List<Event> eventList, boolean isOrganizePage){
        super(context, 0, eventList);
        this.context = context;
        this.eventList = eventList;
        this.isOrganizePage = isOrganizePage;
    }


    /**
     * Provides a view for an AdapterView (ListView) for each event.
     * Sets event details in the view and fetches status data from Firebase for display.
     *
     * @param position     Position of the item within the adapter's data set.
     * @param convertView  The old view to reuse, if possible.
     * @param parent       The parent that this view will eventually be attached to.
     * @return The View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.event_list_content, parent, false);
        }

        MyApp app = (MyApp) context.getApplicationContext();
        String entrantID = app.getAndroidId();
        Event event = eventList.get(position);

        TextView eventTitle = convertView.findViewById(R.id.event_title);
//        TextView eventDesc = convertView.findViewById(R.id.event_desc);
        TextView eventRegistrationEndDate = convertView.findViewById(R.id.registration_date);
        TextView eventStartDate = convertView.findViewById(R.id.event_date);
        ImageView statusIcon = convertView.findViewById(R.id.statusIcon);
        ConstraintLayout eventCard = convertView.findViewById(R.id.event_information);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("AndroidID").document(entrantID).collection("waitListedEvents").document(event.getEventId()).get().addOnSuccessListener(DocumentSnapshot -> {
            String status = DocumentSnapshot.getString("status");

            statusIcon.setVisibility(View.VISIBLE);

            // Change the icon based off the status
            if (Objects.equals(status, "enrolled")) {
                statusIcon.setImageResource(R.drawable.waiting_list_icon);
                eventCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFDBBB")));
            } else if (Objects.equals(status, "invited")) {
                statusIcon.setImageResource(R.drawable.invited_icon);
                eventCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#add8e6")));
            } else if (Objects.equals(status, "accepted")) {
                statusIcon.setImageResource(R.drawable.accepted_icon);
                eventCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#a6ffa6")));
            } else if (Objects.equals(status, "rejected")) {
                eventCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffc0c0")));
                statusIcon.setImageResource(R.drawable.rejected_icon);
            } else {
                statusIcon.setVisibility(View.INVISIBLE);
            }
        });


        eventTitle.setText(event.getName());
//        eventDesc.setText(event.getDescription());
        eventRegistrationEndDate.setText(event.getRegistrationEndDate());
        eventStartDate.setText(event.getDate());

        if (Objects.equals(event.getOrganizerID(), entrantID) && !isOrganizePage){
            LinearLayout topPart = convertView.findViewById(R.id.top_part);
            LinearLayout bottomPart = convertView.findViewById(R.id.event_dates);
            bottomPart.setVisibility(View.GONE);
            topPart.setVisibility(View.GONE);
        }
        convertView.setOnClickListener(view -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("name", event.getName() );
            intent.putExtra("date", event.getDate());
            intent.putExtra("facility", event.getFacility());
            intent.putExtra("registrationEndDate", event.getRegistrationEndDate());
            intent.putExtra("description", event.getDescription() );
            intent.putExtra("maxWaitEntrants", event.getMaxWaitEntrants());
            intent.putExtra("maxSampleEntrants", event.getMaxSampleEntrants());
            intent.putExtra("eventID", event.getEventId());
            intent.putExtra("posterUri", event.getPosterUri());
            intent.putExtra("isGeolocate", event.isGeolocate());
            context.startActivity(intent);
        });

        return convertView;
    }
}
