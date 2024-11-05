package com.example.appify;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
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




    public Event(Context context,String  name, String date, String facility, String registrationEndDate, String description, int maxWishEntrants, int maxSampleEntrants, Uri posterUri, boolean isGeolocate, boolean notifyWaitlisted, boolean notifyEnrolled, boolean notifyCancelled, boolean notifyInvited) {
        this.name = name;
        this.date = date;
        this.facility = facility;
        this.registrationEndDate = registrationEndDate;
        this.description = description;
        this.maxWishEntrants = maxWishEntrants;
        this.maxSampleEntrants = maxSampleEntrants;
        this.posterUri = posterUri != null ? posterUri.toString() : null; // Convert URI to String
        this.isGeolocate = isGeolocate;
        this.eventId = UUID.randomUUID().toString();
        this.notifyWaitlisted = notifyWaitlisted;
        this.notifyEnrolled = notifyEnrolled;
        this.notifyCancelled = notifyCancelled;
        this.notifyInvited = notifyInvited;
        this.context = context;
        this.organizerID = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        System.out.println("Device ID: " + organizerID);

    }

    public Event() {

    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getFacility() {
        return facility;
    }

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


    // Override the toString() method to display the event name in the ListView
    @Override
    public String toString() {
        return name;
    }


    public interface EventAddCallback {
        void onEventAdded(Event event);
    }

    // Modify your addToFirestore method to include image upload
    public void addToFirestore(EventAddCallback callback) {
        if (posterUri != null) {
            // Reference to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            // Create a unique path for each image in Firebase Storage
            StorageReference posterRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

            // Upload the image file
            UploadTask uploadTask = posterRef.putFile(Uri.parse(posterUri));

            // Add listeners to handle success or failure
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Retrieve the download URL
                posterRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    // Set the download URL as the posterUri
                    this.posterUri = downloadUri.toString();

                    // Save the Event data with the new posterUri to Firestore
                    db.collection("events")
                            .document(this.eventId)
                            .set(this)
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) {
                                    callback.onEventAdded(this);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error adding event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to upload poster image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // If no image is selected, save event directly to Firestore
            db.collection("events")
                    .document(this.eventId)
                    .set(this)
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
}
