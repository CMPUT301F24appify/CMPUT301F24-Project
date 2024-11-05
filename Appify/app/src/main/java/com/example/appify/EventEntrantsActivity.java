package com.example.appify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

// This activity is the page that displays the entrants that have enrolled for an event, and what their enrollment status is.
public class EventEntrantsActivity extends AppCompatActivity{
    private FirebaseFirestore db;
    ListView entrantListView;
    CustomEntrantAdapter entrantAdapter;
    CollectionReference waitingListRef;
    ArrayList<Entrant> entrantList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_entrants);

        // Retrieve the eventID from the previous activity
        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");

        db = FirebaseFirestore.getInstance();

        entrantListView = findViewById(R.id.entrant_list);

        // Get the waiting list details for the current event
        waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Setup the adapter AFTER firebase retrieves all the data, by checking when all tasks are complete
                int[] tasksCompleted = {0};
                int totalTasks = task.getResult().size();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                    // For each user in the waiting list, get their details from the "Android ID" collection
                    String userID = document.getId();
                    Object waitingListStatus = document.get("status");

                    // Access each entrant in the waiting list for this event.
                    db.collection("Android ID").document(userID).get()
                            .addOnCompleteListener(task2 ->{
                                if(task2.isSuccessful() && task2.getResult() != null){
                                    DocumentSnapshot entrantData = task2.getResult();
                                    String entrantID = userID;
                                    String entrantName = entrantData.get("name").toString();
                                    String entrantEmail = entrantData.get("email").toString();
                                    String entrantWaitingListStatus = waitingListStatus.toString();
                                    Entrant entrant = new Entrant(entrantID, entrantName, entrantEmail, entrantWaitingListStatus);
                                    entrantList.add(entrant);
                                }
                                else {
                                    System.out.println("Error getting AndroidID document for " + userID + ": " + task2.getException());
                                }
                                tasksCompleted[0]++;

                                if (tasksCompleted[0] == totalTasks){
                                    // All tasks complete, set up adapter
                                    System.out.println("Outside test: " + entrantList);
                                    entrantAdapter = new CustomEntrantAdapter(this,entrantList);
                                    entrantListView.setAdapter(entrantAdapter);
                                }
                            });
                }

                }

            else {
                System.out.println("Error getting documents: " + task.getException());
            }
        });


    }
}
