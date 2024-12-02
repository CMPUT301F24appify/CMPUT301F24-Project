
package com.example.appify.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.appify.Activities.ImageGridActivity;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;


public class CustomFolderAdapter extends ArrayAdapter {

    private Context context; // Context in which the adapter is used
    private FirebaseStorage storage; // FirebaseStorage instance for accessing storage data
    private List<StorageReference> folderList; // List of folders to display


    /**
     * Constructor for initializing the adapter with folder data and context.
     *
     * @param context    The current context.
     * @param folderList List of folder references to display.
     */
    public CustomFolderAdapter(Context context, List<StorageReference> folderList){
        super(context, 0, folderList);
        this.context = context;
        this.folderList = folderList;
    }


    /**
     * Provides a view for each folder in the ListView.
     * Sets folder name based on its Firebase Storage reference and attaches click listeners for navigation.
     *
     * @param position    Position of the folder in the list.
     * @param convertView Recycled view (if available).
     * @param parent      Parent view group.
     * @return Updated View for the folder at the specified position.
     */
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            // Inflate the layout for a single folder item in the list
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_image_folders, parent, false);
        }

        // Initialize FirebaseStorage instance
        storage = FirebaseStorage.getInstance();

        // Get the folder reference at the current position
        StorageReference ref = folderList.get(position);

        // Bind the TextView for folder name
        TextView name = convertView.findViewById(R.id.folder_Name);

        // Set display name for known folders
        if (ref.getName().equals("event_posters")){
            name.setText("Event Posters");
        } else if (ref.getName().equals("profile_images")) {
            name.setText("Profile Images");
        }

        // Retrieve application context for global data access
        MyApp app = (MyApp) context.getApplicationContext();

        // Set click listener for navigation to ImageGridActivity
        convertView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageGridActivity.class);
            intent.putExtra("StorageReferenceName", ref.getName());
            context.startActivity(intent);
        });


        return convertView;
    }
}
