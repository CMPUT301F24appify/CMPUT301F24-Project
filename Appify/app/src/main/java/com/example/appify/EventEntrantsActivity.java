package com.example.appify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EventEntrantsActivity extends AppCompatActivity{
    private FirebaseFirestore db;
    ListView entrantListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_entrants);

        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");


    }
}
