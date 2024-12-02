
package com.example.appify.Adapters;

import android.app.AlertDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

/**
 * CustomEventAdapter is a custom ArrayAdapter for displaying and interacting with events in a ListView.
 * It binds event data to views, dynamically updates UI elements, and provides specific actions
 * based on the context (Organizer, Admin, or Participant views).
 */
public class CustomEventAdapter extends ArrayAdapter<Event> {

    private Context context; // Context in which the adapter is used
    private List<Event> eventList; // List of events to display
    private boolean isOrganizePage; // Indicates if this adapter is used on the organizer's page
    private boolean isAdminPage; // Indicates if this adapter is used on the admin page
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance

    /**
     * Constructor for CustomEventAdapter.
     *
     * @param context        The current context.
     * @param eventList      The list of events to display.
     * @param isOrganizePage Boolean indicating if the adapter is for the organizer's page.
     * @param isAdminPage    Boolean indicating if the adapter is for the admin page.
     */
    public CustomEventAdapter(Context context, List<Event> eventList, boolean isOrganizePage, boolean isAdminPage){
        super(context, 0, eventList);
        this.context = context;
        this.eventList = eventList;
        this.isOrganizePage = isOrganizePage;
        this.isAdminPage = isAdminPage;
    }


    /**
     * Provides a view for an event in the ListView, binds event data to views,
     * and dynamically updates UI elements based on the event's status and context.
     *
     * @param position     Position of the item within the adapter's data set.
     * @param convertView  The old view to reuse, if possible.
     * @param parent       The parent view that this view will eventually be attached to.
     * @return The View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.event_list_content, parent, false);
        }

        // Retrieve event and user details
        MyApp app = (MyApp) context.getApplicationContext();
        String entrantID = app.getAndroidId();
        Event event = eventList.get(position);

        // Bind views
        TextView eventTitle = convertView.findViewById(R.id.event_title);
        TextView eventRegistrationEndDate = convertView.findViewById(R.id.registration_date);
        TextView eventStartDate = convertView.findViewById(R.id.event_date);
        ImageView statusIcon = convertView.findViewById(R.id.statusIcon);
        ImageView x_Icon = convertView.findViewById(R.id.x_icon);
        ConstraintLayout eventCard = convertView.findViewById(R.id.event_information);

        if(!isAdminPage) {
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
        }

        // Set event details
        eventTitle.setText(event.getName());
        eventRegistrationEndDate.setText(event.getRegistrationEndDate());
        eventStartDate.setText(event.getDate());

        // Hide event for organizers viewing as participants
        if (Objects.equals(event.getOrganizerID(), entrantID) && !isOrganizePage && !isAdminPage){
            LinearLayout topPart = convertView.findViewById(R.id.top_part);
            LinearLayout bottomPart = convertView.findViewById(R.id.profile_information);
            bottomPart.setVisibility(View.GONE);
            topPart.setVisibility(View.GONE);
        }

        notifyDataSetChanged();

        // Check if it is the Admin Page:
        if (isAdminPage) {
            x_Icon.setVisibility(View.VISIBLE);
            x_Icon.setOnClickListener(v -> {
                showCancelEventDialog(event);
            });
            convertView.setOnClickListener(view -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("name", event.getName() );
                intent.putExtra("date", event.getDate());

                intent.putExtra("registrationEndDate", event.getRegistrationEndDate());
                intent.putExtra("description", event.getDescription() );
                intent.putExtra("maxWaitEntrants", event.getMaxWaitEntrants());
                intent.putExtra("maxSampleEntrants", event.getMaxSampleEntrants());
                intent.putExtra("eventID", event.getEventId());
                intent.putExtra("posterUri", event.getPosterUri());
                intent.putExtra("isGeolocate", event.isGeolocate());
                intent.putExtra("isAdminPage", true);
                String facilityID = event.getFacility();
                db.collection("facilities").document(facilityID).get().addOnSuccessListener(documentSnapshot -> {
                    String facilityName = documentSnapshot.getString("name");
                    intent.putExtra("facility", facilityName);
                    context.startActivity(intent);
                });

            });
        } else {
            x_Icon.setVisibility(View.GONE);
        }

        return convertView;
    }

    /**
     * Displays a confirmation dialog for deleting an event.
     * Removes the event and its associated data from Firestore upon confirmation.
     *
     * @param event The event to delete.
     */
    private void showCancelEventDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder
                .setTitle("Delete Event: " + event.getName())
                .setMessage("Please confirm that you want to delete: " + event.getName() + ". ALL users will be removed from the waiting list. This action cannot be undone.")
                .setPositiveButton("Confirm", ((dialog, which) -> {
                    // Delete the Event from the Users waitListedEvents
                    db.collection("events").document(event.getEventId()).collection("waitingList")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (QueryDocumentSnapshot userDoc : querySnapshot) {
                                    String userID = userDoc.getId();

                                    // Delete the event from the Users waitListedEvents
                                    db.collection("AndroidID").document(userID)
                                            .collection("waitListedEvents")
                                            .document(event.getEventId())
                                            .delete()
                                            .addOnSuccessListener(v -> {
                                                // Delete the User from the Event's waitingList
                                                db.collection("events").document(event.getEventId())
                                                        .collection("waitingList")
                                                        .document(userID)
                                                        .delete();
                                            });

                                }
                            })
                            .addOnCompleteListener(eventTask -> {
                                // Delete the Event along with its poster
                                db.collection("events").document(event.getEventId())
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String qrCodeLocationUrl = documentSnapshot.getString("qrCodeLocationUrl");
                                                if (qrCodeLocationUrl != null && !qrCodeLocationUrl.isEmpty()) {
                                                    // Delete the qrCode image from Firebase Storage
                                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                                    StorageReference imageRef = storage.getReferenceFromUrl(qrCodeLocationUrl);
                                                    imageRef.delete();
                                                }

                                                String posterUri = documentSnapshot.getString("posterUri");

                                                if (posterUri != null && !posterUri.isEmpty()) {
                                                    // Delete the poster image from Firebase Storage
                                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                                    StorageReference imageRef = storage.getReferenceFromUrl(posterUri);
                                                    imageRef.delete();
                                                }
                                            }

                                            // Delete the Event all together
                                            db.collection("events").document(event.getEventId())
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        eventList.remove(event);
                                                        notifyDataSetChanged();
                                                    });
                                        });
                            });
                }))
                .setNegativeButton("Cancel", ((dialog, which) -> {
                    dialog.dismiss();
                }));
        builder.create();
        builder.show();
    }
}
