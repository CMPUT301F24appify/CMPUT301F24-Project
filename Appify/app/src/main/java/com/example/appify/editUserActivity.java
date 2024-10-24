package com.example.appify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.shape.CornerFamily;

public class editUserActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
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

        // Fetch the EditText fields
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText phoneEditText = findViewById(R.id.phoneEditText);
        EditText emailEditText = findViewById(R.id.emailEditText);
        profileImageView = findViewById(R.id.profileImageView);
        Button uploadButton = findViewById(R.id.uploadButton);

        // test
        String userName = "John Doe";
        String userPhone = "123 456 7890";
        String userEmail = "John@gmail.com";

        // set test info
        nameEditText.setText(userName);
        phoneEditText.setText(userPhone);
        emailEditText.setText(userEmail);

        uploadButton.setOnClickListener(v -> openFileChooser());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);  // Launch the file picker with the new ActivityResultLauncher
    }
}
