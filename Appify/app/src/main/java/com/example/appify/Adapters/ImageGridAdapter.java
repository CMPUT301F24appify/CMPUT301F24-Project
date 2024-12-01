package com.example.appify.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appify.Activities.editUserActivity;
import com.example.appify.Activities.userProfileActivity;
import com.example.appify.Model.Entrant;
import com.example.appify.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;

public class ImageGridAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> imageUrls;
    private int resource;
    private String pictureUri;

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
    public interface OnProfileURLGenerated {
        void onSuccess(String url);
        void onFailure(Exception e);
    }
}
