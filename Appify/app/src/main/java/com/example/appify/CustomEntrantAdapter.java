package com.example.appify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CustomEntrantAdapter extends ArrayAdapter<Entrant> {
    private Context context;
    private List<Entrant> entrantList;

    public CustomEntrantAdapter(Context context, List<Entrant> entrantList){
        super(context,0,entrantList);
        this.context = context;
        this.entrantList = entrantList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.event_list_content, parent, false);
        }

        Event event = entrantList.get(position);

        TextView eventTitle = convertView.findViewById(R.id.event_title);
        TextView eventDesc = convertView.findViewById(R.id.event_desc);

        eventTitle.setText(event.getName());
        eventDesc.setText(event.getDescription());
        return convertView;
    }

}
