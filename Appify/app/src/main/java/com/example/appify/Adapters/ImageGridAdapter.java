package com.example.appify.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;

/**
 * ImageGridAdapter is a custom ArrayAdapter for displaying images in a grid layout.
 * It binds image URLs to ImageView elements and provides functionality to delete images.
 */
public class ImageGridAdapter extends ArrayAdapter<String> {

    private Context context; // Context in which the adapter is used
    private List<String> imageUrls; // List of image URLs to display
    private int resource; // Layout resource ID for each grid item

    /**
     * Constructor for initializing the adapter with image data and context.
     *
     * @param context   The current context.
     * @param resource  Resource ID for the grid item layout.
     * @param imageUrls List of image URLs to display.
     */
    public ImageGridAdapter(@NonNull Context context, int resource, @NonNull List<String> imageUrls) {
        super(context, resource, imageUrls);
        this.context = context;
        this.imageUrls = imageUrls;
        this.resource = resource;
    }

    /**
     * Provides a view for each image in the grid.
     *
     * @param position    Position of the image in the list.
     * @param convertView Recycled view (if available).
     * @param parent      Parent view group.
     * @return Updated View for the image at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            // Inflate the layout for a single grid item
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        // Bind ImageView and delete icon
        ImageView imageView = convertView.findViewById(R.id.image_item);
        ImageView x_icon = convertView.findViewById(R.id.x_icon);

        // Load the image from the URL into the ImageView using Glide
        Glide.with(context)
                .load(imageUrls.get(position))
                .into(imageView);

        // Set click listener for the delete icon
        x_icon.setOnClickListener(v -> {
            showCancelImageDialog(imageUrls.get(position)); // Show confirmation dialog for deleting the image
        });

        return convertView;
    }

    /**
     * Displays a confirmation dialog for deleting an image.
     * Deletes the image from Firebase Storage and updates the local list and Firestore as needed.
     *
     * @param imageUrl The URL of the image to delete.
     */
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

                                                    getNewProfileURL(document.getId(), new OnProfileURLGenerated() {
                                                        @Override
                                                        public void onSuccess(String url) {
                                                            db.collection("AndroidID")
                                                                    .document(document.getId())
                                                                    .update("profilePictureUrl", url);
                                                            db.collection("AndroidID")
                                                                    .document(document.getId()).update("generatedPicture", true);
                                                        }
                                                        @Override
                                                        public void onFailure(Exception e) {
                                                        }
                                                    });

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

    /**
     * Generates a profile picture bitmap with a random background and the first letter of a name.
     *
     * @param firstLetter The first letter to display in the profile picture.
     * @return A bitmap representing the generated profile picture.
     */
    private Bitmap generateProfilePicture(String firstLetter) {
        int imageSize = 150;  // 150x150

        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);

        // Generate a random background color
        int backgroundColor = getRandomColor();
        canvas.drawColor(backgroundColor);

        // Set up the paint for drawing the text (the first letter)
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(75);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);

        // Draw the letter in the center
        Rect bounds = new Rect();
        paint.getTextBounds(firstLetter, 0, firstLetter.length(), bounds);
        int x = canvas.getWidth() / 2;
        int y = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));

        canvas.drawText(firstLetter, x, y, paint);
        return bitmap;
    }

    /**
     * Generates a random color for the profile picture background.
     * @return Color represented as an RGB integer.
     */
    private int getRandomColor() {
        Random random = new Random();
        int red = random.nextInt(200) + 55;
        int green = random.nextInt(200) + 55;
        int blue = random.nextInt(200) + 55;
        return Color.rgb(red, green, blue);
    }

    /**
     * Generates a new profile picture URL for a user and uploads it to Firebase Storage.
     *
     * @param android_id The ID of the Android user.
     * @param callback   A callback for handling the success or failure of the URL generation.
     */
    public void getNewProfileURL(String android_id, OnProfileURLGenerated callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("generated_pictures/" + android_id + ".jpg");

        db.collection("AndroidID").document(android_id).get().addOnSuccessListener(getName -> {
            String name = getName.getString("name");
            String firstLetter = String.valueOf(name.charAt(0)).toUpperCase();
            Bitmap profilePicture = generateProfilePicture(firstLetter);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            profilePicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] profilePictureByte = baos.toByteArray();
            storageRef.putBytes(profilePictureByte)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String pictureUri = uri.toString();
                        callback.onSuccess(pictureUri); // Invoke the callback with the result
                    }));
        });
    }

    /**
     * Interface for handling profile picture URL generation callbacks.
     */
    public interface OnProfileURLGenerated {
        /**
         * Called when the profile picture URL is successfully generated.
         *
         * @param url The generated URL.
         */
        void onSuccess(String url);
        /**
         * Called when an error occurs during URL generation.
         *
         * @param e The exception representing the error.
         */
        void onFailure(Exception e);
    }
}
