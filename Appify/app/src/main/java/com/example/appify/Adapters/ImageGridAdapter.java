package com.example.appify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.example.appify.R;

import java.util.List;

public class ImageGridAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> imageUrls;
    private int resource;

    public ImageGridAdapter(@NonNull Context context, int resource, @NonNull List<String> imageUrls) {
        super(context, resource, imageUrls);
        this.context = context;
        this.imageUrls = imageUrls;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }
        ImageView imageView = convertView.findViewById(R.id.image_item);
        Glide.with(context)
                .load(imageUrls.get(position))
                .into(imageView);

        return convertView;
    }
}
