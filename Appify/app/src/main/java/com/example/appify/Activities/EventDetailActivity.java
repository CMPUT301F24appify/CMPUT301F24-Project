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
 * The EventDetailActivity class displays and manages the details of a selected event.
 * This class allows users to view, edit, and send notifications to participants.
 */
public class EventDetailActivity extends AppCompatActivity implements EditEventDialogFragment.EditEventDialogListener {
    private FirebaseFirestore db;
    private String eventID;
    private Button showMap;

    // Variables to store different notification messages for participants
    private String waitlistedMessage = "";
    private String enrolledMessage = "";
    private String cancelledMessage = "";
    private String invitedMessage = "";
    private String qrCodeLocationURL;
    private StorageReference storageRef;
    private ImageView qrImageView;


    /**
     * Called when the activity is created. Sets up the UI and initializes data fields.
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
        eventID = getIntent().getStringExtra("eventID");
        if (eventID == null || eventID.isEmpty()) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity gracefully
            return;
        }
        storageRef = storage.getReference().child("qrcode_images/" + eventID + ".jpg");

        // QR code generation for event-specific content
        qrImageView = findViewById(R.id.qr_code);




//         If QR code exists in database use it. Otherwise, generate new one and store in db
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            String qrCodeLocationURL = documentSnapshot.getString("qrCodeLocationUrl");

            if (qrCodeLocationURL != null){
                // Qr code exists
                System.out.println(qrCodeLocationURL);
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
        System.out.println("Viewed from admin page: "+isAdminPage);

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
            showMap.setVisibility(View.GONE);
        }


        // Display poster image if URI is valid, using Glide library
        if (posterUri != null) {
            Glide.with(this).load(posterUri).into(posterImageView);
        }

        Button regenerateQRCodeButton = findViewById(R.id.RegenerateQrCode);
        regenerateQRCodeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
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

    public void setDeleteQRCodeButton(Button oldButton){
        oldButton.setText("Delete QR Code");
        oldButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailActivity.this);
                builder.setTitle("Confirm QR Code Deletion");
                builder.setMessage("Are you sure you want to delete this events' QR Code? This will deactivate the current QR Code.");

                builder.setPositiveButton("Confirm Deletion", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventDetailActivity.this, "Deleted QR Code.", Toast.LENGTH_SHORT).show();

                        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
                            qrCodeLocationURL = documentSnapshot.getString("qrCodeLocationUrl");
                            System.out.println(qrCodeLocationURL);
                            db.collection("events").document(eventID).update("qrCodeLocationUrl", null);
                            db.collection("events").document(eventID).update("qrCodePassKey", null);
                            dialog.dismiss();
                            recreate();
                        });
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

    public void regenerateQRCode(){

        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            qrCodeLocationURL = documentSnapshot.getString("qrCodeLocationUrl");
            System.out.println("abc"+qrCodeLocationURL);
            if (qrCodeLocationURL != null){
                db.collection("events").document(eventID).update("qrCodeLocationUrl", null);
                db.collection("events").document(eventID).update("qrCodePassKey", null);
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            if (qrCodeLocationURL  != null){
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
                        System.out.println(storageRef.getBytes(1024 * 1024));

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
                    System.out.println(storageRef.getBytes(1024 * 1024));

                    // Set the QR code bitmap to the ImageView
                    qrImageView.setImageBitmap(qrBitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void repurposeBackButton(Button backButton){
        backButton.setText("Back to Admin Page");
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Updates the event data with edited values from the EditEventDialogFragment.
     *
     * @param name Event name
     * @param date Event date
     * @param facility Event location
     * @param registrationEndDate Event registration end date
     * @param description Event description
     * @param maxWaitEntrants Maximum waitlist entrants
     * @param maxSampleEntrants Maximum sample entrants
     * @param posterUri URI of the event poster
     * @param isGeolocate Geo-location status of the event
     * @param waitlistedMessage Notification message for waitlisted participants
     * @param enrolledMessage Notification message for enrolled participants
     * @param cancelledMessage Notification message for cancelled events
     * @param invitedMessage Notification message for invited participants
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
                    refreshEventUI(name, date, facility, registrationEndDate, description,
                            maxWaitEntrants, maxSampleEntrants, posterUri, isGeolocate);
                })
                .addOnFailureListener(e -> {
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
     * @param name Event name
     * @param date Event date
     * @param facilityName Event facility
     * @param registrationEndDate Registration end date
     * @param description Event description
     * @param maxWaitEntrants Maximum waitlist entrants
     * @param maxSampleEntrants Maximum sample entrants
     * @param posterUri URI of the event poster
     * @param isGeolocate Geo-location status of the event
     */
    private void refreshEventUI(String name, String date, String facilityName, String registrationEndDate,
                                String description, int maxWaitEntrants, int maxSampleEntrants,
                                String posterUri, boolean isGeolocate) {

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
        facilityTextView.setText(facilityName);
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
     */
    private interface NotificationMessageCallback {
        void onMessageSet(String message);
    }

    /**
     * Shows a dialog for the user to enter or edit a notification message.
     *
     * @param title Title of the dialog
     * @param existingMessage Existing message text to be displayed in the dialog
     * @param callback Callback function to handle the message once set
     */
    private void showNotificationInputDialog(String title, String existingMessage, NotificationMessageCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setText(existingMessage);
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String message = input.getText().toString();
            callback.onMessageSet(message);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Delete", (dialog, which) -> {
            callback.onMessageSet("");
            Toast.makeText(getApplicationContext(), "Notification message deleted", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    /**
     * Updates notification messages in Firestore for a specific event.
     *
     * @param messageField Firestore field to store the message
     * @param message The notification message
     * @param notifyField Field indicating whether to send the notification
     */
    private void updateNotificationMessage(String messageField, String message, String notifyField) {
        boolean shouldNotify = !message.isEmpty();

        Map<String, Object> updates = new HashMap<>();
        updates.put(messageField, message);
        updates.put(notifyField, shouldNotify);

        db.collection("events").document(eventID)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Notification updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update notification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
