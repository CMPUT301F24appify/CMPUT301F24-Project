package com.example.appify.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ImageGridAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> imageUrls;
    private int resource;

    public ImageGridAdapter(@NonNull Context context, int resource, @NonNull List<String> imageUrls) {
        super(context, resource, imageUrls);
        this.context = context;
        this.imageUrls = imageUrls;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.image_item);
        ImageView x_icon = convertView.findViewById(R.id.x_icon);

        Glide.with(context)
                .load(imageUrls.get(position))
                .into(imageView);

        x_icon.setOnClickListener(v -> {
            showCancelImageDialog(imageUrls.get(position));
        });

        return convertView;
    }

    private void showCancelImageDialog(String imageUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder
                .setTitle("Delete Image")
                .setMessage("Please confirm that you want to delete the image. This action cannot be undone.")
                .setPositiveButton("Confirm", ((dialog, which) -> {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);

                    // Determine the folder from the image path
                    String folderName = imageRef.getParent().getName(); // Get folder name

                    // Delete the image from Firebase Storage
                    imageRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                // Update Firestore based on folder name
                                if ("profile_images".equals(folderName)) {
                                    // Search through the AndroidID collection for matching profilePictureUrl
                                    db.collection("AndroidID")
                                            .whereEqualTo("profilePictureUrl", imageUrl)
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                for (QueryDocumentSnapshot document : querySnapshot) {
                                                    // Set the profilePictureUrl to null
                                                    db.collection("AndroidID")
                                                            .document(document.getId())
                                                            .update("profilePictureUrl", null);
                                                }
                                            });

                                } else if ("event_posters".equals(folderName)) {
                                    // Search through the events collection for matching posterUri
                                    db.collection("events")
                                            .whereEqualTo("posterUri", imageUrl)
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                for (QueryDocumentSnapshot document : querySnapshot) {
                                                    // Set the posterUri to null
                                                    db.collection("events")
                                                            .document(document.getId())
                                                            .update("posterUri", null);
                                                }
                                            });
                                }

                                // Remove from local list and update UI
                                imageUrls.remove(imageUrl);
                                notifyDataSetChanged();
                            });
                }))
                .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));
        builder.create();
        builder.show();
    }
}
