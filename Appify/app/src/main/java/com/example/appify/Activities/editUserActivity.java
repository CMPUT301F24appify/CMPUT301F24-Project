package com.example.appify.Activities;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.appify.HeaderNavigation;
import com.example.appify.Model.Entrant;
import com.example.appify.R;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Activity to edit the user's profile information.
 * Allows users to set their profile picture, name, phone number, email, and notification preferences.
 */
public class editUserActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private Uri imageUri = null;
    private String android_id;
    private byte[] profilePictureByte;
    private EditText nameEditText, phoneEditText, emailEditText;
    private String facilityID = null;
    private double deviceLatitude;
    private double deviceLongitude;
    private LocationRequest deviceLocationRequest;
    private Bitmap bitmapImage = null;
    private boolean defaultFlag = true;
    private boolean cameraFlag,generatedPicture = false;
    private Bitmap uriBitmap = null;

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 10010001;


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
        bitmapImage = (Bitmap) getIntent().getExtras().get("Image Bitmap");
        android_id = getIntent().getStringExtra("AndroidID");
        boolean firstEntry = getIntent().getBooleanExtra("firstEntry", false);
        boolean pictureFlag = getIntent().getBooleanExtra("pictureFlag", false);
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
        Button cancelButton = findViewById(R.id.cancelButton);

        deviceLocationRequest = LocationRequest.create();
        deviceLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        deviceLocationRequest.setInterval(5000);
        deviceLocationRequest.setFastestInterval(2000);

        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                // Permission already granted
                Log.d("MainActivity", "Notification permission already granted.");
            }
        }

        if (firstEntry) {
            cancelButton.setVisibility(View.GONE);
        }
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(editUserActivity.this, userProfileActivity.class);
            intent.putExtra("AndroidID", android_id);
            startActivity(intent);
        });
        db = FirebaseFirestore.getInstance();
        if (android_id != null) {
            populateFields(android_id);
        }
        uploadButton.setOnClickListener(v -> openImagePicker());

        removeButton.setOnClickListener(v -> {
            profileImageView.setImageResource(R.drawable.default_profile);  // Reset to default image
            imageUri = null;
            defaultFlag = true;
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
            }
            else {
                // Generate profile picture if needed
                if (imageUri == null && !cameraFlag) {
                    String firstLetter = String.valueOf(name.charAt(0)).toUpperCase();
                    if (defaultFlag) {
                        Bitmap profilePicture = generateProfilePicture(firstLetter);
                        uriBitmap = profilePicture;
                        profileImageView.setImageBitmap(profilePicture);
                    } else {
                        if(pictureFlag){
                            generatedPicture = true;
                        }
                        uriBitmap = bitmapImage;
                        profileImageView.setImageBitmap(bitmapImage);
                    }
                }

                // Submit data after location is obtained
                sendEntrantData(android_id, name, phoneNumber, email, deviceLatitude, deviceLongitude);
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("MainActivity", "Notification permission granted.");
            } else {
                // Permission denied
                Log.d("MainActivity", "Notification permission denied.");
                // Optionally, inform the user
                Toast.makeText(this, "Notification permission is required for event updates.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openImagePicker() {
        ImagePicker.with(this)
                .crop()
                .maxResultSize(1080, 1080)
                .start();
    }

    /**
     * Handle the result from ImagePicker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            // Retrieve the URI of the selected image
            imageUri = data.getData();
            try {
                uriBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            profileImageView.setImageURI(imageUri);
        }
    }

    /**
     * Generates a profile picture with the user's first name initial and a random background color.
     *
     * @param firstLetter The first letter of the user's name.
     * @return A Bitmap of the generated profile picture.
     */
    private Bitmap generateProfilePicture(String firstLetter) {
        int imageSize = 150;  // 150x150
        generatedPicture = true;
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
    private void sendEntrantData(String id,String name, String phone, String email, double latitude, double longitude){
        Bitmap profilePicture = getBitmapFromImageView(profileImageView);
        if(uriBitmap != null){
            profilePicture = uriBitmap;
        }
        String path;
        if(generatedPicture){
            path = "generated_pictures/";
        } else {
            path = "profile_images/";
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(path + android_id + ".jpg");
        // Convert Bitmap to ByteArray
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profilePicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        profilePictureByte = baos.toByteArray();
        storageRef.putBytes(profilePictureByte)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    db.collection("AndroidID").document(android_id).get().addOnSuccessListener(findURL ->{
                        String oldURL = findURL.getString("profilePictureUrl");
                        FirebaseStorage storage2 = FirebaseStorage.getInstance();

                        // Create Entrant object with the download URL
                        Entrant user = new Entrant(id, name, phone, email, downloadUrl, false, facilityID, latitude, longitude);
                        StorageReference imageRefNew = storage2.getReferenceFromUrl(downloadUrl);
                        if(oldURL!= null) {
                            StorageReference imageRefOld = storage2.getReferenceFromUrl(oldURL);
                            if (!imageRefNew.equals(imageRefOld)) {
                                imageRefOld.delete();
                            }
                        }
                        user.setGeneratedPicture(generatedPicture);
                        // Save Entrant data to Firestore
                        db.collection("AndroidID").document(android_id).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully saved data to Firestore
                                    Intent intent = new Intent(editUserActivity.this, userProfileActivity.class);
                                    intent.putExtra("AndroidID", android_id);
                                    startActivity(intent);
                                });
                    });
                }));
    }

    /**
     * Populates the Text Boxes with the user's data retrieved from the Database.
     *
     * @param android_id The user's unique device ID.
     */
    private void populateFields(String android_id){
        db.collection("AndroidID").document(android_id).get()
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
        db.collection("AndroidID").document(android_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean generated = documentSnapshot.getBoolean("generatedPicture");
                        String path = generated ? "generated_pictures/" : "profile_images/";
                        StorageReference storageRef = storage.getReference().child(path + android_id + ".jpg");

                        long size = 1024 * 1024;
                        storageRef.getBytes(size)
                                .addOnSuccessListener(bytes -> {
                                    // Convert the byte array to a Bitmap
                                    defaultFlag = false;
                                    bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    profileImageView.setImageBitmap(bitmapImage);
                                });
                    }
                });

    }

}