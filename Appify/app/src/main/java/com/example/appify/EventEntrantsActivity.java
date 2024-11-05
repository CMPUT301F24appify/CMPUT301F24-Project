package com.example.appify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class EventEntrantsActivity extends AppCompatActivity{
    private FirebaseFirestore db;
    ListView entrantListView;
    ArrayList<Entrant> entrantList;
    CustomEntrantAdapter entrantAdapter;
    CollectionReference waitingListRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_entrants);

        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");

        db = FirebaseFirestore.getInstance();

        entrantListView = findViewById(R.id.entrant_list);
        ArrayList<String> userIDs = new ArrayList<>();
        ArrayList<Map<String, Object>> waitingListStatuses = new ArrayList<>();
        waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Access each document in the waitingList
                    String userID = document.getId();
                    Object waitingListStatus = document.getData();

                    db.collection("Android ID").document(userID).get()
                                    .addOnCompleteListener(task2 ->{
                                        if(task2.isSuccessful() && task2.getResult() != null){
                                            DocumentSnapshot entrantData = task2.getResult();
                                            System.out.println("AndroidID Data for " + userID + ": " + entrantData.get("name"));
                                            userIDs.add(userID);
                                        }
                                        else {
                                            System.out.println("Error getting AndroidID document for " + userID + ": " + task2.getException());
                                        }
                                    });

                    userIDs.add(userID);
                    waitingListStatuses.add(document.getData());
                }
            } else {
                System.out.println("Error getting documents: " + task.getException());
            }
        });

        // Sample entrants
        Entrant entrant1 = new Entrant("1","Bob","bob@gmail.com","Enrolled");
        Entrant entrant2 = new Entrant("2","John","john@gmail.com","Invited");
        Entrant entrant3 = new Entrant("3","Nathan","nhlin@ualberta.ca","Accepted");
        Entrant entrant4 = new Entrant("4","Trueman","ntrueman@ualberta.ca","Rejected");

        Entrant []entrants = {entrant1,entrant2,entrant3,entrant4};
        entrantList = new ArrayList<>();
        entrantList.addAll(Arrays.asList(entrants));

        entrantAdapter = new CustomEntrantAdapter(this,entrantList);
        entrantListView.setAdapter(entrantAdapter);

    }
}
