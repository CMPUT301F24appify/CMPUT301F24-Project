package com.example.appify.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appify.HeaderNavigation;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class userProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView profileImageView, headerImageView;
    private TextView nameTextView, phoneTextView, emailTextView, notificationTextView;
    private ListenerRegistration listenerRegistration;
    private Button editButton;


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
        headerImageView = findViewById(R.id.profileImageViewHeader);
        // Edit button to open edit activity
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(userProfileActivity.this, editUserActivity.class);
            intent.putExtra("Android ID", android_id);
            startActivity(intent);
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
                            loadProfilePicture(android_id);
                            //headerImageView.setImageBitmap(app.loadProfilePictureBitmap());
                        }
                    });
        }
    }
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
