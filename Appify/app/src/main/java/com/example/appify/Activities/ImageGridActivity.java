package com.example.appify.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ImageGridActivity extends AppCompatActivity {
    private List<Bitmap> bitmapList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user); // Change to grid view

        String refName = getIntent().getStringExtra("StorageReferenceName");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child(refName);
        long size = 1024 * 1024;
        ref.listAll().addOnSuccessListener(listResult -> {
            // Iterate over each file item
            for (StorageReference item : listResult.getItems()) {
                // Download the item as bytes
                System.out.println(item.getName());
                item.getBytes(size).addOnSuccessListener(bytes -> {
                    // Decode bytes into a Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    bitmapList.add(bitmap);

                });
            }
        });
    }
}
