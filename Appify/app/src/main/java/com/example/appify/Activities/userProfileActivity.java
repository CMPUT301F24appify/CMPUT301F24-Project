package com.example.appify.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Activity to display the user's profile information.
 * Fetches data and photo from Database and displays it.
 */
public class userProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView profileImageView, headerImageView;
    private TextView nameTextView, phoneTextView, emailTextView;
    private ListenerRegistration listenerRegistration;
    private Button editButton, adminButton;
    private boolean generatePicture = false;

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
        editButton = findViewById(R.id.editButton);
        adminButton = findViewById(R.id.adminButton);
        headerImageView = findViewById(R.id.profileImageViewHeader);

        // Edit button to open edit activity
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(userProfileActivity.this, editUserActivity.class);
            intent.putExtra("AndroidID", android_id);
            intent.putExtra("pictureFlag", generatePicture);
            startActivity(intent);
        });

        // Handle Admin Button Click
        adminButton.setOnClickListener(v -> {
            db.collection("AndroidID").document(android_id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean isAdmin = documentSnapshot.getBoolean("admin");

                            // Toggle isAdmin state
                            boolean newAdminState = isAdmin == null || !isAdmin; // Default to false if null
                            db.collection("AndroidID").document(android_id)
                                    .update("admin", newAdminState)
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
                            db.collection("AndroidID").document(android_id)
                                    .set(Collections.singletonMap("admin", true), SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        adminButton.setBackgroundColor(Color.GREEN);
                                    });
                        }
                    });
        });

        //Retrieve and display user data
        if (android_id != null) {
            db.collection("AndroidID").document(android_id).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phone = documentSnapshot.getString("phoneNumber");
                            String email = documentSnapshot.getString("email");
                            Boolean notificationsCheck = documentSnapshot.getBoolean("notifications");
                            Boolean isAdmin = documentSnapshot.getBoolean("admin");
                            TextView phoneText = findViewById(R.id.phoneText);
                            generatePicture = documentSnapshot.getBoolean("generatedPicture");
                            //String profileImageUrl = documentSnapshot.getString("profilePictureUrl");
                            nameTextView.setText(name);
                            if(Objects.equals(phone, "")){
                                phoneTextView.setVisibility(View.GONE);
                                phoneText.setVisibility(View.GONE);
                            }
                            else{
                                phoneTextView.setText(phone);
                            }
                            emailTextView.setText(email);

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
     * Loads the user's profile picture from Firebase Storage based on the AndroidID.
     *
     * @param android_id Unique identifier for the user's device.
     */
    private void loadProfilePicture(String android_id) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        db.collection("AndroidID").document(android_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean generated = documentSnapshot.getBoolean("generatedPicture");
                        String path = generated ? "generated_pictures/" : "profile_images/";
                        StorageReference storageRef = storage.getReference().child(path + android_id + ".jpg");

                        long size = 1024 * 1024;
                        storageRef.getBytes(size)
                                .addOnSuccessListener(bytes -> {
                                    // Convert the byte array to a Bitmap
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    profileImageView.setImageBitmap(bitmap);
                                    //headerImageView.setImageBitmap(bitmap);
                                });
                    }
                });

    }
}
