package com.example.appify.Activities;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Adapters.CustomEntrantAdapter;
import com.example.appify.Model.Entrant;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;


/**
 * This class is an Android activity that displays the list of entrants
 * enrolled in a particular event, as well as their enrollment status.
 * Users can filter entrants based on their status (waiting list, invited, accepted, rejected) using checkboxes.
 */
public class EventEntrantsActivity extends AppCompatActivity{
    public FirebaseFirestore db;
    public ListView entrantListView;
    CustomEntrantAdapter entrantAdapterAll;
    CollectionReference waitingListRef;
    public ArrayList<Entrant> entrantListAll = new ArrayList<>();
    public ArrayList<Entrant> entrantListWaitinglisted = new ArrayList<>();
    ArrayList<Entrant> entrantListInvited = new ArrayList<>();
    ArrayList<Entrant> entrantListAccepted = new ArrayList<>();
    ArrayList<Entrant> entrantListRejected = new ArrayList<>();

    public CheckBox waitListedCheckbox;
    public CheckBox invitedCheckBox;
    public CheckBox acceptedCheckBox;
    public CheckBox rejectedCheckBox;

    /**
     * Called when the activity is first created.
     * Initializes the activity's views, retrieves the event ID, loads entrant data, and sets up listeners.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_entrants);

        // Retrieve the eventID from the previous activity
        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");

        MyApp app = (MyApp) getApplication();
        db = app.getFirebaseInstance();

        entrantListView = findViewById(R.id.entrant_list);
        waitListedCheckbox = findViewById(R.id.waitListed_checkbox);
        invitedCheckBox = findViewById(R.id.invited_checkbox);
        acceptedCheckBox = findViewById(R.id.accepted_checkbox);
        rejectedCheckBox = findViewById(R.id.rejected_checkbox);

        // Initialize Data to be displayed
        reloadData(eventID);

        // Add listeners to each checkbox, to display the correct data
        waitListedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                entrantListAll.addAll(entrantListWaitinglisted);
            } else {
                entrantListAll.removeAll(entrantListWaitinglisted);
            }
            entrantAdapterAll.notifyDataSetChanged();
        });

        invitedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                entrantListAll.addAll(entrantListInvited);
            } else {
                entrantListAll.removeAll(entrantListInvited);
            }
            entrantAdapterAll.notifyDataSetChanged();
        });

        acceptedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                entrantListAll.addAll(entrantListAccepted);
            } else {
                entrantListAll.removeAll(entrantListAccepted);
            }
            entrantAdapterAll.notifyDataSetChanged();
        });

        rejectedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                entrantListAll.addAll(entrantListRejected);
            } else {
                entrantListAll.removeAll(entrantListRejected);
            }
            entrantAdapterAll.notifyDataSetChanged();
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

    }

    /**
     * Reloads entrant data for the specified event by querying Firebase Firestore.
     * Retrieves and categorizes entrants based on their enrollment status (waiting list, invited, accepted, or rejected).
     * Sets up a custom adapter for displaying the entrant data.
     *
     * @param eventID The ID of the event for which to retrieve entrant data.
     */
    public void reloadData(String eventID) {

        entrantListAll.clear(); // Clear existing list

        // Get the waiting list details for the current event
        waitingListRef = db.collection("events").document(eventID).collection("waitingList");
        waitingListRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Setup the adapter AFTER firebase retrieves all the data, by checking when all tasks are complete
                int[] tasksCompleted = {0};
                int totalTasks = task.getResult().size();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // For each user in the waiting list, get their details from the "AndroidID" collection
                    String userID = document.getId();
                    Object waitingListStatus = document.get("status");

                    // Access each entrant in the waiting list for this event.
                    db.collection("AndroidID").document(userID).get()
                            .addOnCompleteListener(task2 ->{
                                if(task2.isSuccessful() && task2.getResult() != null){
                                    DocumentSnapshot entrantData = task2.getResult();
                                    String entrantID = userID;
                                    String entrantName = entrantData.get("name").toString();
                                    String entrantEmail = entrantData.get("email").toString();
                                    String phoneNumber = entrantData.get("phoneNumber").toString();
                                    String email = entrantData.get("email").toString();
//                                    String entrantProfilePic = entrantData.get("profilePictureUrl").toString();
                                    boolean notifications = entrantData.getBoolean("notifications");
                                    String facilityID = entrantData.getString("facilityID");
                                    double latitude = entrantData.getDouble("latitude");
                                    double longitude = entrantData.getDouble("longitude");
                                    Entrant entrant = new Entrant(entrantID, entrantName, phoneNumber, entrantEmail, null, notifications, facilityID, latitude, longitude);
                                    entrant.setFacilityID(facilityID);

                                    db.collection("AndroidID").document(entrantID).collection("waitListedEvents").document(eventID).get().addOnSuccessListener(DocumentSnapshot -> {
                                        String status = DocumentSnapshot.getString("status");
                                        if (Objects.equals(status, "enrolled")){
                                            entrantListWaitinglisted.add(entrant);
                                        } else if (Objects.equals(status, "invited")) {
                                            entrantListInvited.add(entrant);
                                        } else if (Objects.equals(status, "accepted")) {
                                            entrantListAccepted.add(entrant);
                                        } else if (Objects.equals(status, "rejected")) {
                                            entrantListRejected.add(entrant);
                                        }
                                    });
                                    entrantListAll.add(entrant);
                                }
                                tasksCompleted[0]++;

                                if (tasksCompleted[0] == totalTasks){
                                    // All tasks complete, set up adapter
                                    entrantAdapterAll = new CustomEntrantAdapter(this, entrantListAll, eventID);
                                    entrantListView.setAdapter(entrantAdapterAll);
                                }
                            });
                }
            }
        });
    }

}
