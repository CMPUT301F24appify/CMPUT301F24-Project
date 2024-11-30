package com.example.appify.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Adapters.ImageGridAdapter;
import com.example.appify.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImageGridActivity extends AppCompatActivity {

    private List<String> imageUrls;
    private Map<String, Long> timeMap;
    private GridView gridView;
    private ImageGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid); // GridView layout

        // Initialize components
        gridView = findViewById(R.id.image_grid_view);
        imageUrls = new ArrayList<>();
        timeMap = new LinkedHashMap<>();
        adapter = new ImageGridAdapter(this, R.layout.grid_item_image, imageUrls);

        String refName = getIntent().getStringExtra("StorageReferenceName");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child(refName);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        TextView titleText = findViewById(R.id.title_text);
        if (refName.equals("event_posters")) {
            titleText.setText("Event Posters");
        } else if (refName.equals("profile_images")) {
            titleText.setText("Profile Images");
        }

        // Fetch images and metadata from Firebase
        ref.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getMetadata().addOnSuccessListener(metadata -> {
                    item.getDownloadUrl().addOnSuccessListener(uri -> {

                        timeMap.put(uri.toString(), metadata.getCreationTimeMillis());
                        imageUrls.clear();
                        imageUrls.addAll(
                                timeMap.entrySet().stream()
                                        .sorted(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey)
                                        .collect(Collectors.toList())
                        );
                        adapter.notifyDataSetChanged();
                    });
                });
            }
        });

        gridView.setAdapter(adapter);
    }
}
