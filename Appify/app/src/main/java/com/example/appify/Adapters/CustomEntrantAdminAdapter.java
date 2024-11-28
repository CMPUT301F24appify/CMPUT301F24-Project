
package com.example.appify.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.appify.Model.Event;
import com.example.appify.Model.Entrant;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class CustomEntrantAdminAdapter extends ArrayAdapter<Entrant> {
    private Context context;
    private List<Entrant> entrantList;


    public CustomEntrantAdminAdapter(Context context, List<Entrant> entrantList){
        super(context, 0, entrantList);
        this.context = context;
        this.entrantList = entrantList;
    }


    /**
     * Provides a view for an AdapterView (ListView) for each event.
     * Sets event details in the view and fetches status data from Firebase for display.
     *
     * @param position     Position of the item within the adapter's data set.
     * @param convertView  The old view to reuse, if possible.
     * @param parent       The parent that this view will eventually be attached to.
     * @return The View corresponding to the data at the specified position.
     */
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.entrant_admin_list, parent, false);
        }

        MyApp app = (MyApp) context.getApplicationContext();

        Entrant entrant = entrantList.get(position);
        String entrantID = entrant.getId();
        TextView email = convertView.findViewById(R.id.email);
        TextView phoneNumber = convertView.findViewById(R.id.phone);
        TextView name = convertView.findViewById(R.id.entrant_name_admin);
        ImageView statusIcon = convertView.findViewById(R.id.statusIcon);
        ConstraintLayout eventCard = convertView.findViewById(R.id.event_information);

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + entrantID + ".jpg");
        long size = 1024 * 1024;
        storageRef.getBytes(size).addOnSuccessListener(bytes -> {
                // Convert the byte array to a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                statusIcon.setImageBitmap(bitmap);
            });
        email.setText("Email: " + entrant.getEmail());
        name.setText(entrant.getName());
        if(entrant.getPhoneNumber() != "") {
            phoneNumber.setVisibility(View.VISIBLE);
            phoneNumber.setText("Phone Number: " + entrant.getPhoneNumber());
        }
        statusIcon.setVisibility(View.VISIBLE);


//        LinearLayout topPart = convertView.findViewById(R.id.top_part);
//        LinearLayout bottomPart = convertView.findViewById(R.id.event_dates);
//        bottomPart.setVisibility(View.GONE);
//        topPart.setVisibility(View.GONE);

        return convertView;
    }
}