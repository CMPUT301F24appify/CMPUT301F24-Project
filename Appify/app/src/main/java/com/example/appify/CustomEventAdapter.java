package com.example.appify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CustomEventAdapter extends ArrayAdapter<Event> {
    private Context context;
    private List<Event> eventList;

    public CustomEventAdapter(Context context, List<Event> eventList){
        super(context, 0, eventList);
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.event_list_content, parent, false);
        }

        Event event = eventList.get(position);

        TextView eventTitle = convertView.findViewById(R.id.event_title);
        TextView eventFacility = convertView.findViewById(R.id.event_facility);
        TextView eventDate = convertView.findViewById(R.id.event_date);
        TextView eventRegistrationEndDate = convertView.findViewById(R.id.event_registrationEndDate);

        eventTitle.setText(event.getName());
        eventFacility.setText(event.getFacility());
        eventDate.setText(event.getDate());
        eventRegistrationEndDate.setText(event.getRegistrationEndDate());
        return convertView;
    }
}
