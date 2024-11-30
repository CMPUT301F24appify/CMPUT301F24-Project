package com.example.appify.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appify.Adapters.ImageGridAdapter;
import com.example.appify.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageGridActivity extends AppCompatActivity {

    private List<String> imageUrls; // List to store image URLs
    private GridView gridView; // GridView to display the images
    private ImageGridAdapter adapter; // Custom adapter for the GridView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid); // GridView layout

        // Initialize components
        gridView = findViewById(R.id.image_grid_view);
        imageUrls = new ArrayList<>();
        adapter = new ImageGridAdapter(this, R.layout.grid_item_image, imageUrls);
        String refName = getIntent().getStringExtra("StorageReferenceName");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child(refName);


        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });
        TextView titleText = findViewById(R.id.title_text);
        if (refName.equals("event_posters")){

            titleText.setText("Event Posters");
        } else if (refName.equals("profile_images")) {
            titleText.setText("Profile Images");
        }

        ref.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    adapter.notifyDataSetChanged();
                });
            }
        });
        gridView.setAdapter(adapter);
    }
}