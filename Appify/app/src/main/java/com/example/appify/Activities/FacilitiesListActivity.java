package com.example.appify.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Adapters.CustomFacilityAdapter;
import com.example.appify.HeaderNavigation;
import com.example.appify.Model.Facility;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Displays a list of facilities.
 */
public class FacilitiesListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ListView facilitiesListView;
    private CustomFacilityAdapter facilityAdapter;
    private ArrayList<Facility> facilityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facilities);

        db = FirebaseFirestore.getInstance();

        // Initialize the ListView and the Adapter
        facilityList = new ArrayList<>();
        facilityAdapter = new CustomFacilityAdapter(this, facilityList);
        facilitiesListView = findViewById(R.id.facilities_list);
        facilitiesListView.setAdapter(facilityAdapter);

        // HeaderNavigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();
        TextView facilitiesText = findViewById(R.id.facilitiesText_navBar);
        facilitiesText.setTextColor(Color.parseColor("#800080"));
        facilitiesText.setTypeface(facilitiesText.getTypeface(), Typeface.BOLD);

        // Load Facilities from Firestore
        loadFacilitiesFromFirestore();
    }

    private void loadFacilitiesFromFirestore() {
        CollectionReference facilitiesRef = db.collection("facilities");

        facilitiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                facilityList.clear(); // Clear the list to avoid duplicates
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String id = doc.getId();
                    String name = doc.getString("name");
                    String location = doc.getString("location");
                    String email = doc.getString("email");
                    String description = doc.getString("description");
                    int capacity = doc.getLong("capacity").intValue();
                    String organizerID = doc.getString("organizerID");

                    Facility facility = new Facility(id, name, location, email, description, capacity, organizerID);
                    facilityList.add(facility);
                }
                facilityAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error loading facilities.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
