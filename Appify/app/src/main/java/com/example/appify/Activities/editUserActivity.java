package com.example.appify.Activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.appify.HeaderNavigation;
import com.example.appify.Model.Entrant;
import com.example.appify.R;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Activity to edit the user's profile information.
 * Allows users to set their profile picture, name, phone number, email, and notification preferences.
 */
public class editUserActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private Uri imageUri;
    private String android_id;
    private byte[] profilePictureByte;
    private EditText nameEditText, phoneEditText, emailEditText;
    private CheckBox notifications;
    private String facilityID = null;
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

    /**
     *
     * Called when the activity is first created.
     * Sets up the layout, initializes views, retrieves user data,displays the profile picture and tests for First time entry.
     *
     * @param savedInstanceState most recent Data sent.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user);
        android_id = getIntent().getStringExtra("Android ID");
        boolean firstEntry = getIntent().getBooleanExtra("firstEntry", false);
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();

        // Fetch the EditText fields
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        profileImageView = findViewById(R.id.profileImageView);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button removeButton = findViewById(R.id.removeButton);
        Button submitButton = findViewById(R.id.submitButton);
        notifications = findViewById(R.id.notificationsCheckBox);
        Button cancelButton = findViewById(R.id.cancelButton);


        if (firstEntry) {
            cancelButton.setVisibility(View.GONE);
        }
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(editUserActivity.this, userProfileActivity.class);
            intent.putExtra("Android ID", android_id);
            startActivity(intent);
        });
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

            //Check Whether the Inputs are Correct for Name, Phone and Email
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(editUserActivity.this, "Please fill in both Name and Email Fields", Toast.LENGTH_SHORT).show();
            } else if (!Character.isLetter(name.charAt(0))) {
                Toast.makeText(editUserActivity.this, "Name must start with a letter", Toast.LENGTH_SHORT).show();
            } else if (phoneNumber.length() != 10 && !phoneNumber.isEmpty()) {
                Toast.makeText(editUserActivity.this, "Please enter a 10 digit Phone Number", Toast.LENGTH_SHORT).show();

            } else if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                Toast.makeText(editUserActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            }  else {
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

    /**
     * Opens the file chooser to select an image from the devices files.
     */
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
    }

    /**
     * Generates a profile picture with the user's first name initial and a random background color.
     *
     * @param firstLetter The first letter of the user's name.
     * @return A Bitmap of the generated profile picture.
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
     * Saves the user's data and profile picture to Firestore and Firebase Storage.
     *
     * @param id The user's unique device ID.
     * @param name The user's name.
     * @param phone The user's phone number.
     * @param email The user's email address.
     */
    private void sendEntrantData(String id,String name, String phone, String email){
        Bitmap profilePicture = getBitmapFromImageView(profileImageView);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + android_id + ".jpg");
        boolean notificationCheck = notifications.isChecked();
        // Convert Bitmap to ByteArray
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profilePicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        profilePictureByte = baos.toByteArray();
        storageRef.putBytes(profilePictureByte)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();

                    // Create Entrant object with the download URL
                    Entrant user = new Entrant(id, name, phone, email, downloadUrl, notificationCheck);
                    user.setFacilityID(facilityID);
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

    /**
     * Populates the Text Boxes with the user's data retrieved from the Database.
     *
     * @param android_id The user's unique device ID.
     */
    private void populateFields(String android_id){
        db.collection("Android ID").document(android_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve current user data
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phoneNumber");
                        String email = documentSnapshot.getString("email");
                        facilityID = documentSnapshot.getString("facilityID");
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
    /**
     * Retrieves the Bitmap from an ImageView.
     *
     * @param imageView The ImageView used to create the Bitmap8.
     * @return A Bitmap created from the ImageView.
     */
    private Bitmap getBitmapFromImageView(ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        return Bitmap.createBitmap(imageView.getDrawingCache());
    }
    /**
     * Loads the user's profile picture from Firebase Storage and sets it.
     *
     * @param android_id The user's unique device ID.
     */
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