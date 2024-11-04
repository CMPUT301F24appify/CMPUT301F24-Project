package com.example.appify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

public class userProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView profileImageView;
    private TextView nameTextView, phoneTextView, emailTextView;
    private ListenerRegistration listenerRegistration;
    private Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        db = FirebaseFirestore.getInstance();

        // Get Android ID passed from the previous activity
        String android_id = getIntent().getStringExtra("Android ID");
        byte[] profilePicture = getIntent().getByteArrayExtra("Profile Picture");

        // Initialize Views
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        emailTextView = findViewById(R.id.emailTextView);
        editButton = findViewById(R.id.editButton);

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
                            //String profileImageUrl = documentSnapshot.getString("profilePictureUrl");
                            nameTextView.setText("Name: " + name);
                            phoneTextView.setText("Phone: " + phone);
                            emailTextView.setText("Email Address: " + email);
                            if (profilePicture != null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(profilePicture, 0, profilePicture.length);
                                profileImageView.setImageBitmap(bitmap);
                            }
                        }
                    });
        }
    }

}


