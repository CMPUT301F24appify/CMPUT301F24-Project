package com.example.appify;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class editUserActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private Uri imageUri;
    private String android_id;
    private byte[] profilePictureByte;
    private EditText nameEditText, phoneEditText, emailEditText;
    private CheckBox notifications;
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        profileImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        profileImageView.setImageURI(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user);
        android_id = getIntent().getStringExtra("Android ID");

        // Fetch the EditText fields
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        profileImageView = findViewById(R.id.profileImageView);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button removeButton = findViewById(R.id.removeButton);
        Button submitButton = findViewById(R.id.submitButton);
        notifications = findViewById(R.id.notificationsCheckBox);

        db = FirebaseFirestore.getInstance();
        populateFields(android_id);
        uploadButton.setOnClickListener(v -> openFileChooser());

        removeButton.setOnClickListener(v -> {
            profileImageView.setImageResource(R.drawable.default_profile);  // Reset to default image
            imageUri = null;
        });

        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String phoneNumber = phoneEditText.getText().toString();
            String email = emailEditText.getText().toString();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(editUserActivity.this, "Please fill in both Name and Email Fields", Toast.LENGTH_SHORT).show();
            } else {
                // Generate profile picture
                if (imageUri == null) {
                    String firstLetter = String.valueOf(name.charAt(0)).toUpperCase();
                    Bitmap profilePicture = generateProfilePicture(firstLetter);
                    profileImageView.setImageBitmap(profilePicture);
                }
                //Submit Data and open other Activity
                sendEntrantData(android_id, name, phoneNumber, email);
            }

        });
    }

    // Method to open the gallery or file picker
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);  // Launch the file picker
    }

    // Generate a profile picture with the first letter of the user's name
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

    // Generate Random Color for Pfp
    private int getRandomColor() {
        Random random = new Random();
        int red = random.nextInt(200) + 55;
        int green = random.nextInt(200) + 55;
        int blue = random.nextInt(200) + 55;
        return Color.rgb(red, green, blue);
    }

    private void sendEntrantData(String id,String name, String phone, String email){
        Bitmap profilePicture = getBitmapFromImageView(profileImageView);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + android_id + ".jpg");
        boolean notifcationCheck = notifications.isChecked();
        // Convert Bitmap to ByteArray
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profilePicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        profilePictureByte = baos.toByteArray();
        storageRef.putBytes(profilePictureByte)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();

                    // Create Entrant object with the download URL
                    Entrant user = new Entrant(id, name, phone, email, downloadUrl, notifcationCheck);

                    // Save Entrant data to Firestore
                    db.collection("Android ID").document(android_id).set(user)
                            .addOnSuccessListener(aVoid -> {
                                // Successfully saved data to Firestore
                                Intent intent = new Intent(editUserActivity.this, userProfileActivity.class);
                                intent.putExtra("Android ID", android_id);
                                startActivity(intent);
                            });
                }));
    }
    private void populateFields(String android_id){
        db.collection("Android ID").document(android_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve current user data
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phoneNumber");
                        String email = documentSnapshot.getString("email");
                        //String profileImageUrl = documentSnapshot.getString("profilePictureUrl");

                        // Populate the EditText fields with the retrieved data
                        nameEditText.setText(name);
                        phoneEditText.setText(phone);
                        emailEditText.setText(email);
                        notifications.setChecked(documentSnapshot.getBoolean("notifications"));
                        loadProfilePicture(android_id);
                    }
                });
    }
    private Bitmap getBitmapFromImageView(ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        return Bitmap.createBitmap(imageView.getDrawingCache());
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
                });
    }
}