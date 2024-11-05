package com.example.appify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.provider.Settings.Secure;

import com.google.firebase.firestore.CollectionReference;
import com.example.appify.Activities.editUserActivity;
import com.example.appify.Activities.userProfileActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String android_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Retrieve the unique Android ID
        android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        Log.d("MainActivity", "Android ID: " + android_id);

        // Adjust padding based on system bars (optional, depending on your UI design)
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Start the startup animation
        startUpAnimation();
    }

    void startUpAnimation(){

        // Animate the top bar of the Appify Logo
        View topBar = findViewById(R.id.top_bar);
        float topBar_endY = topBar.getTranslationY();
        topBar.setTranslationY(-1000f);
        ObjectAnimator tY_topBar = ObjectAnimator.ofFloat(topBar,
                View.TRANSLATION_Y,
                topBar.getTranslationY(),
                topBar_endY);
        tY_topBar.setDuration(2000);
        topBar.setTranslationY(topBar_endY);

        // Animate the bottom bar of the Appify Logo
        View bottomBar = findViewById(R.id.bottom_bar);
        float bottomBar_endY = bottomBar.getTranslationY();
        bottomBar.setTranslationY(+1000f);
        ObjectAnimator tY_bottomBar = ObjectAnimator.ofFloat(bottomBar,
                View.TRANSLATION_Y,
                bottomBar.getTranslationY(),
                bottomBar_endY);
        tY_bottomBar.setDuration(2000);
        bottomBar.setTranslationY(bottomBar_endY);

        // Animate the APP text of the Appify Logo
        View appText = findViewById(R.id.app_text);
        float appText_endX = appText.getTranslationX();
        appText.setTranslationX(appText_endX - 500f);
        ObjectAnimator tX_appText = ObjectAnimator.ofFloat(appText,
                View.TRANSLATION_X,
                appText.getTranslationX(),
                appText_endX);
        tX_appText.setDuration(2000);
        appText.setTranslationX(appText_endX);

        // Animate the IFY text of the Appify Logo
        View ifyText = findViewById(R.id.ify_text);
        float ifyText_endX = ifyText.getTranslationX();
        ifyText.setTranslationX(ifyText_endX + 500f);
        ObjectAnimator tX_ifyText = ObjectAnimator.ofFloat(ifyText,
                View.TRANSLATION_X,
                ifyText.getTranslationX(),
                ifyText_endX);
        tX_ifyText.setDuration(2000);
        ifyText.setTranslationX(ifyText_endX);

        // Combine all animations
        AnimatorSet firstSet = new AnimatorSet();
        firstSet.playTogether(tY_topBar, tY_bottomBar, tX_appText, tX_ifyText);

        // When the animation ends, switch the view after a short delay.
        firstSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                checkAndNavigate(android_id);
            }
        });

        // Start the animation
        firstSet.start();
    }

    /**
     * Checks if the user exists in Firestore and navigates accordingly.
     *
     * @param androidId The unique Android ID of the device/user.
     */
    private void checkAndNavigate(String androidId) {
        db.collection("Android ID").document(androidId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            // New user, navigate to editUserActivity
                            Log.d("MainActivity", "User is new. Navigating to editUserActivity.");
                            navigateToEditUser(androidId);
                        } else {
                            // Existing user, navigate to userProfileActivity
                            Log.d("MainActivity", "User exists. Navigating to userProfileActivity.");
                            navigateToUserProfile(androidId);
                        }
                    } else {
                        // Handle the error
                        Log.w("MainActivity", "Error checking for Android ID", task.getException());
                        Toast.makeText(MainActivity.this, "Error checking user status. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Navigates the user to editUserActivity to set up their profile.
     *
     * @param androidId The unique Android ID of the device/user.
     */
    private void navigateToEditUser(String androidId) {
        Intent intent = new Intent(MainActivity.this, editUserActivity.class);
        intent.putExtra("Android ID", androidId);
        startActivity(intent);
        finish(); // Prevent user from returning to MainActivity
    }

    /**
     * Navigates the user to userProfileActivity to view their profile.
     *
     * @param androidId The unique Android ID of the device/user.
     */
    private void navigateToUserProfile(String androidId) {
        Intent intent = new Intent(MainActivity.this, userProfileActivity.class);
        intent.putExtra("Android ID", androidId);
        startActivity(intent);
        finish(); // Prevent user from returning to MainActivity
    }
}
