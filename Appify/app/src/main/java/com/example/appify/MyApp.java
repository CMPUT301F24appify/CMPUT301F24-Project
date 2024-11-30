// MyApp.java
package com.example.appify;


import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;


import com.example.appify.Activities.EntrantEnlistActivity;
import com.example.appify.Model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MyApp extends Application {
    private static final String TAG = "MyApp";
    private static final String NOTIFICATION_CHANNEL_ID = "event_channel_id";
    private static final String NOTIFICATION_CHANNEL_NAME = "Event Notifications";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications for event status changes";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String PREF_ANDROID_ID = "androidId";

    // Firebase Firestore instance
    private FirebaseFirestore db;
    private ListenerRegistration eventListener;
    private ListenerRegistration waitingListListener; // **New ListenerRegistration for waitingList**

    // User Identifier
    private String androidId;

    // Define the delay duration for flag and message reset in milliseconds (e.g., 3000 ms = 3 seconds)
    private static final long FLAG_RESET_DELAY_MILLISECONDS = 3000;

    // Handler for scheduling flag and message resets
    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate() {
        super.onCreate();


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();


        // Retrieve Android ID
        androidId = getAndroidId();
        Log.d(TAG, "Android ID: " + androidId);


        if (androidId == null) {
            Log.e(TAG, "Failed to retrieve Android ID.");
            // Handle the error as needed, possibly exit the app or retry
            return;
        }


        // Create Notification Channel
        createNotificationChannel();


        // Start listening for event-level notifications
        listenForEventNotifications();

        // **Start listening for waitingList subcollection changes**
        listenForWaitingListAdditions();

        listenForInvitations();
    }


    /**
     * Retrieves the device's Android ID.
     *
     * @return The Android ID as a String.
     */
    public String getAndroidId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedAndroidId = prefs.getString(PREF_ANDROID_ID, null);


        if (storedAndroidId != null) {
            return storedAndroidId;
        } else {
            String newAndroidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            setAndroidId(newAndroidId);
            return newAndroidId;
        }
    }


    /**
     * Sets the device's Android ID in SharedPreferences.
     *
     * @param id The Android ID to store.
     */
    public void setAndroidId(String id) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_ANDROID_ID, id);
        editor.apply();
        Log.d(TAG, "Android ID set to: " + id);
    }


    /**
     * Creates a notification channel for Android Oreo and above.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = NOTIFICATION_CHANNEL_NAME;
            String description = NOTIFICATION_CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_HIGH;


            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);


            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created.");
            } else {
                Log.e(TAG, "Notification Manager is null. Cannot create notification channel.");
            }
        }
    }


    /**
     * Sets up a listener for the 'events' collection to detect changes.
     */
    private void listenForEventNotifications() {
        CollectionReference eventsRef = db.collection("events");
        eventListener = eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot snapshots,
                                @androidx.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed for events.", e);
                    return;
                }


                if (snapshots != null) {
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.MODIFIED || dc.getType() == DocumentChange.Type.ADDED) {
                            Log.d(TAG, "Detected change in event: " + dc.getDocument().getId());
                            handleEventChange(dc.getDocument());
                        }
                    }
                }
            }
        });


        Log.d(TAG, "Started listening for event notifications.");
    }


    /**
     * Sets up a collection group listener for all 'waitingList' subcollections to detect new users.
     */
    private void listenForWaitingListAdditions() {
        waitingListListener = db.collectionGroup("waitingList")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable QuerySnapshot snapshots,
                                        @androidx.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed for waitingList collection group.", e);
                            return;
                        }

                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    Log.d(TAG, "New user added to waitingList: " + dc.getDocument().getId());
                                    handleNewWaitingListUser(dc.getDocument());
                                }
                            }
                        }
                    }
                });

        Log.d(TAG, "Started listening for new waitingList additions.");
    }

    private void listenForInvitations() {
        waitingListListener = db.collection("events")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed for events collection.", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            DocumentSnapshot eventDoc = dc.getDocument();
                            String eventId = eventDoc.getId();

                            // Check if the document's lotteryRan counter was updated
                            if (dc.getType() == DocumentChange.Type.MODIFIED && eventDoc.contains("lotteryRan")) {
                                Log.d(TAG, "Lottery counter updated for event: " + eventId);

                                // Fetch the organizerID from the event document
                                String organizerId = eventDoc.getString("organizerID");

                                // Fetch the waiting list for this event
                                db.collection("events").document(eventId)
                                        .collection("waitingList")
                                        .get()
                                        .addOnSuccessListener(waitingListSnapshot -> {
                                            for (DocumentSnapshot userDoc : waitingListSnapshot) {
                                                String userId = userDoc.getId();
                                                String status = userDoc.getString("status");

                                                // Ignore the organizer
                                                if (userId.equals(organizerId)) {
                                                    Log.d(TAG, "Skipping notification for organizer: " + organizerId);
                                                    continue;
                                                }

                                                if ("invited".equals(status)) {
                                                    // Send 'invited' notification
                                                    String message = "You have been invited to the event!";
                                                    Log.d(TAG, "Sending 'invited' notification to user: " + userId);
                                                    sendNotification(eventId, "invited", message, "Event ID: " + eventId);
                                                } else if ("enrolled".equals(status)) {
                                                    // Send 'not invited' notification
                                                    String message = "You were not invited to the event.";
                                                    Log.d(TAG, "Sending 'not invited' notification to user: " + userId);
                                                    sendNotification(eventId, "not_invited", message, "Event ID: " + eventId);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(err -> {
                                            Log.e(TAG, "Error fetching waiting list for event: " + eventId, err);
                                        });
                            }
                        }
                    }
                });

        Log.d(TAG, "Started listening for updates to the lottery counter.");
    }







    /**
     * Handles changes in an individual event document.
     *
     * @param eventDoc The changed event document.
     */
    private void handleEventChange(DocumentSnapshot eventDoc) {
        String eventId = eventDoc.getId();
        Log.d(TAG, "Handling event change for eventId: " + eventId);


        // Fetch the Event model from the document
        Event event = eventDoc.toObject(Event.class);
        if (event == null) {
            Log.e(TAG, "Failed to convert document to Event model for eventId: " + eventId);
            return;
        }


        // List of statuses to handle
        String[] statuses = {"invited", "accepted", "enrolled", "rejected"};


        for (String status : statuses) {
            boolean notifyFlag = false;
            String message = "";


            switch (status) {
                case "invited":
                    notifyFlag = event.isNotifyInvited();
                    message = event.getInvitedMessage();
                    break;
                case "accepted":
                    notifyFlag = event.isNotifyEnrolled();
                    message = event.getEnrolledMessage();
                    break;
                case "enrolled":
                    notifyFlag = event.isNotifyWaitlisted();
                    message = event.getWaitlistedMessage();
                    break;
                case "rejected":
                    notifyFlag = event.isNotifyCancelled();
                    message = event.getCancelledMessage();
                    break;
                default:
                    Log.w(TAG, "Unknown status: " + status);
            }


            Log.d(TAG, "Status: " + status + ", notifyFlag: " + notifyFlag + ", message: " + message);


            if (notifyFlag && message != null && !message.isEmpty()) {
                Log.d(TAG, "Preparing to check and send notification for status: " + status);
                checkAndSendNotification(eventId, status, message, event.getName());
            } else {
                Log.d(TAG, "No notification needed for status: " + status);
            }
        }
    }


    /**
     * Checks if the current user is affected by the status change and sends a notification if true.
     *
     * @param eventId   The ID of the event.
     * @param status    The status that has changed.
     * @param message   The message to send in the notification.
     * @param eventName The name of the event.
     */
    private void checkAndSendNotification(String eventId, String status, String message, String eventName) {
        DocumentReference userRef = db.collection("events").document(eventId)
                .collection("waitingList").document(androidId);


        Log.d(TAG, "Checking user status for eventId: " + eventId + ", status: " + status);


        userRef.get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String userStatus = userDoc.getString("status");
                Boolean notified = userDoc.getBoolean(status + "Notified");
                if (notified == null) notified = false;


                Log.d(TAG, "UserStatus: " + userStatus + ", Notified: " + notified);


                if (userStatus != null && userStatus.equals(status) && !notified) {
                    Log.d(TAG, "User is affected by status: " + status + ". Sending notification.");
                    sendNotification(eventId, status, message, eventName);
                } else {
                    Log.d(TAG, "User is not affected by status: " + status + " or already notified.");
                }
            } else {
                Log.d(TAG, "User document does not exist in waitingList for eventId: " + eventId);
            }
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error fetching user document for eventId: " + eventId, exception);
        });
    }


    /**
     * Handles a newly added user document in the 'waitingList' subcollection.
     *
     * @param userDoc The newly added user document.
     */
    private void handleNewWaitingListUser(DocumentSnapshot userDoc) {
        String userId = userDoc.getId();
        Log.d(TAG, "Handling new waitingList user: " + userId);


        String status = userDoc.getString("status");
        Boolean notified = userDoc.getBoolean(status + "Notified");
        if (notified == null) notified = false;


        Log.d(TAG, "UserStatus: " + status + ", Notified: " + notified);


        if (status != null && status.equals("invited") && !notified) {
            // Retrieve the eventId from the document's path: events/{eventId}/waitingList/{userId}
            String eventId = userDoc.getReference().getParent().getParent().getId();
            Log.d(TAG, "Associated eventId for user " + userId + ": " + eventId);

            // Fetch the Event document to get the invitedMessage and eventName
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(eventDoc -> {
                        if (eventDoc.exists()) {
                            String message = eventDoc.getString("invitedMessage");
                            String eventName = eventDoc.getString("name"); // Ensure 'name' field exists
                            if (message != null && !message.isEmpty()) {
                                Log.d(TAG, "Sending invited notification to user: " + userId);
                                sendNotification(eventId, "invited", message, eventName);
                            } else {
                                Log.d(TAG, "Invited message is empty for eventId: " + eventId + ". No notification sent.");
                            }
                        } else {
                            Log.d(TAG, "Event document does not exist for eventId: " + eventId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching event document for eventId: " + eventId, e);
                    });
        } else {
            Log.d(TAG, "No invited notification needed for user: " + userId);
        }
    }


    /**
     * Sends a local notification to the device and schedules a flag and message reset after a delay.
     *
     * @param eventId   The ID of the event.
     * @param status    The status of the user.
     * @param message   The message to display in the notification.
     * @param eventName The name of the event.
     */
    private void sendNotification(String eventId, String status, String message, String eventName) {
        Log.d(TAG, "Attempting to send notification for eventId: " + eventId + ", status: " + status);


        // Create an intent to open EntrantEnlistActivity when the notification is tapped
        Intent intent = new Intent(this, EntrantEnlistActivity.class);
        intent.putExtra("eventID", eventId);
        intent.putExtra("status", status);
        intent.putExtra("eventName", eventName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }


        // Build the notification
        String notificationTitle = eventName + " - " + capitalize(status) + " Notification";


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_bell) // Ensure this icon exists in res/drawable
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        // Get NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (notificationManager != null) {
            // Use a unique notification ID
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());
            Log.d(TAG, "Notification sent for eventId: " + eventId + " with status: " + status);


            // After sending the notification, set the statusNotified flag to true
            setNotifiedFlag(eventId, status, true);


            // Schedule the flag and message reset after the defined delay
            scheduleFlagAndMessageReset(eventId, status, androidId);
        } else {
            Log.e(TAG, "Notification Manager is null. Cannot send notification for eventId: " + eventId);
        }
    }


    /**
     * Sets the statusNotified flag to the specified value.
     *
     * @param eventId The ID of the event.
     * @param status  The status related to the notification.
     * @param value   The value to set for statusNotified (true or false).
     */
    private void setNotifiedFlag(String eventId, String status, boolean value) {
        Log.d(TAG, "Setting " + status + "Notified to " + value + " for eventId: " + eventId);


        DocumentReference userRef = db.collection("events").document(eventId)
                .collection("waitingList").document(androidId);
        Map<String, Object> updates = new HashMap<>();
        updates.put(status + "Notified", value);


        userRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully set " + status + "Notified to " + value + " for eventId: " + eventId))
                .addOnFailureListener(error -> Log.e(TAG, "Failed to set " + status + "Notified to " + value + " for eventId: " + eventId, error));
    }


    /**
     * Schedules the flag and message reset after a specified delay using Handler.
     *
     * @param eventId   The ID of the event.
     * @param status    The status related to the notification.
     * @param androidId The user's androidId.
     */
    private void scheduleFlagAndMessageReset(String eventId, String status, String androidId) {
        Log.d(TAG, "Scheduling flag and message reset for eventId: " + eventId + ", status: " + status + ", androidId: " + androidId);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetFlagAndMessage(eventId, status, androidId);
            }
        }, FLAG_RESET_DELAY_MILLISECONDS); // Delay in milliseconds (e.g., 3000 ms = 3 seconds)
    }


    /**
     * Resets the statusNotified flag to false and the corresponding message field to an empty string in Firestore.
     * Additionally, resets the related notify* boolean and message field in the events collection.
     *
     * @param eventId   The ID of the event.
     * @param status    The status related to the notification.
     * @param androidId The user's androidId.
     */
    private void resetFlagAndMessage(String eventId, String status, String androidId) {
        Log.d(TAG, "Resetting " + status + "Notified to false and " + status + "Message to empty string for eventId: " + eventId + ", androidId: " + androidId);


        // Reset in waitingList subcollection
        DocumentReference userRef = db.collection("events").document(eventId)
                .collection("waitingList").document(androidId);
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put(status + "Notified", false);
        // Assuming 'waitingList' subcollection does NOT have message fields. If it does, uncomment the next line.
        // userUpdates.put(status + "Message", ""); // Reset the message field to an empty string (if exists)


        userRef.update(userUpdates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully reset " + status + "Notified to false for eventId: " + eventId + ", androidId: " + androidId))
                .addOnFailureListener(error -> Log.e(TAG, "Failed to reset " + status + "Notified for eventId: " + eventId + ", androidId: " + androidId, error));


        // Reset in events collection
        DocumentReference eventRef = db.collection("events").document(eventId);
        Map<String, Object> eventUpdates = new HashMap<>();


        switch (status) {
            case "invited":
                eventUpdates.put("notifyInvited", false);
                eventUpdates.put("invitedMessage", "");
                break;
            case "accepted":
                eventUpdates.put("notifyEnrolled", false);
                eventUpdates.put("enrolledMessage", "");
                break;
            case "enrolled":
                eventUpdates.put("notifyWaitlisted", false);
                eventUpdates.put("waitlistedMessage", "");
                break;
            case "rejected":
                eventUpdates.put("notifyCancelled", false);
                eventUpdates.put("cancelledMessage", "");
                break;
            default:
                Log.w(TAG, "Unknown status during event reset: " + status);
                return; // Exit if status is unknown
        }


        eventRef.update(eventUpdates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully reset notify*" + capitalize(status) + " to false and " + status + "Message to empty string for eventId: " + eventId))
                .addOnFailureListener(error -> Log.e(TAG, "Failed to reset notify*" + capitalize(status) + " and " + status + "Message for eventId: " + eventId, error));
    }


    /**
     * Capitalizes the first letter of a string.
     *
     * @param str The input string.
     * @return The capitalized string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        // Remove the event listener to prevent memory leaks
        if (eventListener != null) {
            eventListener.remove();
            Log.d(TAG, "Removed event listener.");
        }

        // **Remove the waitingListListener**
        if (waitingListListener != null) {
            waitingListListener.remove();
            Log.d(TAG, "Removed waitingList listener.");
        }

        Log.d(TAG, "App terminated and listeners removed.");
    }
}
