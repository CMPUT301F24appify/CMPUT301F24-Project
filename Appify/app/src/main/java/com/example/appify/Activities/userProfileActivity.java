package com.example.appify.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appify.HeaderNavigation;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;

/**
 * Activity to display the user's profile information.
 * Fetches data and photo from Database and displays it.
 */
public class userProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView profileImageView, headerImageView;
    private TextView nameTextView, phoneTextView, emailTextView, notificationTextView;
    private ListenerRegistration listenerRegistration;
    private Button editButton, adminButton;

    /**
     * Called when the activity is first created.
     * Sets up the layout, initializes views, retrieves user data, and displays the profile picture.
     *
     * @param savedInstanceState most recent Data sent.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        db = FirebaseFirestore.getInstance();

        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();
        MyApp app = (MyApp) getApplication();
        String android_id = app.getAndroidId();

        // Initialize Views
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        emailTextView = findViewById(R.id.emailTextView);
        notificationTextView = findViewById(R.id.notificationTextView);
        editButton = findViewById(R.id.editButton);
        adminButton = findViewById(R.id.adminButton);
        headerImageView = findViewById(R.id.profileImageViewHeader);

        // Edit button to open edit activity
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(userProfileActivity.this, editUserActivity.class);
            intent.putExtra("Android ID", android_id);
            startActivity(intent);
        });

        // Handle Admin Button Click
        adminButton.setOnClickListener(v -> {
            db.collection("Android ID").document(android_id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");

                            // Toggle isAdmin state
                            boolean newAdminState = isAdmin == null || !isAdmin; // Default to false if null
                            db.collection("Android ID").document(android_id)
                                    .update("isAdmin", newAdminState)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update button color and show a success message
                                        if (newAdminState) {
                                            adminButton.setBackgroundColor(Color.GREEN);
                                        } else {
                                            adminButton.setBackgroundColor(Color.RED);
                                        }
                                    });
                        } else {
                            // If the document doesn't exist, set isAdmin to true
                            db.collection("Android ID").document(android_id)
                                    .set(Collections.singletonMap("isAdmin", true), SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        adminButton.setBackgroundColor(Color.GREEN);
                                    });
                        }
                    });
        });

        //Retrieve and display user data
        if (android_id != null) {
            db.collection("Android ID").document(android_id).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phone = documentSnapshot.getString("phoneNumber");
                            String email = documentSnapshot.getString("email");
                            Boolean notificationsCheck = documentSnapshot.getBoolean("notifications");
                            Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                            //String profileImageUrl = documentSnapshot.getString("profilePictureUrl");
                            nameTextView.setText("Name: " + name);
                            if(phone == ""){
                                phoneTextView.setText("");
                            }
                            else{
                                phoneTextView.setText("Phone: " + phone);
                            }
                            emailTextView.setText("Email Address: " + email);

                            if (notificationsCheck != null && notificationsCheck) {
                                notificationTextView.setText("Notifications: ON");
                            } else {
                                notificationTextView.setText("Notifications: OFF");
                            }

                            // Set Admin Button based on isAdmin
                            if (isAdmin != null && isAdmin) {
                                adminButton.setBackgroundColor(Color.GREEN);
                            } else {
                                adminButton.setBackgroundColor(Color.RED);
                            }

                            loadProfilePicture(android_id);
                        }
                    });
        }
    }
    /**
     * Loads the user's profile picture from Firebase Storage based on the Android ID.
     *
     * @param android_id Unique identifier for the user's device.
     */
    private void loadProfilePicture(String android_id) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + android_id + ".jpg");

        long size = 1024 * 1024;
        storageRef.getBytes(size)
                .addOnSuccessListener(bytes -> {
                    // Convert the byte array to a Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    profileImageView.setImageBitmap(bitmap);
                    //headerImageView.setImageBitmap(bitmap);
                });
    }
}
