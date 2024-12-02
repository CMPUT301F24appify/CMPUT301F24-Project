package com.example.appify.Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appify.Fragments.EditEventDialogFragment;
import com.example.appify.HeaderNavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appify.Model.Event;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The EventDetailActivity class provides a detailed view of a selected event.
 * It allows users to view event details, manage QR codes, send notifications, and perform actions
 * like editing the event, viewing entrants, or running a lottery.
 */
public class EventDetailActivity extends AppCompatActivity implements EditEventDialogFragment.EditEventDialogListener {

    private FirebaseFirestore db; // Firestore instance for database interactions
    private String eventID; // ID of the selected event
    private Button showMap; // Button to display the map of entrants
    private String waitlistedMessage = ""; // Notification message for waitlisted participants
    private String enrolledMessage = ""; // Notification message for enrolled participants
    private String cancelledMessage = ""; // Notification message for cancelled participants
    private String invitedMessage = ""; // Notification message for invited participants
    private String qrCodeLocationURL; // URL of the QR code image stored in Firebase Storage
    private StorageReference storageRef; // Reference to the QR code storage
    private ImageView qrImageView; // ImageView to display the event's QR code


    /**
     * Called when the activity is created. Initializes the user interface, sets up event details,
     * and configures buttons for notifications, editing, QR code management, and entrant management.
     *
     * @param savedInstanceState If the activity is re-initialized after being shut down,
     *                           this Bundle contains the most recent data; otherwise, it is null.
     */
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize header navigation with highlighting for the "Organize" section
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();

