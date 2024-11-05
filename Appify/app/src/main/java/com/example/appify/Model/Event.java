package com.example.appify.Model;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Random;

public class Event {
    private String name;
    private String date;
    private String registrationEndDate;
    private String description;
    private String facility;
    private int maxWishEntrants;
    private int maxSampleEntrants;
    private String posterUri;  // Store URI as String
    private boolean isGeolocate;
    private String eventId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private String organizerID;


    public Event(String name, String date, String registrationEndDate, String description, String facility, int maxWishEntrants,
                 int maxSampleEntrants, String posterUri, boolean isGeolocate) {
        this.name = name;
        this.date = date;
        this.registrationEndDate = registrationEndDate;
        this.description = description;
        this.facility = facility;
        this.maxWishEntrants = maxWishEntrants;
        this.maxSampleEntrants = maxSampleEntrants;
        this.posterUri = posterUri;
        this.isGeolocate = isGeolocate;
        this.eventId = UUID.randomUUID().toString();
    }

    public static Event fromFirestore(QueryDocumentSnapshot document) {
        String name = document.getString("name");
        String date = document.getString("date");
        String registrationEndDate = document.getString("registrationEndDate");
        String description = document.getString("description");
        String facility = document.getString("facility");
        int maxWishEntrants = document.getLong("maxWishEntrants").intValue();
        int maxSampleEntrants = document.getLong("maxSampleEntrants").intValue();
        String posterUri = document.getString("posterUri");
        boolean isGeolocate = document.getBoolean("isGeolocate") != null ? document.getBoolean("isGeolocate") : false;

        Event event = new Event(name, date, registrationEndDate, description, facility, maxWishEntrants, maxSampleEntrants, posterUri, isGeolocate);
        event.eventId = document.getId(); // Use Firestore ID if available

        return event;
    }
    
    public ArrayList<Entrant> getWaitingList() {
        ArrayList<Entrant> waitingList = new ArrayList<>();

        db.collection("events").document(this.eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getString("id");
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String status = document.getString("status");

                        Entrant entrant = new Entrant(id, name, email, status);
                        waitingList.add(entrant);
                    }
                    Log.d("Event", "Waiting list retrieved successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Event", "Error retrieving waiting list", e);
                });

        return waitingList;
    }

    public void lottery(FirebaseFirestore db, String eventID) {
        int chosenPeople = 0;
        ArrayList<Entrant> waitingListUpdate = this.getWaitingList();
        Random chance = new Random();

        while (chosenPeople < maxWishEntrants) {
            for (Entrant entrant : waitingListUpdate) {
                if ("enrolled".equals(entrant.getStatus())) {
                    int chanceChoose = chance.nextInt(2); // 50% chance
                    if (chanceChoose == 1) {
                        // Person is chosen, update their status to "invited" directly in Firestore
                        db.collection("events").document(eventID)
                                .collection("waitingList").document(entrant.getId())
                                .update("status", "invited")
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Lottery", "Entrant " + entrant.getId() + " invited successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("Lottery", "Error inviting entrant " + entrant.getId(), e);
                                });

                        chosenPeople++;
                        if (chosenPeople >= maxWishEntrants) {
                            return; // Exit the method once the max number of entrants is invited
                        }
                    }
                }
            }
        }
    }


    // Getters
    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getRegistrationEndDate() { return registrationEndDate; }

    public String getFacility() { return facility; }

    public String getDescription() {
        return description;
    }

    public int getMaxWishEntrants() {
        return maxWishEntrants;
    }

    public int getMaxSampleEntrants() {
        return maxSampleEntrants;
    }

    public String getPosterUri() {
        return posterUri;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public String getEventId() {
        return eventId;
    }

    public boolean isGeolocate() {
        return isGeolocate;
    }

    // Override the toString() method to display the event name in the ListView
    @Override
    public String toString() {
        return name;
    }


}
