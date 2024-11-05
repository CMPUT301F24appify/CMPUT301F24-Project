package com.example.appify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);




        // Retrieve data from the Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String date = intent.getStringExtra("date");
        String facility = intent.getStringExtra("facility");
        String deadline = intent.getStringExtra("deadline");
        String description = intent.getStringExtra("description");
        int maxWishEntrants = intent.getIntExtra("maxWishEntrants", 0);
        int maxSampleEntrants = intent.getIntExtra("maxSampleEntrants", 0);
        String posterUriString = intent.getStringExtra("posterUri");
        boolean isGeolocate = intent.getBooleanExtra("isGeolocate", false);
        String eventID = intent.getStringExtra("eventID");


        Uri posterUri = posterUriString != null && !posterUriString.isEmpty() ? Uri.parse(posterUriString) : null;

        // Bind data to views
        TextView nameTextView = findViewById(R.id.textViewName);
        TextView dateTextView = findViewById(R.id.textViewDate);
        TextView facilityTextView = findViewById(R.id.textViewFacility);
        TextView deadlineTextView = findViewById(R.id.textViewDeadline);
        TextView descriptionTextView = findViewById(R.id.textViewDescription);
        TextView maxWishTextView = findViewById(R.id.textViewMaxWishEntrants);
        TextView maxSampleTextView = findViewById(R.id.textViewMaxSampleEntrants);
        ImageView posterImageView = findViewById(R.id.imageViewPoster);
        TextView geolocateTextView = findViewById(R.id.textViewGeolocate);

        nameTextView.setText(name);
        dateTextView.setText(date);
        facilityTextView.setText(facility);
        deadlineTextView.setText(deadline);
        descriptionTextView.setText(description);
        maxWishTextView.setText("Max Wish Entrants: " + maxWishEntrants);
        maxSampleTextView.setText("Max Sample Entrants: " + maxSampleEntrants);
        geolocateTextView.setText(isGeolocate ? "Geo-Location Enabled" : "Geo-Location Disabled");

        Button backButton = findViewById(R.id.buttonBackToEvents);
        backButton.setOnClickListener(v -> finish());

        Button entrantListButton = findViewById(R.id.entrant_list_button);
        entrantListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventDetailActivity.this, EventEntrantsActivity.class);
                intent.putExtra("eventID", eventID);

                startActivity(intent);
            }
        });


        // Display the image if the URI is valid
        if (posterUri != null && !posterUri.toString().isEmpty()) {
            // Use Glide to load the image from the Firebase Storage URL
            Glide.with(this).load(posterUri).into(posterImageView);
        } else {
//            posterImageView.setImageResource(R.drawable.placeholder_image);  // Set a placeholder if no image is available
        }
    }
}

