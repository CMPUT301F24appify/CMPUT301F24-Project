// MyApp.java
package com.example.appify;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyApp extends Application {
    private String androidId;
    private List<ListenerRegistration> statusListeners = new ArrayList<>();
    private FirebaseFirestore db;
    private static final String TAG = "MyApp";
    private static final String NOTIFICATION_CHANNEL_ID = "event_channel_id";

    // Constants for statuses and message fields
    private static final String STATUS_ENROLLED = "enrolled";
    private static final String STATUS_INVITED = "invited";
    private static final String STATUS_ACCEPTED = "accepted";
    private static final String STATUS_REJECTED = "rejected";

    private static final String FIELD_NOTIFY_ENROLLED = "notifyEnrolled";
    private static final String FIELD_WAITLISTED_MESSAGE = "waitlistedMessage";

    private static final String FIELD_NOTIFY_INVITED = "notifyInvited";
    private static final String FIELD_INVITED_MESSAGE = "invitedMessage";

    private static final String FIELD_NOTIFY_ACCEPTED = "notifyAccepted";
    private static final String FIELD_ENROLLED_MESSAGE = "enrolledMessage";

    private static final String FIELD_NOTIFY_REJECTED = "notifyRejected";
    private static final String FIELD_CANCELLED_MESSAGE = "cancelledMessage";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve and set the AndroidID
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "AndroidID: " + androidId);

        // Create Notification Channel
        createNotificationChannel();

        // Start listening for user status changes (Invited Status)
        listenForStatusChanges();

        // Start listening for event-level notifications
        listenForEventNotifications();
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    /**
     * Creates a notification channel for Android O and above.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Event Notifications";
            String channelDescription = "Notifications for event status changes";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
                Log.d(TAG, "Notification channel created.");
            } else {
                Log.e(TAG, "Notification Manager is null. Cannot create notification channel.");
            }
        }
    }

    /**
     * Sets up listeners for individual user status changes (existing Invited status feature).
     */
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

                                    if (STATUS_INVITED.equals(status)) {
                                        fetchEventNameAndNotify(eventId, STATUS_INVITED);
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

    /**
     * Sets up a listener for event-level notifications based on boolean flags.
     */
    private void listenForEventNotifications() {
        db.collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Event notifications listen failed.", e);
                            return;
                        }

                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.MODIFIED || dc.getType() == DocumentChange.Type.ADDED) {
                                    handleEventNotifications(dc.getDocument());
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Handles sending notifications based on event-level boolean flags.
     *
     * @param eventDoc The event document snapshot.
     */
    private void handleEventNotifications(DocumentSnapshot eventDoc) {
        String eventId = eventDoc.getId();
        Log.d(TAG, "Handling event notifications for eventId: " + eventId);

        // Retrieve the event name
        String eventName = eventDoc.getString("name"); // Ensure 'name' field exists
        if (eventName == null || eventName.isEmpty()) {
            Log.e(TAG, "Event name is missing for eventId: " + eventId);
            return;
        }

        // Handle Enrolled Status (Waitlisted)
        Boolean notifyEnrolled = eventDoc.getBoolean(FIELD_NOTIFY_ENROLLED);
        String waitlistedMessage = eventDoc.getString(FIELD_WAITLISTED_MESSAGE);
        if (Boolean.TRUE.equals(notifyEnrolled) && waitlistedMessage != null && !waitlistedMessage.isEmpty()) {
            Log.d(TAG, "Notify Enrolled is true. Sending waitlistedMessage.");
            sendBulkNotification(eventId, STATUS_ENROLLED, waitlistedMessage, FIELD_NOTIFY_ENROLLED, FIELD_WAITLISTED_MESSAGE, eventName);
        }

        // Handle Invited Status
        Boolean notifyInvited = eventDoc.getBoolean(FIELD_NOTIFY_INVITED);
        String invitedMessage = eventDoc.getString(FIELD_INVITED_MESSAGE);
        if (Boolean.TRUE.equals(notifyInvited) && invitedMessage != null && !invitedMessage.isEmpty()) {
            Log.d(TAG, "Notify Invited is true. Sending invitedMessage.");
            sendBulkNotification(eventId, STATUS_INVITED, invitedMessage, FIELD_NOTIFY_INVITED, FIELD_INVITED_MESSAGE, eventName);
        }

        // Handle Accepted Status
        Boolean notifyAccepted = eventDoc.getBoolean(FIELD_NOTIFY_ACCEPTED);
        String enrolledMessage = eventDoc.getString(FIELD_ENROLLED_MESSAGE);
        if (Boolean.TRUE.equals(notifyAccepted) && enrolledMessage != null && !enrolledMessage.isEmpty()) {
            Log.d(TAG, "Notify Accepted is true. Sending enrolledMessage.");
            sendBulkNotification(eventId, STATUS_ACCEPTED, enrolledMessage, FIELD_NOTIFY_ACCEPTED, FIELD_ENROLLED_MESSAGE, eventName);
        }

        // Handle Rejected Status (Cancelled)
        Boolean notifyRejected = eventDoc.getBoolean(FIELD_NOTIFY_REJECTED);
        String cancelledMessage = eventDoc.getString(FIELD_CANCELLED_MESSAGE);
        if (Boolean.TRUE.equals(notifyRejected) && cancelledMessage != null && !cancelledMessage.isEmpty()) {
            Log.d(TAG, "Notify Rejected is true. Sending cancelledMessage.");
            sendBulkNotification(eventId, STATUS_REJECTED, cancelledMessage, FIELD_NOTIFY_REJECTED, FIELD_CANCELLED_MESSAGE, eventName);
        }
    }

    /**
     * Sends notifications in bulk to all users with a specific status, excluding the sender.
     *
     * @param eventId        The ID of the event.
     * @param status         The status to filter users.
     * @param message        The message to send.
     * @param notifyField    The boolean flag field name.
     * @param messageField   The message field name.
     * @param eventName      The name of the event.
     */
    private void sendBulkNotification(String eventId, String status, String message, String notifyField, String messageField, String eventName) {
        // Fetch all users with the specified status
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot userDoc : queryDocumentSnapshots) {
                            String userId = userDoc.getId();
                            // **Exclude the sender**
                            if (!userId.equals(androidId)) {
                                // Send notification to each user with event name
                                sendNotification(message, eventId, userId, eventName);
                            } else {
                                Log.d(TAG, "Excluded sender (userId: " + userId + ") from receiving notification for event: " + eventId);
                            }
                        }
                        // After sending notifications, reset the message field and notify flag
                        resetEventFields(eventId, notifyField, messageField);
                    } else {
                        Log.d(TAG, "No users with status: " + status + " for eventId: " + eventId);
                        // Still reset the fields even if no users are found
                        resetEventFields(eventId, notifyField, messageField);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch users with status: " + status + " for eventId: " + eventId, e));
    }

    /**
     * Sends a notification to a single user with a personalized title.
     *
     * @param message   The message to send.
     * @param eventId   The ID of the event.
     * @param userId    The ID of the user.
     * @param eventName The name of the event.
     */
    private void sendNotification(String message, String eventId, String userId, String eventName) {
        // Create an intent that opens the event details when the notification is tapped
        Intent intent = new Intent(this, com.example.appify.Activities.EntrantEnlistActivity.class);
        intent.putExtra("eventID", eventId); // Ensure the key matches what your EntrantEnlistActivity expects
        intent.putExtra("userID", userId);   // Pass the user ID if needed
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

        // Build the notification with the event name in the title
        String notificationTitle = eventName + " Notification"; // e.g., "City Marathon Notification"

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_bell) // Replace with your app's notification icon
                .setContentTitle(notificationTitle) // Dynamic title with event name
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Display the notification
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
            Log.d(TAG, "Notification sent to userId: " + userId + " for event: " + eventId);
        } else {
            Log.e(TAG, "Notification Manager is null");
        }
    }

    /**
     * Resets the event's message field and notify flag after sending notifications.
     *
     * @param eventId      The ID of the event.
     * @param notifyField  The boolean flag field name.
     * @param messageField The message field name.
     */
    private void resetEventFields(String eventId, String notifyField, String messageField) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        Map<String, Object> updates = new HashMap<>();
        updates.put(messageField, ""); // Reset message field
        updates.put(notifyField, false); // Reset notify flag

        eventRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Reset " + messageField + " and " + notifyField + " for eventId: " + eventId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to reset fields for eventId: " + eventId, e));
    }

    /**
     * Handles sending notifications based on individual user status changes (existing Invited feature).
     *
     * @param eventId The ID of the event.
     * @param status  The new status of the user.
     */
    private void fetchEventNameAndNotify(String eventId, String status) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventName = documentSnapshot.getString("name"); // Assuming 'name' is the field for event name
                        String message = "";

                        // Determine the message based on status
                        switch (status) {
                            case STATUS_INVITED:
                                message = documentSnapshot.getString(FIELD_INVITED_MESSAGE);
                                break;
                            // Add cases if needed for other statuses
                            default:
                                message = "You have a new update for the event: " + eventName;
                                break;
                        }

                        if (message != null && !message.isEmpty()) {
                            // **Do not send notification to the sender themselves**
                            // Since this method is triggered by the sender's own status change,
                            // we skip sending a notification to themselves.
                            Log.d(TAG, "Sender (userId: " + androidId + ") triggered a notification for event: " + eventId + " but will not receive it.");
                            // Reset the message field and notify flag without sending notification
                            resetEventFields(eventId, FIELD_NOTIFY_INVITED, FIELD_INVITED_MESSAGE);
                        }
                    } else {
                        Log.w(TAG, "Event document does not exist for eventId: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch event name.", e));
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
