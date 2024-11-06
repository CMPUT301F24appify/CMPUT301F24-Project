package com.example.appify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.provider.Settings.Secure;

import com.example.appify.Activities.EntrantHomePageActivity;
import com.example.appify.Activities.editUserActivity;
import com.example.appify.Activities.userProfileActivity;
import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Main activity that initiates the application and directs users based on their profile status.
 * Displays an animated logo on startup, retrieves the device's unique Android ID,
 * and checks if the user profile exists in Firestore.
 * Navigates to either editUserActivity (for new users) or userProfileActivity (for existing users).
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String android_id;

    //private static final String TAG = "LotteryTestActivity";
   // private static final String TEST_EVENT_ID = "8b0f2eb9-e96f-48ee-84c0-8002a676f5ca";

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /**
         * Called when the activity is first created. Sets up UI, retrieves the Android ID,
         * initializes Firebase, and starts the startup animation.
         *
         * @param savedInstanceState Saved instance data for restoring state if applicable.
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the unique Android ID
        android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        Log.d("MainActivity", "Android ID: " + android_id);

        // Set Android ID in the global application class
        MyApp app = (MyApp) getApplication();
        app.setAndroidId(android_id);

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

      //  runLotteryTest(db);
      //  runAcceptStatusTest(db);

    }

    /**
     * Initiates an animated sequence for the Appify logo components on the main screen.
     * Once the animation completes, checks user status via {@link #checkAndNavigate(String)}.
     */
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

        // Animate the bottom bar of the Appify Logo
        View bottomBar = findViewById(R.id.bottom_bar);
        float bottomBar_endY = bottomBar.getTranslationY();
        bottomBar.setTranslationY(+1000f);
        ObjectAnimator tY_bottomBar = ObjectAnimator.ofFloat(bottomBar,
                View.TRANSLATION_Y,
                bottomBar.getTranslationY(),
                bottomBar_endY);
        tY_bottomBar.setDuration(2000);

        // Animate the APP text of the Appify Logo
        View appText = findViewById(R.id.app_text);
        float appText_endX = appText.getTranslationX();
        appText.setTranslationX(appText_endX - 500f);
        ObjectAnimator tX_appText = ObjectAnimator.ofFloat(appText,
                View.TRANSLATION_X,
                appText.getTranslationX(),
                appText_endX);
        tX_appText.setDuration(2000);

        // Animate the IFY text of the Appify Logo
        View ifyText = findViewById(R.id.ify_text);
        float ifyText_endX = ifyText.getTranslationX();
        ifyText.setTranslationX(ifyText_endX + 500f);
        ObjectAnimator tX_ifyText = ObjectAnimator.ofFloat(ifyText,
                View.TRANSLATION_X,
                ifyText.getTranslationX(),
                ifyText_endX);
        tX_ifyText.setDuration(2000);

        // Combine all animations
        AnimatorSet firstSet = new AnimatorSet();
        firstSet.playTogether(tY_topBar, tY_bottomBar, tX_appText, tX_ifyText);

        // Listener to handle actions after animation ends
        firstSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                checkAndNavigate(android_id);
            }
        });

        // Start the animation
        firstSet.start();
    }
    //The 2 commented functions below are from chatGPT, "Write test for (lottery/accept) from
    //MainActivity", 2024-11-05
    //Function to run the lottery from the main activity for a specific even. Test/admin tool
    //public void runLotteryTest(FirebaseFirestore db) {
        // Retrieve the event document from Firestore by TEST_EVENT_ID
        //db.collection("events").document(TEST_EVENT_ID)
         //       .get()
         //       .addOnSuccessListener(documentSnapshot -> {
         //           if (documentSnapshot.exists()) {
         //               // Retrieve event details to create an Event instance
         //               String name = documentSnapshot.getString("name");
         //               String date = documentSnapshot.getString("date");
         //               String registrationEndDate = documentSnapshot.getString("registrationEndDate");
         //               String description = documentSnapshot.getString("description");
         //               String facility = documentSnapshot.getString("facility");
         //               int maxWishEntrants = documentSnapshot.getLong("maxWishEntrants").intValue();
         //               int maxSampleEntrants = documentSnapshot.getLong("maxSampleEntrants").intValue();
         //               String posterUri = documentSnapshot.getString("posterUri");
          //              boolean isGeolocate = documentSnapshot.getBoolean("isGeolocate") != null
          //                      && documentSnapshot.getBoolean("isGeolocate");
//
//                        // Create the Event object
//                        Event testEvent = new Event(name, date, registrationEndDate, description, facility,
 //                               maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate);

                        // Run the lottery function for the event
 //                       testEvent.lottery(db, TEST_EVENT_ID);

                        // Log to confirm the test execution
  //                      Log.d(TAG, "Lottery test run completed for event ID: " + TEST_EVENT_ID);
  //                  } else {
  //                      Log.w(TAG, "No event found with ID: " + TEST_EVENT_ID);
   //                 }
    //            })
    //            .addOnFailureListener(e -> {
    //                Log.e(TAG, "Error retrieving event with ID: " + TEST_EVENT_ID, e);
    //            });
   // }



//Function to run the accept function from the main activity for a specific event for all users in it.
// Test/admin tool
  //  public void runAcceptStatusTest(FirebaseFirestore db) {
  //      db.collection("events").document(TEST_EVENT_ID)
  //              .collection("waitingList")
  //              .whereEqualTo("status", "invited") // Filter for entrants with "invited" status
  //              .get()
  //              .addOnSuccessListener(querySnapshot -> {
  //                  for (QueryDocumentSnapshot document : querySnapshot) {
   //                     // Retrieve entrant ID and other necessary fields
   //                     String entrantId = document.getId();
   //                     String name = document.getString("name");
   //                     String phoneNumber = document.getString("phoneNumber");
    //                    String email = document.getString("email");
    //                    String profilePictureUrl = document.getString("profilePictureUrl");
    //                    boolean notifications = document.getBoolean("notifications") != null && document.getBoolean("notifications");
//
//                        // Create an Entrant object
 //                       Entrant entrant = new Entrant(entrantId, name, phoneNumber, email, profilePictureUrl, notifications);
//
 //                       // Call acceptEvent for each invited entrant
 //                       entrant.acceptEvent(db, TEST_EVENT_ID);
 //                   }
//
//                    Log.d(TAG, "AcceptStatusTest: All invited entrants have been accepted for event ID: " + TEST_EVENT_ID);
 //               })
 //               .addOnFailureListener(e -> {
  //                  Log.e(TAG, "Error retrieving invited entrants for event ID: " + TEST_EVENT_ID, e);
  //              });
  //  }




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
                            // Existing user, navigate to HomePage
                            Log.d("MainActivity", "User exists. Navigating to userProfileActivity.");
                            navigateToHomePage(androidId);
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
     * Navigates the user to EntrantHomePageActivity to view their Home Page.
     *
     * @param androidId The unique Android ID of the device/user.
     */
    private void navigateToHomePage(String androidId) {
        Intent intent = new Intent(MainActivity.this, EntrantHomePageActivity.class);
        intent.putExtra("Android ID", androidId);
        startActivity(intent);
        finish(); // Prevent user from returning to MainActivity
    }
}