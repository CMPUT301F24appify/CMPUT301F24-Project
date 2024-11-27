// MyApp.java
package com.example.appify;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable; // Correct import
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException; // Add this import
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MyApp extends Application {
    private String androidId;
    private List<ListenerRegistration> statusListeners = new ArrayList<>();
    private FirebaseFirestore db;
    private static final String TAG = "MyApp";
    private static final String NOTIFICATION_CHANNEL_ID = "event_channel_id";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve and set the AndroidID
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Start listening for status changes
        listenForStatusChanges();
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    private void listenForStatusChanges() {
        if (androidId == null || androidId.isEmpty()) {
            Log.e(TAG, "AndroidID is not set. Cannot listen for status changes.");
            return;
        }

        Log.d(TAG, "Listening for status changes for AndroidID: " + androidId);

        // Get all event IDs
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> eventIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String eventId = doc.getId();
                        eventIds.add(eventId);
                    }

                    if (eventIds.isEmpty()) {
                        Log.d(TAG, "No events found.");
                        return;
                    }

                    // Set up listeners for each event's waitingList document for this androidId
                    for (String eventId : eventIds) {
                        DocumentReference docRef = db.collection("events")
                                .document(eventId)
                                .collection("waitingList")
                                .document(androidId);

                        ListenerRegistration listenerRegistration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen failed for eventId: " + eventId, e);
                                    return;
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    String status = snapshot.getString("status");
                                    Log.d(TAG, "Status for eventId " + eventId + ": " + status);

                                    if ("invited".equals(status)) {
                                        fetchEventNameAndNotify(eventId);
                                    }
                                } else {
                                    Log.d(TAG, "No waitingList document for eventId: " + eventId);
                                }
                            }
                        });

                        // Add the listener to the list for later removal
                        statusListeners.add(listenerRegistration);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get events.", e));
    }

    private void fetchEventNameAndNotify(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventName = documentSnapshot.getString("name");
                        sendNotification(eventName, eventId);
                    } else {
                        Log.w(TAG, "Event document does not exist for eventId: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch event name.", e));
    }

    private void sendNotification(String eventName, String eventId) {
        // Create an intent that opens the event details when the notification is tapped
        Intent intent = new Intent(this, com.example.appify.Activities.EntrantEnlistActivity.class);
        intent.putExtra("eventID", eventId); // Ensure the key matches what your EntrantEnlistActivity expects
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // PendingIntent setup
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }

        // NotificationManager setup
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Handle notification permission for Android 13 (API level 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Since this is the Application class, we cannot request permissions here.
                // You should handle requesting this permission in your MainActivity or a suitable Activity.
                Log.e(TAG, "Notification permission not granted. Cannot send notification.");
                return;
            }
        }

        // Notification channel setup for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Event Notifications";
            String channelDescription = "Notifications for event status changes";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(android.graphics.Color.RED);
            notificationChannel.enableVibration(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_bell) // Replace with your app's notification icon
                .setContentTitle("Selected for Event")
                .setContentText("You have been selected for " + eventName + ". Please sign up.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Display the notification
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            Log.d(TAG, "Notification sent for event: " + eventName);
        } else {
            Log.e(TAG, "Notification Manager is null");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Remove all listeners when the app is terminated
        for (ListenerRegistration listener : statusListeners) {
            listener.remove();
        }
    }
}
