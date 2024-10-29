package com.example.appify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Variables to be used
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the Firebase Firestore and FirebaseAuth
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



        startUpAnimation();
        loadEvents();
    }

    void startUpAnimation() {
        // Animate the top bar of the Appify Logo
        View topBar = findViewById(R.id.top_bar);
        float topBar_endY = topBar.getTranslationY();
        topBar.setTranslationY(-1000f);
        ObjectAnimator tY_topBar = ObjectAnimator.ofFloat(topBar,
                View.TRANSLATION_Y,
                topBar.getTranslationY(),
                topBar_endY);
        tY_topBar.setDuration(2000);
        topBar.setTranslationY(topBar_endY);

        // Animate the bottom bar of the Appify Logo
        View bottomBar = findViewById(R.id.bottom_bar);
        float bottomBar_endY = bottomBar.getTranslationY();
        bottomBar.setTranslationY(+1000f);
        ObjectAnimator tY_bottomBar = ObjectAnimator.ofFloat(bottomBar,
                View.TRANSLATION_Y,
                bottomBar.getTranslationY(),
                bottomBar_endY);
        tY_bottomBar.setDuration(2000);
        bottomBar.setTranslationY(bottomBar_endY);

        // Animate the APP text of the Appify Logo
        View appText = findViewById(R.id.app_text);
        float appText_endX = appText.getTranslationX();
        appText.setTranslationX(appText_endX - 500f);
        ObjectAnimator tX_appText = ObjectAnimator.ofFloat(appText,
                View.TRANSLATION_X,
                appText.getTranslationX(),
                appText_endX);
        tX_appText.setDuration(2000);
        appText.setTranslationX(appText_endX);

        // Animate the IFY text of the Appify Logo
        View ifyText = findViewById(R.id.ify_text);
        float ifyText_endX = ifyText.getTranslationX();
        ifyText.setTranslationX(ifyText_endX + 500f);
        ObjectAnimator tX_ifyText = ObjectAnimator.ofFloat(ifyText,
                View.TRANSLATION_X,
                ifyText.getTranslationX(),
                ifyText_endX);
        tX_ifyText.setDuration(2000);
        ifyText.setTranslationX(ifyText_endX);

        AnimatorSet firstSet = new AnimatorSet();
        firstSet.playTogether(tY_topBar, tY_bottomBar, tX_appText, tX_ifyText);
        final View rootLayout = findViewById(R.id.main);

        // When the animation ends, hide each logo element and show the RecyclerView
        firstSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Hide each logo element
                appText.setVisibility(View.GONE);
                ifyText.setVisibility(View.GONE);
                topBar.setVisibility(View.GONE);
                bottomBar.setVisibility(View.GONE);

                // Animate background color change
                int colorFrom = ContextCompat.getColor(MainActivity.this, R.color.background);
                int colorTo = ContextCompat.getColor(MainActivity.this, R.color.white);
                ValueAnimator colorAnimation = ValueAnimator.ofArgb(colorFrom, colorTo);
                colorAnimation.setDuration(1000); // Set duration for color transition
                colorAnimation.addUpdateListener(animator -> rootLayout.setBackgroundColor((int) animator.getAnimatedValue()));
                colorAnimation.start();

                // Show the RecyclerView
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        firstSet.start();
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
