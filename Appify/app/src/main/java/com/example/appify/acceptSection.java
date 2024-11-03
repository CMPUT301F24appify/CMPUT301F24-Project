package com.example.appify;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
//Not needed - import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class acceptSection {
    private FirebaseFirestore db;
    private User currentUser;
    private ArrayList<Event> userEvents;


    //registered events is an array of the events the user registered for.
    //user is user
    //Replace with appropriate classes during integration
    public acceptSection(User user) {
        currentUser = user;
        userEvents = user.getEvents();
    }

    public void acceptEvent(Event chosenEvent) {
        //General logic - grab ids of event and user. Find the user in the waiting list by id, update
        ArrayList<User> = waitingListchosenEvent.getWaitingList();
    }
    public void declineEvent(Context chosenEvent) {
        //General logic - grab ids of event and user. Find the user in the waiting list by id, update
    }
}
