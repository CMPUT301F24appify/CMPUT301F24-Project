package com.example.appify.Model;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class Event {
    private String name;
    private String date;
    private String facility;
    private String registrationEndDate;
    private String description;
    private int maxWishEntrants;
    private int maxSampleEntrants;
    private String posterUri;  // Store URI as String
    private boolean isGeolocate;
    private String eventId;
    private boolean notifyWaitlisted;
    private boolean notifyEnrolled;
    private boolean notifyCancelled;
    private boolean notifyInvited;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private String organizerID;


    public Event(String name, String date, String facility, String registrationEndDate, String description, int maxWishEntrants, int maxSampleEntrants, String posterUri, boolean isGeolocate, boolean notifyWaitlisted, boolean notifyEnrolled, boolean notifyCancelled, boolean notifyInvited, String organizerID) {
        this.name = name;
        this.date = date;
        this.facility = facility;
        this.registrationEndDate = registrationEndDate;
        this.description = description;
        this.maxWishEntrants = maxWishEntrants;
        this.maxSampleEntrants = maxSampleEntrants;
        this.posterUri = posterUri; // Convert URI to String
        this.isGeolocate = isGeolocate;
        this.eventId = UUID.randomUUID().toString();
        this.notifyWaitlisted = notifyWaitlisted;
        this.notifyEnrolled = notifyEnrolled;
        this.notifyCancelled = notifyCancelled;
        this.notifyInvited = notifyInvited;
        this.organizerID= organizerID;
//        this.context = context;
//        this.organizerID = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    // No-argument constructor (required for Firebase)
    public Event() {
    }

    public static Event fromFirestore(QueryDocumentSnapshot document) {
        String name = document.getString("name");
        String date = document.getString("date");
        String registrationEndDate = document.getString("registrationEndDate");
        String description = document.getString("description");
        String facility = document.getString("facility");
        int maxWishEntrants = document.getLong("maxWishEntrants").intValue();
        int maxSampleEntrants = document.getLong("maxSampleEntrants").intValue();
        String posterUri = document.getString("posterUri");
        boolean isGeolocate = document.getBoolean("isGeolocate") != null ? document.getBoolean("isGeolocate") : false;
        boolean notifyWaitlisted = document.getBoolean("notifyWaitlisted") != null ? document.getBoolean("notifyWaitlisted") : false;
        boolean notifyEnrolled = document.getBoolean("notifyEnrolled") != null ? document.getBoolean("notifyEnrolled") : false;
        boolean notifyCancelled = document.getBoolean("notifyCancelled") != null ? document.getBoolean("notifyCancelled") : false;
        boolean notifyInvited = document.getBoolean("notifyInvited") != null ? document.getBoolean("notifyInvited") : false;
        String organizerID = document.getString("organizerID");


        Event event = new Event(name, date, registrationEndDate, description, facility, maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate, notifyWaitlisted, notifyEnrolled, notifyCancelled,notifyInvited, organizerID);
        event.eventId = document.getId(); // Use Firestore ID if available

        return event;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getFacility() { return facility; }

    public String getRegistrationEndDate() {
        return registrationEndDate;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxWishEntrants() {
        return maxWishEntrants;
    }

    public int getMaxSampleEntrants() {
        return maxSampleEntrants;
    }

    public String getPosterUri() {
        return posterUri;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public String getEventId() {
        return eventId;
    }

    public boolean isGeolocate() {
        return isGeolocate;
    }

    public boolean isNotifyWaitlisted() {
        return notifyWaitlisted;
    }

    public boolean isNotifyEnrolled() {
        return notifyEnrolled;
    }

    public boolean isNotifyCancelled() {
        return notifyCancelled;
    }

    public boolean isNotifyInvited() {
        return notifyInvited;
    }

    public void setGeolocate(boolean geolocate) {
        this.isGeolocate = geolocate;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }



    public interface EventAddCallback {
        void onEventAdded(Event event);
    }

    // Modify your addToFirestore method to include image upload
    public void addToFirestore(EventAddCallback callback) {
        db.collection("events").document(this.eventId).set(this)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onEventAdded(this);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error adding event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
