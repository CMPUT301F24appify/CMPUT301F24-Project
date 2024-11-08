package com.example.appify.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appify.QRScanActivity;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {
    private ImageView qrCodeImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);



        // Retrieve data from the Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String date = intent.getStringExtra("date");
        String facility = intent.getStringExtra("facility");
        String registrationEndDate = intent.getStringExtra("registrationEndDate");
        String description = intent.getStringExtra("description");
        int maxWishEntrants = intent.getIntExtra("maxWishEntrants", 0);
        int maxSampleEntrants = intent.getIntExtra("maxSampleEntrants", 0);
        String posterUriString = intent.getStringExtra("posterUri");
        boolean isGeolocate = intent.getBooleanExtra("isGeolocate", false);
        String eventID = intent.getStringExtra("eventID");


        Uri posterUri = posterUriString != null && !posterUriString.isEmpty() ? Uri.parse(posterUriString) : null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Bind data to views
        TextView nameTextView = findViewById(R.id.textViewName);
        TextView dateTextView = findViewById(R.id.textViewDate);
        TextView facilityTextView = findViewById(R.id.textViewFacility);
        TextView registrationEndDateTextView = findViewById(R.id.textViewRegistrationEndDate);
        TextView descriptionTextView = findViewById(R.id.textViewDescription);
        TextView maxWishTextView = findViewById(R.id.textViewMaxWishEntrants);
        TextView maxSampleTextView = findViewById(R.id.textViewMaxSampleEntrants);
        ImageView posterImageView = findViewById(R.id.imageViewPoster);
        TextView geolocateTextView = findViewById(R.id.textViewGeolocate);
        Button organizerActionsButton = findViewById(R.id.organizerActions);

        nameTextView.setText(name);
        dateTextView.setText(date);
        facilityTextView.setText(facility);
        registrationEndDateTextView.setText(registrationEndDate);
        descriptionTextView.setText(description);
        maxWishTextView.setText("Max Wish Entrants: " + maxWishEntrants);
        maxSampleTextView.setText("Max Sample Entrants: " + maxSampleEntrants);
        geolocateTextView.setText(isGeolocate ? "Geo-Location Enabled" : "Geo-Location Disabled");

        Button backButton = findViewById(R.id.buttonBackToEvents);
        backButton.setOnClickListener(v -> {
            Intent intent2 = new Intent(EventDetailActivity.this, EventActivity.class);
            intent2.putExtra("eventID", eventID);
            startActivity(intent2);

        });


        organizerActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(EventDetailActivity.this, EventActionsActivity.class);
                    intent.putExtra("name", name );
                    intent.putExtra("date", date);
                    intent.putExtra("facility", facility);
                    intent.putExtra("registrationEndDate", registrationEndDate);
                    intent.putExtra("description",description );
                    intent.putExtra("maxWishEntrants", maxWishEntrants);
                    intent.putExtra("maxSampleEntrants", maxSampleEntrants);
                    intent.putExtra("eventID", eventID);
                    startActivity(intent);
                }
            });
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
                            // Check if there are any entrants on the waiting List.
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


        // Display the image if the URI is valid
        if (posterUri != null) {
            // Use Glide to load the image from the Firebase URL
            Glide.with(this).load(posterUri).into(posterImageView);
        } else {
//            posterImageView.setImageResource(R.drawable.placeholder_image);  // Set a placeholder if no image is available
        }
    }

}

