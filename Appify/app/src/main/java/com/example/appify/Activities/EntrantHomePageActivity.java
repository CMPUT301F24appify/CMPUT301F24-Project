package com.example.appify.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Adapters.CustomEventAdapter;
import com.example.appify.MainActivity;
import com.example.appify.Model.Event;
import com.example.appify.HeaderNavigation;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

/**
 * The EntrantHomePageActivity class displays a list of events available to entrants.
 * It fetches event data from Firestore, populates a ListView using a custom adapter,
 * and handles user interactions such as clicking on an event to view details.
 */
public class EntrantHomePageActivity extends AppCompatActivity {
    // Variables
    private FirebaseFirestore db;
    ListView eventListView;
    CustomEventAdapter eventAdapter;
    ArrayList<Event> eventList;

    /**
     * Called when the activity is starting. Initializes UI components and loads event data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, then this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        setContentView(R.layout.entrant_home_page);

        db = FirebaseFirestore.getInstance();

        // Initialize the ListView and the Adapter
        eventList = new ArrayList<>();
        eventAdapter = new CustomEventAdapter(this, eventList, false);
        eventListView = findViewById(R.id.home_events);
        eventListView.setAdapter(eventAdapter);

        // HeaderNavigation
        HeaderNavigation headerNavigation = new HeaderNavigation(this);
        headerNavigation.setupNavigation();
        TextView eventsText = findViewById(R.id.eventsText_navBar);
        eventsText.setTextColor(Color.parseColor("#800080"));
        eventsText.setTypeface(eventsText.getTypeface(), Typeface.BOLD);

        // Load Events from Firebase
        loadEventsFromFirestore();


        // OnClickListener for the Events in the ListView
        eventListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Event selectedEvent = eventList.get(position);

            System.out.println("abc " + selectedEvent.getName());
            Intent intent = new Intent(EntrantHomePageActivity.this, EntrantEnlistActivity.class);

            intent.putExtra("eventId", selectedEvent.getEventId());
            intent.putExtra("name", selectedEvent.getName());
            intent.putExtra("date", selectedEvent.getDate());
            intent.putExtra("registrationEndDate", selectedEvent.getRegistrationEndDate());
            intent.putExtra("facility", selectedEvent.getFacility());
            intent.putExtra("description", selectedEvent.getDescription());
            intent.putExtra("maxWaitEntrants", selectedEvent.getMaxWaitEntrants());
            intent.putExtra("maxSampleEntrants", selectedEvent.getMaxSampleEntrants());
            intent.putExtra("posterUri", selectedEvent.getPosterUri());
            intent.putExtra("geolocate", selectedEvent.isGeolocate());

            startActivity(intent);
        });

        // To add event, open a QR Code scanner
        Button scanEventButton = findViewById(R.id.scanEvent_button);
        scanEventButton.setOnClickListener(v->{
            scanCode();
        });
    }

    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents()!=null){
            AlertDialog.Builder builder = new AlertDialog.Builder(EntrantHomePageActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                    dialogInterface.dismiss();
                }
            }).show();
        }
    });

    /**
     * Loads events from the Firestore database and updates the ListView.
     * Retrieves documents from the "events" collection, creates Event objects,
     * and notifies the adapter to refresh the ListView.
     */
    private void loadEventsFromFirestore() {
        MyApp app = (MyApp) getApplication();
        String androidId = app.getAndroidId();
        System.out.println(androidId);

        CollectionReference userRef = db.collection("Android ID").document(androidId).collection("waitListedEvents");

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                eventList.clear(); // Clear the list to avoid duplicates
                for (QueryDocumentSnapshot event : task.getResult()) {
                    // Use the fromFirestore method to create an Event object

                    db.collection("events").document(event.getId()).get().addOnCompleteListener(task1 -> {
                        System.out.println(db.collection("events").document(event.getId()));
                        DocumentSnapshot eventData = task1.getResult();
                        System.out.println(eventData);

                        String eventID = eventData.getId();
                        String name = eventData.getString("name");
                        System.out.println(name);
                        String date = eventData.getString("date");
                        System.out.println(date);
                        String registrationEndDate = eventData.getString("registrationEndDate");
                        System.out.println(registrationEndDate);
                        String description = eventData.getString("description");
                        System.out.println(description);
                        String facility = eventData.getString("facility");
                        System.out.println(facility);
                        int maxWaitEntrants = eventData.getLong("maxWaitEntrants").intValue();
                        System.out.println(maxWaitEntrants);
                        int maxSampleEntrants = eventData.getLong("maxSampleEntrants").intValue();
                        System.out.println(maxSampleEntrants);
                        String posterUri = eventData.getString("posterUri");
                        System.out.println(posterUri);
                        boolean isGeolocate = eventData.getBoolean("geolocate") != null ? eventData.getBoolean("geolocate") : false;
                        System.out.println(isGeolocate);
                        boolean notifyWaitlisted = eventData.getBoolean("notifyWaitlisted") != null ? eventData.getBoolean("notifyWaitlisted") : false;
                        System.out.println(notifyWaitlisted);
                        boolean notifyEnrolled = eventData.getBoolean("notifyEnrolled") != null ? eventData.getBoolean("notifyEnrolled") : false;
                        System.out.println(notifyEnrolled);
                        boolean notifyCancelled = eventData.getBoolean("notifyCancelled") != null ? eventData.getBoolean("notifyCancelled") : false;
                        System.out.println(notifyCancelled);
                        boolean notifyInvited = eventData.getBoolean("notifyInvited") != null ? eventData.getBoolean("notifyInvited") : false;
                        System.out.println(notifyInvited);
                        String organizerID = eventData.getString("organizerID");
                        System.out.println(organizerID);

                        // Retrieve notification messages
                        String waitlistedMessage = eventData.getString("waitlistedMessage");
                        String enrolledMessage = eventData.getString("enrolledMessage");
                        String cancelledMessage = eventData.getString("cancelledMessage");
                        String invitedMessage = eventData.getString("invitedMessage");

                        Event event1 = new Event(name,date,facility,registrationEndDate,description,maxWaitEntrants,maxSampleEntrants,posterUri,isGeolocate,notifyWaitlisted,notifyEnrolled,notifyCancelled,notifyInvited,waitlistedMessage,enrolledMessage,cancelledMessage,invitedMessage,organizerID);
                        event1.setEventId(eventID);
                        eventList.add(event1);
                        // Notify the adapter that data has changed
                        eventAdapter.notifyDataSetChanged();
                    });
                }

            } else {

                Toast.makeText(EntrantHomePageActivity.this, "Error getting events.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
