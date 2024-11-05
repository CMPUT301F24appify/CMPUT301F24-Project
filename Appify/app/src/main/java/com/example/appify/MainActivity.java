package com.example.appify;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.provider.Settings.Secure;

import com.example.appify.Activities.EntrantHomePageActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String android_id;
    private boolean firstEntry = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();

        startUpAnimation();
    }

    void startUpAnimation(){

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
        appText.setTranslationX(appText_endX-500f);
        ObjectAnimator tX_appText = ObjectAnimator.ofFloat(appText,
                View.TRANSLATION_X,
                appText.getTranslationX(),
                appText_endX);
        tX_appText.setDuration(2000);
        appText.setTranslationX(appText_endX);

        // Animate the IFY text of the Appify Logo
        View ifyText = findViewById(R.id.ify_text);
        float ifyText_endX = ifyText.getTranslationX();
        ifyText.setTranslationX(ifyText_endX+500f);
        ObjectAnimator tX_ifyText = ObjectAnimator.ofFloat(ifyText,
                View.TRANSLATION_X,
                ifyText.getTranslationX(),
                ifyText_endX);
        tX_ifyText.setDuration(2000);
        ifyText.setTranslationX(ifyText_endX);

        AnimatorSet firstSet = new AnimatorSet();
        firstSet.playTogether(tY_topBar, tY_bottomBar, tX_appText, tX_ifyText);

        // When the animation ends, switch the view after a short delay.
        firstSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(checkAndAddAndroidId(android_id)){
                    Intent intent = new Intent(MainActivity.this,editUserActivity.class);
                    intent.putExtra("Android ID", android_id);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(MainActivity.this,userProfileActivity.class);
                    intent.putExtra("Android ID", android_id);
                    startActivity(intent);
                }

//                 Intent intent = new Intent(MainActivity.this,editUserActivity.class);
//                 intent.putExtra("Android ID", android_id);
//                 startActivity(intent);
            }
        });
        firstSet.start();
    }
    private boolean checkAndAddAndroidId(String androidId) {
        // Access the specific document by androidId

        db.collection("Android ID").document(androidId)
                .get()  // Fetch the document snapshot
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (!document.exists()) {
                            db.collection("Android ID").document(androidId)
                                    .set(new HashMap<>())  // Using an empty object to avoid adding fields
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Document created successfully with android_id: " + androidId))
                                    .addOnFailureListener(e -> Log.w("Firestore", "Error creating document", e));
                            firstEntry = true;
                        }
                    } else {
                        Log.w("Firestore", "Error checking for Android ID", task.getException());
                    }
                });
        return firstEntry;
    }
}
