package com.example.appify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList, event -> {
            // Handle event click by launching EventDetailsActivity
            Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);
            intent.putExtra("eventID", event.getId());  // Pass eventID to the next activity
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        CollectionReference eventsRef = db.collection("events");

        // Fetch all events from Firestore
        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getId();
                    String name = document.getString("name");
                    String startDate = document.getString("startDate");
                    String endDate = document.getString("endDate");
                    Event event = new Event(id, name, startDate, endDate);
                    eventList.add(event);
                }
                adapter.notifyDataSetChanged();  // Notify adapter about the new data
            } else {
                Log.w("Firestore", "Error getting events.", task.getException());
            }
        });
    }
}
