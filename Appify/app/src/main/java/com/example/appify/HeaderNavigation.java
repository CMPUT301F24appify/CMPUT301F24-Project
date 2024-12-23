package com.example.appify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.appify.Activities.EntrantHomePageActivity;
import com.example.appify.Activities.EventActivity;
import com.example.appify.Activities.AdminListActivity;
import com.example.appify.Activities.userProfileActivity;
import com.example.appify.Fragments.AddFacilityDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The HeaderNavigation class handles navigation within the application by setting up listeners for
 * various header components, including events, facilities, organizing, notifications, home, and profile.
 * It also checks if a user has a facility (organizer status) and prompts them to create one if not.
 */
public class HeaderNavigation {
    // Attributes
    private Activity activity;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    /**
     * Constructor for HeaderNavigation.
     *
     * @param activity The activity where the header navigation is used.
     */
    public HeaderNavigation(Activity activity) {
        this.activity = activity;
    }

    /**
     * Initializes navigation listeners for various header components.
     */
    public void setupNavigation() {
        // Events
        TextView eventsText = activity.findViewById(R.id.eventsText_navBar);
        if (eventsText != null) {
            eventsText.setOnClickListener(v -> navigateToEvents());
        }

        // Admin
        TextView adminText = activity.findViewById(R.id.adminText_navBar);

        // Check if user is admin or not

        MyApp app = (MyApp) activity.getApplication();
        String currentUserID = app.getAndroidId();

        db.collection("AndroidID").document(currentUserID).get().addOnSuccessListener(documentSnapshot->{
            if (documentSnapshot.exists()){
                boolean isAdmin = documentSnapshot.getBoolean("admin");
                if (adminText != null && isAdmin) {
                    adminText.setVisibility(View.VISIBLE);
                    adminText.setOnClickListener(v -> checkAdminStatus());
                }
            }

        });

        // Organize
        TextView organizeText = activity.findViewById(R.id.organizeText_navBar);
        if (organizeText != null) {
            organizeText.setOnClickListener(v -> {
                checkOrganizerStatus();
            });
        }

        // Entrant Home
        ImageView appifyLogo = activity.findViewById(R.id.logo);
        if (appifyLogo != null) {
            appifyLogo.setOnClickListener(v -> navigateToEvents());
        }

        // Profile Picture
        ImageView profilePicture = activity.findViewById(R.id.profileImageViewHeader);
        if (profilePicture != null) {
            profilePicture.setOnClickListener(v -> navigateToProfile());
            loadProfilePicture(profilePicture);
        }

    }

    /**
     * Navigates to the entrant events page.
     */
    private void navigateToEvents() {
        Intent intent = new Intent(activity, EntrantHomePageActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Navigates to the facilities management page.
     */
    private void navigateToFacilities() {
        Intent intent = new Intent(activity, AdminListActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Navigates to the event management page.
     */
    public void navigateToOrganize() {
        Intent intent = new Intent(activity, EventActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Navigates to the user's profile page.
     */
    private void navigateToProfile() {
        Intent intent = new Intent(activity, userProfileActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Loads the user's profile picture from Firebase Storage into the provided ImageView.
     *
     * @param profilePicture The ImageView to display the user's profile picture.
     */
    private void loadProfilePicture(ImageView profilePicture) {
        MyApp app = (MyApp) activity.getApplication();
        String androidId = app.getAndroidId();

        if (androidId != null && !androidId.isEmpty()) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            AtomicReference<String> path = new AtomicReference<>("profile_images/");
            db.collection("AndroidID").document(androidId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            boolean generated = documentSnapshot.getBoolean("generatedPicture");
                            String imagePath = generated ? "generated_pictures/" : "profile_images/";
                            StorageReference storageRef = storage.getReference().child(imagePath + androidId + ".jpg");

                            long size = 1024 * 1024; // Adjust the size as needed
                            storageRef.getBytes(size)
                                    .addOnSuccessListener(bytes -> {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        profilePicture.setImageBitmap(bitmap);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Optionally set a default image if loading fails
                                        profilePicture.setImageResource(R.drawable.default_pfp);
                                    });
                        }
                    });


        }
    }

    /**
     * Checks if the user is an organizer by querying the database.
     * If the user is an organizer, navigates to EventActivity.
     * If the user is not an organizer, shows the Add Facility dialog.
     */
    private void checkOrganizerStatus() {
        MyApp app = (MyApp) activity.getApplication();
        String androidId = app.getAndroidId();

        if (androidId != null) {
            db.collection("AndroidID").document(androidId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.getString("facilityID") != null) {
                            // User is an organizer, navigate to EventActivity
                            navigateToOrganize();
                        } else {
                            // User is not an organizer, show Add Facility dialog
                            showAddFacilityDialog();
                        }
                    });
        }
    }

    /**
     * Checks if the user has admin privileges.
     * Navigates to the facilities page if the user is an admin; otherwise, shows a toast message.
     */
    private void checkAdminStatus() {
        MyApp app = (MyApp) activity.getApplication();
        String androidId = app.getAndroidId();

        if (androidId != null) {
            db.collection("AndroidID").document(androidId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && Boolean.TRUE.equals(documentSnapshot.getBoolean("admin"))) {
                            navigateToFacilities();
                        } else {
                            Toast.makeText(activity, "You are not an admin.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(activity, "Error checking admin status.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Displays the Add Facility dialog, allowing the user to add a new facility.
     * This method checks if the current activity is a FragmentActivity to ensure fragment management compatibility.
     * If the condition is met, it creates an instance of the AddFacilityDialogFragment and displays it.
     */
    private void showAddFacilityDialog() {
        // Check if the activity is a FragmentActivity, required for managing fragments
        if (activity instanceof androidx.fragment.app.FragmentActivity) {
            androidx.fragment.app.FragmentActivity fragmentActivity = (androidx.fragment.app.FragmentActivity) activity;

            // Create an instance of the AddFacilityDialogFragment
            AddFacilityDialogFragment addFacilityDialog = new AddFacilityDialogFragment();

            // Show the dialog fragment
            addFacilityDialog.show(fragmentActivity.getSupportFragmentManager(), "AddFacilityDialog");
        }
    }
}
