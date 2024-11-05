package com.example.appify;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.appify.Activities.EntrantHomePageActivity;
import com.example.appify.Activities.userProfileActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HeaderNavigation {
    private Activity activity;

    // Constructor
    public HeaderNavigation(Activity activity) {
        this.activity = activity;
    }

    // Method to initialize the navigation
    public void setupNavigation() {
        // Events
        TextView eventsText = activity.findViewById(R.id.eventsText_navBar);
        if (eventsText != null) {
            eventsText.setOnClickListener(v -> navigateToEvents());
        }

        // Facilities
        TextView facilitiesText = activity.findViewById(R.id.facilitiesText_navBar);
        if (facilitiesText != null) {
            facilitiesText.setOnClickListener(v -> navigateToFacilities());
        }

        // Organize
        TextView organizeText = activity.findViewById(R.id.organizeText_navBar);
        if (organizeText != null) {
            organizeText.setOnClickListener(v -> navigateToOrganize());
        }

        // Notifications
        ImageView notificationBell = activity.findViewById(R.id.header_notifications);
        if (notificationBell != null) {
            notificationBell.setOnClickListener(v -> navigateToNotifications());
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
        }

    }

    // Navigation methods for each destination
    private void navigateToEvents() {
        Intent intent = new Intent(activity, EntrantHomePageActivity.class);
        activity.startActivity(intent);
    }

    private void navigateToFacilities() {
//        Intent intent = new Intent(activity, FacilitiesActivity.class);
//        activity.startActivity(intent);
    }

    private void navigateToOrganize() {
//        Intent intent = new Intent(activity, OrganizeActivity.class);
//        activity.startActivity(intent);
    }

    private void navigateToNotifications() {
//        Intent intent = new Intent(activity, NotificationsActivity.class);
//        activity.startActivity(intent);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(activity, userProfileActivity.class);
        activity.startActivity(intent);
    }

}
