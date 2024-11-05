package com.example.appify;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.appify.Activities.EntrantHomePageActivity;

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

}
