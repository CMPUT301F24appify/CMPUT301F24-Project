
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

import com.example.appify.Activities.EventDetailActivity;
import com.example.appify.Activities.ImageGridActivity;
import com.example.appify.MyApp;
import com.example.appify.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;


public class CustomFolderAdapter extends ArrayAdapter {
    private Context context;
    private FirebaseStorage storage;
    private List<StorageReference> folderList;
    private String folderName;


    public CustomFolderAdapter(Context context, List<StorageReference> folderList){
        super(context, 0, folderList);
        this.context = context;
        this.folderList = folderList;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_image_folders, parent, false);
        }

        storage = FirebaseStorage.getInstance();
        StorageReference ref = folderList.get(position);
        TextView name = convertView.findViewById(R.id.folder_Name);

        if (ref.getName().equals("event_posters")){
            name.setText("Event Posters");
        } else if (ref.getName().equals("profile_images")) {
            name.setText("Profile Images");
        }
        MyApp app = (MyApp) context.getApplicationContext();
        convertView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageGridActivity.class);
            intent.putExtra("StorageReferenceName", ref.getName());
            context.startActivity(intent);
        });


        return convertView;
    }
}
