package com.example.appify;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

public class acceptSection {
    private FirebaseFirestore db;
    private Context currentUser;
    private Context userEvents;

    //registered events is an array of the events the user registered for.
    //user is user
    //Replace with appropriate classes during integration
    public acceptSection(Context registeredEvents, Context user) {
        currentUser = user;
        userEvents = registeredEvents;
        // Initialize Firestore (commented out for now, as requested)
        // db = FirebaseFirestore.getInstance();

        // Temporary feedback for testing without Firebase
        //Toast.makeText(context, "acceptSection initialized", Toast.LENGTH_SHORT).show();
    }

    public void acceptEvent(Context chosenEvent) {
        //Mark the event as joined. Check the exact method again.
        //add to firebase
    }
    public void declineEvent(Context chosenEvent) {
        //remove the event
    }
}