        // Initialize Firebase Firestore instance
        MyApp app = (MyApp) getApplication();
        db = app.getFirebaseInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Retrieve event ID from intent extras
        eventID = getIntent().getStringExtra("eventID");
        if (eventID == null || eventID.isEmpty()) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity gracefully
            return;
        }

        // Initialize storage reference for QR codes
        storageRef = storage.getReference().child("qrcode_images/" + eventID + ".jpg");

        // QR code generation for event-specific content
        qrImageView = findViewById(R.id.qr_code);


        // If QR code exists in database use it. Otherwise, generate new one and store in db
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            String qrCodeLocationURL = documentSnapshot.getString("qrCodeLocationUrl");

            if (qrCodeLocationURL != null){
                // Qr code exists
                storageRef.getBytes(1024 * 1024)
                        .addOnSuccessListener(bytes -> {
                            // Convert the byte array to a Bitmap
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            qrImageView.setImageBitmap(bitmap);
                        });
            }         
        });

        // Retrieve event data from intent extras
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String date = intent.getStringExtra("date");
        String facility = intent.getStringExtra("facility");
        String registrationEndDate = intent.getStringExtra("registrationEndDate");
        String description = intent.getStringExtra("description");
        int maxWaitEntrants = intent.getIntExtra("maxWaitEntrants", 0);
        int maxSampleEntrants = intent.getIntExtra("maxSampleEntrants", 0);
        String posterUriString = intent.getStringExtra("posterUri");
        boolean isGeolocate = intent.getBooleanExtra("isGeolocate", false);
        Uri posterUri = posterUriString != null && !posterUriString.isEmpty() ? Uri.parse(posterUriString) : null;
        boolean isAdminPage = intent.getBooleanExtra("isAdminPage", false);

        // Bind event data to UI elements
        TextView nameTextView = findViewById(R.id.textViewName);
        TextView dateTextView = findViewById(R.id.textViewDate);
        TextView facilityTextView = findViewById(R.id.textViewFacility);
        TextView registrationEndDateTextView = findViewById(R.id.textViewRegistrationEndDate);
        TextView descriptionTextView = findViewById(R.id.textViewDescription);
        TextView maxWaitTextView = findViewById(R.id.textViewMaxWaitEntrants);
        TextView maxSampleTextView = findViewById(R.id.textViewMaxSampleEntrants);
        ImageView posterImageView = findViewById(R.id.imageViewPoster);
        TextView geolocateTextView = findViewById(R.id.textViewGeolocate);
        Button organizerActionsButton = findViewById(R.id.organizerActions);

        // Initialize notification button click listeners
        Button notifyWaitlisted = findViewById(R.id.buttonWaitlisted);
        Button notifyEnrolled = findViewById(R.id.buttonEnrolled);
        Button notifyCancelled = findViewById(R.id.buttonCancelled);
        Button notifyInvited = findViewById(R.id.buttonInvited);
        Button backButton = findViewById(R.id.buttonBackToEvents);
        Button editEventButton = findViewById(R.id.buttonEditEvent);


        // Bind retrieved data to respective UI components
        nameTextView.setText(name);
        dateTextView.setText(date);
        facilityTextView.setText(facility);
        registrationEndDateTextView.setText(registrationEndDate);
        descriptionTextView.setText(description);
        if (maxWaitEntrants == Integer.MAX_VALUE) {
            maxWaitTextView.setText(""+"No Limit");
        } else {
            maxWaitTextView.setText(""+maxWaitEntrants);
        }
        maxSampleTextView.setText(""+maxSampleEntrants);
        geolocateTextView.setText(isGeolocate ? "Geo-Location Enabled" : "Geo-Location Disabled");

        // If viewed from the adminPage, hide some buttons, repurpose others.
        if (isAdminPage){
            notifyCancelled.setVisibility(View.GONE);
            notifyEnrolled.setVisibility(View.GONE);
            notifyInvited.setVisibility(View.GONE);
            notifyWaitlisted.setVisibility(View.GONE);
            TextView notificationsHeaderText = findViewById(R.id.notificationsHeader);
            notificationsHeaderText.setVisibility(View.GONE);
            GridLayout notificationsBackground = findViewById(R.id.notificationsBackground);
            notificationsBackground.setVisibility(View.GONE);
            setDeleteQRCodeButton(organizerActionsButton);
            repurposeBackButton(backButton);

        }
        else{
            // Set up click listeners for notification buttons
            notifyWaitlisted.setOnClickListener(v -> showNotificationInputDialog("Waitlisted Notification", waitlistedMessage, message -> {
                waitlistedMessage = message;
                updateNotificationMessage("waitlistedMessage", message, "notifyWaitlisted");
                updateButtonAppearance(notifyWaitlisted, !message.isEmpty());
            }));

            notifyEnrolled.setOnClickListener(v -> showNotificationInputDialog("Accepted Notification", enrolledMessage, message -> {
                enrolledMessage = message;
                updateNotificationMessage("enrolledMessage", message, "notifyEnrolled");
                updateButtonAppearance(notifyEnrolled, !message.isEmpty());
            }));

            notifyCancelled.setOnClickListener(v -> showNotificationInputDialog("Cancelled Notification", cancelledMessage, message -> {
                cancelledMessage = message;
                updateNotificationMessage("cancelledMessage", message, "notifyCancelled");
                updateButtonAppearance(notifyCancelled, !message.isEmpty());
            }));

            notifyInvited.setOnClickListener(v -> showNotificationInputDialog("Invited Notification", invitedMessage, message -> {
                invitedMessage = message;
                updateNotificationMessage("invitedMessage", message, "notifyInvited");
                updateButtonAppearance(notifyInvited, !message.isEmpty());
            }));

            // Set up the back button to return to EventActivity
            backButton.setOnClickListener(v -> {
                Intent intent2 = new Intent(EventDetailActivity.this, EventActivity.class);
                intent2.putExtra("eventID", eventID);
                startActivity(intent2);
            });

            // Set up the back button to return to EventActivity
            backButton.setOnClickListener(v -> {
                Intent intent2 = new Intent(EventDetailActivity.this, EventActivity.class);
                intent2.putExtra("eventID", eventID);
                startActivity(intent2);
            });
            db.collection("events").document(eventID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean lotteryButton = documentSnapshot.getBoolean("lotteryButton");
                            if (lotteryButton != null && lotteryButton) {
                                // Lottery already ran, update button appearance
                                organizerActionsButton.setText("Lottery has been ran");
                                organizerActionsButton.setEnabled(false);
                                organizerActionsButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                            } else {
                                // Enable the button if lotteryButton is false or null
                                organizerActionsButton.setText("Run Lottery");
                                organizerActionsButton.setEnabled(true);
                            }
                        } else {
                            Log.w("EventDetailActivity", "No event found with ID: " + eventID);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("EventDetailActivity", "Error retrieving event with ID: " + eventID, e));

            // Set up organizer actions button to navigate to EventActionsActivity
            organizerActionsButton.setOnClickListener(v -> {
                db.collection("events").document(eventID)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Boolean lotteryButton = documentSnapshot.getBoolean("lotteryButton");
                                if (lotteryButton != null && lotteryButton) {
                                    // Lottery has already been run, update button appearance and disable
                                    organizerActionsButton.setText("Lottery has been ran");
                                    organizerActionsButton.setEnabled(false);
                                    organizerActionsButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                                    Log.d("EventDetailActivity", "Lottery already ran for event ID: " + eventID);
                                    return; // Exit early
                                }

                                // Create Event object with event details
                                Event event = new Event(
                                        name,
                                        date,
                                        facility,
                                        registrationEndDate,
                                        description,
                                        maxWaitEntrants,
                                        maxSampleEntrants,
                                        posterUriString,
                                        isGeolocate,
                                        documentSnapshot.getBoolean("notifyWaitlisted"),
                                        documentSnapshot.getBoolean("notifyEnrolled"),
                                        documentSnapshot.getBoolean("notifyCancelled"),
                                        documentSnapshot.getBoolean("notifyInvited"),
                                        documentSnapshot.getString("waitlistedMessage"),
                                        documentSnapshot.getString("enrolledMessage"),
                                        documentSnapshot.getString("cancelledMessage"),
                                        documentSnapshot.getString("invitedMessage"),
                                        documentSnapshot.getString("organizerID")
                                );

                                // Run the lottery for this event
                                event.lottery(db, eventID);

                                // Update button state and Firestore after lottery
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("lotteryButton", true); // Mark lottery as run
                                db.collection("events").document(eventID)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            organizerActionsButton.setText("Lottery has been ran");
                                            organizerActionsButton.setEnabled(false);
                                            organizerActionsButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                                            Log.d("EventDetailActivity", "Lottery successfully ran and button updated for event ID: " + eventID);
                                        })
                                        .addOnFailureListener(e -> Log.e("EventDetailActivity", "Failed to update lotteryButton flag", e));
                            } else {
                                Log.w("EventDetailActivity", "No event found with ID: " + eventID);
                            }
                        })
                        .addOnFailureListener(e -> Log.e("EventDetailActivity", "Error retrieving event with ID: " + eventID, e));
            });



        }
        // Set up edit event button to open EditEventDialogFragment
        editEventButton.setOnClickListener(v -> {
            EditEventDialogFragment dialog = new EditEventDialogFragment();
            Bundle args = new Bundle();
            args.putString("eventID", eventID);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "EditEventDialogFragment");
        });
        // Set up entrant list button to display entrants or show a message if none exist
        Button entrantListButton = findViewById(R.id.entrant_list_button);
        entrantListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference waitingListRef;
                waitingListRef = db.collection("events").document(eventID).collection("waitingList");

                waitingListRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalTasks = task.getResult().size();
                        if (totalTasks == 0){
                            // Check if there are any entrants on the waiting List
                            Toast.makeText(getApplicationContext(), "No entrants", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // If there are, switch to the view entrants activity.
                            Intent intent = new Intent(EventDetailActivity.this, EventEntrantsActivity.class);
                            intent.putExtra("eventID", eventID);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        // Set up map button to display the map of geolocations of entrants
        showMap = findViewById(R.id.map_button);
        if (isGeolocate) {
            showMap.setVisibility(View.VISIBLE);
            showMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EventDetailActivity.this, MapActivity.class);
                    intent.putExtra("name", name );
                    intent.putExtra("date", date);
                    intent.putExtra("facility", facility);
                    intent.putExtra("registrationEndDate", registrationEndDate);
                    intent.putExtra("description",description );
                    intent.putExtra("maxWaitEntrants", maxWaitEntrants);
                    intent.putExtra("maxSampleEntrants", maxSampleEntrants);
                    intent.putExtra("eventID", eventID);
                    intent.putExtra("posterUri", posterUriString);
                    intent.putExtra("isGeolocate", isGeolocate);
                    startActivity(intent);
                }
            });
        } else {
            // Hide button if geolocation is off
            showMap.setVisibility(View.GONE);
        }


        // Display poster image if URI is valid, using Glide library
        if (posterUri != null) {
            Glide.with(this).load(posterUri).into(posterImageView);
        }

        // Set up regenerate QR code button with a confirmation dialog
        Button regenerateQRCodeButton = findViewById(R.id.RegenerateQrCode);
        regenerateQRCodeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // Show a confirmation dialog for QR code regeneration
                AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailActivity.this);
                builder.setTitle("Confirm QR Code Regeneration");
                builder.setMessage("Are you sure you want to regeneration this events' QR Code? This will deactivate the current QR Code and generate a new one.");

                builder.setPositiveButton("Confirm Regeneration", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        regenerateQRCode();
                        Toast.makeText(EventDetailActivity.this, "Regenerated QR Code.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventDetailActivity.this, "Cancelled.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    /**
     * Configures a button to delete the QR code for the event.
     * Deleting the QR code will deactivate it and remove its reference from Firestore.
     *
     * @param oldButton The button to be configured for deleting the QR code.
     */
    public void setDeleteQRCodeButton(Button oldButton){
        oldButton.setText("Delete QR Code");
        oldButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // Show confirmation dialog before deleting the QR code
                AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailActivity.this);
                builder.setTitle("Confirm QR Code Deletion");
                builder.setMessage("Are you sure you want to delete this events' QR Code? This will deactivate the current QR Code.");

                // Confirm deletion
                builder.setPositiveButton("Confirm Deletion", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventDetailActivity.this, "Deleted QR Code.", Toast.LENGTH_SHORT).show();

                        // Remove QR code details from Firestore
                        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
                            qrCodeLocationURL = documentSnapshot.getString("qrCodeLocationUrl");
                            db.collection("events").document(eventID).update("qrCodeLocationUrl", null);
                            db.collection("events").document(eventID).update("qrCodePassKey", null);
                            dialog.dismiss();
                            recreate();
                        });
                    }
                });
                // Cancel deletion
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventDetailActivity.this, "Cancelled.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                // Show the confirmation dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    /**
     * Regenerates a QR code for the event. Deletes the existing QR code (if any),
     * creates a new one, and updates the QR code details in Firestore.
     */
    public void regenerateQRCode(){
        // Retrieve the event's QR code details from Firestore
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            qrCodeLocationURL = documentSnapshot.getString("qrCodeLocationUrl");

            // If a QR code exists, remove it from Firestore and Storage
            if (qrCodeLocationURL != null){
                db.collection("events").document(eventID).update("qrCodeLocationUrl", null);
                db.collection("events").document(eventID).update("qrCodePassKey", null);
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            if (qrCodeLocationURL  != null){
                // Delete the QR code file from Firebase Storage
                StorageReference qrCodeRef = storage.getReferenceFromUrl(qrCodeLocationURL);
                qrCodeRef.delete().addOnSuccessListener(v -> {
                    try {

                        QRCodeWriter qrCodeWriter = new QRCodeWriter();

                        // Generate a passKey for this version of the QRcode, store in database
                        String passKey = UUID.randomUUID().toString();
                        db.collection("events").document(eventID).update("qrCodePassKey", passKey);
                        String qrContent = "myapp://event/" + eventID +"/"+passKey;

                        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
                        int width = bitMatrix.getWidth();
                        int height = bitMatrix.getHeight();
                        Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                        byte[] qrCodeByte;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                            }
                        }
                        qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        qrCodeByte = baos.toByteArray();
                        storageRef.putBytes(qrCodeByte)
                                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String qrCodeLocationUrl = uri.toString();
                                    db.collection("events").document(eventID).update("qrCodeLocationUrl", qrCodeLocationUrl);
                                }));

                        // Set the QR code bitmap to the ImageView
                        qrImageView.setImageBitmap(qrBitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                });
            } else{
                try {

                    QRCodeWriter qrCodeWriter = new QRCodeWriter();

                    // Generate a passKey for this version of the QRcode, store in database
                    String passKey = UUID.randomUUID().toString();
                    db.collection("events").document(eventID).update("qrCodePassKey", passKey);
                    String qrContent = "myapp://event/" + eventID +"/"+passKey;

                    BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    byte[] qrCodeByte;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    qrCodeByte = baos.toByteArray();
                    storageRef.putBytes(qrCodeByte)
                            .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String qrCodeLocationUrl = uri.toString();
                                db.collection("events").document(eventID).update("qrCodeLocationUrl", qrCodeLocationUrl);
                            }));

                    // Set the QR code bitmap to the ImageView
                    qrImageView.setImageBitmap(qrBitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Repurposes the back button to navigate back to the admin page.
     *
     * @param backButton The button to be repurposed.
     */
    public void repurposeBackButton(Button backButton){
        backButton.setText("Back to Admin Page");
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Updates the event data with edited values from the EditEventDialogFragment.
     *
     * @param name              The updated event name.
     * @param date              The updated event date.
     * @param facility          The updated event location.
     * @param registrationEndDate The updated registration end date for the event.
     * @param description       The updated event description.
     * @param maxWaitEntrants   The updated maximum number of waitlist entrants.
     * @param maxSampleEntrants The updated maximum number of sample entrants.
     * @param posterUri         The updated URI of the event poster.
     * @param isGeolocate       Indicates whether geolocation is enabled for the event.
     * @param waitlistedMessage The updated notification message for waitlisted participants.
     * @param enrolledMessage   The updated notification message for enrolled participants.
     * @param cancelledMessage  The updated notification message for cancelled events.
     * @param invitedMessage    The updated notification message for invited participants.
     */
    @Override
    public void onEventEdited(String name, String date, String facility, String registrationEndDate,
                              String description, int maxWaitEntrants, int maxSampleEntrants,
                              String posterUri, boolean isGeolocate,
                              String waitlistedMessage, String enrolledMessage,
                              String cancelledMessage, String invitedMessage) {

        MyApp app = (MyApp) getApplication();
        String organizerID = app.getAndroidId();

        // Update event details in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("date", date);
        updates.put("facility", facility);
        updates.put("registrationEndDate", registrationEndDate);
        updates.put("description", description);
        updates.put("maxWaitEntrants", maxWaitEntrants);
        updates.put("maxSampleEntrants", maxSampleEntrants);
        updates.put("posterUri", posterUri);
        updates.put("geolocate", isGeolocate);
        updates.put("waitlistedMessage", waitlistedMessage);
        updates.put("enrolledMessage", enrolledMessage);
        updates.put("cancelledMessage", cancelledMessage);
        updates.put("invitedMessage", invitedMessage);

        // Firestore update with success and failure listeners
        db.collection("events").document(eventID)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Refresh the UI with updated event details
                    refreshEventUI(name, date, facility, registrationEndDate, description,
                            maxWaitEntrants, maxSampleEntrants, posterUri, isGeolocate);
                })
                .addOnFailureListener(e -> {
                    // Display an error message if the update fails
                    Toast.makeText(this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Create new Event object with notification messages
        Event updatedEvent = new Event(name, date, facility, registrationEndDate, description,
                maxWaitEntrants, maxSampleEntrants, posterUri, isGeolocate,
                false, false, false, false,
                "", "", "", "",
                organizerID);
    }

    /**
     * Refreshes the UI with updated event details.
     *
     * @param name              The updated event name.
     * @param date              The updated event date.
     * @param facilityId        The ID of the updated event facility.
     * @param registrationEndDate The updated registration end date.
     * @param description       The updated event description.
     * @param maxWaitEntrants   The updated maximum number of waitlist entrants.
     * @param maxSampleEntrants The updated maximum number of sample entrants.
     * @param posterUri         The updated URI of the event poster.
     * @param isGeolocate       Indicates whether geolocation is enabled for the event.
     */
    private void refreshEventUI(String name, String date, String facilityId, String registrationEndDate,
                                String description, int maxWaitEntrants, int maxSampleEntrants,
                                String posterUri, boolean isGeolocate) {

        // Fetch the facility name from Firestore using the facility ID
        db.collection("facilities").document(facilityId).get().addOnSuccessListener(documentSnapshot -> {
           String facName = documentSnapshot.getString("name");

            // Update UI components with new event details
            TextView nameTextView = findViewById(R.id.textViewName);
            TextView dateTextView = findViewById(R.id.textViewDate);
            TextView facilityTextView = findViewById(R.id.textViewFacility);
            TextView registrationEndDateTextView = findViewById(R.id.textViewRegistrationEndDate);
            TextView descriptionTextView = findViewById(R.id.textViewDescription);
            TextView maxWaitTextView = findViewById(R.id.textViewMaxWaitEntrants);
            TextView maxSampleTextView = findViewById(R.id.textViewMaxSampleEntrants);
            ImageView posterImageView = findViewById(R.id.imageViewPoster);
            TextView geolocateTextView = findViewById(R.id.textViewGeolocate);

            nameTextView.setText(name);
            dateTextView.setText(date);
            facilityTextView.setText(facName);
            registrationEndDateTextView.setText(registrationEndDate);
            descriptionTextView.setText(description);
            if (maxWaitEntrants == Integer.MAX_VALUE) {
                maxWaitTextView.setText(""+"No Limit");
            } else {
                maxWaitTextView.setText(""+maxWaitEntrants);
            }
            maxSampleTextView.setText(""+maxSampleEntrants);
            geolocateTextView.setText(isGeolocate ? "Geo-Location Enabled" : "Geo-Location Disabled");

            if (posterUri != null && !posterUri.isEmpty()) {
                Glide.with(this).load(posterUri).into(posterImageView);
            }
        });

    }

    /**
     * Updates the appearance of a button based on whether it is active.
     *
     * @param button The button to update
     * @param isActive True if the button should appear active, false otherwise
     */
    private void updateButtonAppearance(Button button, boolean isActive) {
        if (isActive) {
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        } else {
            button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    /**
     * Callback interface for notification message input.
     * Used to handle the user's message input in a notification dialog.
     */
    private interface NotificationMessageCallback {
        void onMessageSet(String message);
    }

    /**
     * Displays a dialog for the user to enter, edit, or delete a notification message.
     *
     * @param title           The title of the dialog.
     * @param existingMessage The existing message to be displayed in the input field.
     * @param callback        A callback to handle the message after user input.
     */
    private void showNotificationInputDialog(String title, String existingMessage, NotificationMessageCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setText(existingMessage); // Pre-fill the input field with the existing message
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String message = input.getText().toString();
            callback.onMessageSet(message); // Pass the entered message to the callback
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Delete", (dialog, which) -> {
            callback.onMessageSet("");
            Toast.makeText(getApplicationContext(), "Notification message deleted", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    /**
     * Updates notification messages for a specific event in Firestore.
     * Sets a notification field to true if a message is provided, otherwise sets it to false.
     *
     * @param messageField The Firestore field to store the notification message.
     * @param message      The notification message content.
     * @param notifyField  The Firestore field indicating whether to send the notification.
     */
    private void updateNotificationMessage(String messageField, String message, String notifyField) {
        boolean shouldNotify = !message.isEmpty();

        Map<String, Object> updates = new HashMap<>();
        updates.put(messageField, message); // Update the message field
        updates.put(notifyField, shouldNotify); // Update the notify field

        db.collection("events").document(eventID)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Notification updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update notification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
