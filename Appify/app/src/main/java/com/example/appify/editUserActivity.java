package com.example.appify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Random;

public class editUserActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private Uri imageUri;
    private String android_id;
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);  // Set the selected image
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user);
        android_id = getIntent().getStringExtra("Android ID");
        // Fetch the EditText fields
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText phoneEditText = findViewById(R.id.phoneEditText);
        EditText emailEditText = findViewById(R.id.emailEditText);
        profileImageView = findViewById(R.id.profileImageView);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button removeButton = findViewById(R.id.removeButton);
        Button submitButton = findViewById(R.id.submitButton);

        db = FirebaseFirestore.getInstance();


        uploadButton.setOnClickListener(v -> openFileChooser());

        removeButton.setOnClickListener(v -> {
            profileImageView.setImageResource(R.drawable.default_profile);  // Reset to default image
            imageUri = null;
        });

        submitButton.setOnClickListener(v -> {
            if (imageUri == null) {
                // Generate a profile picture with the first letter of the user's name
                String name = nameEditText.getText().toString();
                String firstName = name.trim();
                String phoneNumber = phoneEditText.getText().toString();
                String email = emailEditText.getText().toString();
                sendEntrantData(android_id,name,phoneNumber,email);
                if (!firstName.isEmpty()) {
                    String firstLetter = String.valueOf(firstName.charAt(0)).toUpperCase();
                    Bitmap profilePicture = generateProfilePicture(firstLetter);
                    profileImageView.setImageBitmap(profilePicture);  // Set the generated image
                }
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
        Entrant User = new Entrant(id,name,phone,email);
        db.collection("Android ID").document(android_id).set(User);
    }
}
