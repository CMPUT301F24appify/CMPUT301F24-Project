// MainActivity.java
package com.example.appify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appify.Activities.EntrantHomePageActivity;
import com.example.appify.Activities.editUserActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Main activity that initiates the application and directs users based on their profile status.
 * Displays an animated logo on startup, retrieves the device's unique AndroidID,
 * and checks if the user profile exists in Firestore.
 * Navigates to either editUserActivity (for new users) or EntrantHomePageActivity (for existing users).
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String android_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Retrieve the unique AndroidID
        android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        // Set AndroidID in the global application class
        MyApp app = (MyApp) getApplication();
        app.setAndroidId(android_id);
        db = app.getFirebaseInstance();
        // Set db in the global application class

//        M firebase = (Firebase) getApplication();
//        firebase.setFirebaseInstance(db);

        // Adjust padding based on system bars (optional, depending on your UI design)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Start the startup animation
        startUpAnimation();
    }



    /**
     * Initiates an animated sequence for the Appify logo components on the main screen.
     * Once the animation completes, checks user status via {@link #checkAndNavigate(String)}.
     */
    private void startUpAnimation(){

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
     * @param androidId The unique AndroidID of the device/user.
     */
    private void checkAndNavigate(String androidId) {
        Log.d("MainActivity", "Checking user status for AndroidID: " + androidId);

        // Updated collection name to "AndroidID" without spaces
        db.collection("AndroidID").document(androidId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Existing user, navigate to HomePage
                            navigateToHomePage(androidId);
                        } else {
                            // New user, navigate to editUserActivity
                            navigateToEditUser(androidId);
                        }
                    } else {
                        // Handle the error
                        Toast.makeText(MainActivity.this, "Error checking user status. Please try again.", Toast.LENGTH_SHORT).show();

                        // Log additional information
                        if (task.getException() != null) {
                            Log.e("MainActivity", "Firestore Exception: ", task.getException());
                        }
                    }
                });
    }

    /**
     * Navigates the user to editUserActivity to set up their profile.
     *
     * @param androidId The unique AndroidID of the device/user.
     */
    private void navigateToEditUser(String androidId) {
        Intent intent = new Intent(MainActivity.this, editUserActivity.class);
        intent.putExtra("AndroidID", androidId); // Updated key to "AndroidID" without spaces
        intent.putExtra("firstEntry", true);
        startActivity(intent);
        finish(); // Prevent user from returning to MainActivity
    }

    /**
     * Navigates the user to EntrantHomePageActivity to view their Home Page.
     *
     * @param androidId The unique AndroidID of the device/user.
     */
    private void navigateToHomePage(String androidId) {
        Intent intent = new Intent(MainActivity.this, EntrantHomePageActivity.class);
        intent.putExtra("AndroidID", androidId); // Updated key to "AndroidID" without spaces
        startActivity(intent);
        finish(); // Prevent user from returning to MainActivity
    }
}
