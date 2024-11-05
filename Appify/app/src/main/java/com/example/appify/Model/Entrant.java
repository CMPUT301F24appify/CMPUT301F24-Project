package com.example.appify.Model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Represents an entrant with personal details and preferences.
 * An entrant can have various attributes such as name, contact information,
 * profile picture, notification preferences, and a list of events they are associated with.
 */
public class Entrant {
    // Attributes
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private boolean notifications;
    private List<String> eventList;


    /**
     * Constructs an Entrant with the specified attributes.
     *
     * @param id                the unique ID of the entrant
     * @param name              the name of the entrant
     * @param phoneNumber       the phone number of the entrant
     * @param email             the email address of the entrant
     * @param profilePicture    the URL of the entrant's profile picture
     * @param notifications     the notification preference of the entrant
     */
    public Entrant(String id, String name, String phoneNumber, String email, String profilePicture, boolean notifications) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.profilePictureUrl = profilePicture;
        this.notifications = notifications;
    }

    // Getters

    /**
     * Gets the unique ID of the entrant.
     *
     * @return the entrant's ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name of the entrant.
     *
     * @return the entrant's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the phone number of the entrant.
     *
     * @return the entrant's phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number of the entrant.
     *
     * @param phoneNumber the entrant's new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the email address of the entrant.
     *
     * @return the entrant's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the URL of the entrant's profile picture.
     *
     * @return the URL of the profile picture
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * Sets the URL of the entrant's profile picture.
     *
     * @param profilePictureUrl the new URL of the profile picture
     */
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * Checks whether notifications are enabled for the entrant.
     *
     * @return true if notifications are enabled, false otherwise
     */
    public boolean isNotifications() {
        return notifications;
    }

    /**
     * Sets the notification preference for the entrant.
     *
     * @param notifications true to enable notifications, false to disable
     */
    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    /**
     * Gets the list of event IDs associated with the entrant.
     *
     * @return a list of event IDs
     */
    public List<String> getEventList() {
        return eventList;
    }

    /**
     * Sets the list of event IDs associated with the entrant.
     *
     * @param eventList a list of event IDs
     */
    public void setEventList(List<String> eventList) {
        this.eventList = eventList;
    }
}

