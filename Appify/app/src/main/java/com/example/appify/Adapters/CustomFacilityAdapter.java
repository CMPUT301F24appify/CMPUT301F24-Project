package com.example.appify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.appify.Model.Facility;
import com.example.appify.R;

import java.util.List;

/**
 * Custom adapter to display facilities in a ListView.
 */
public class CustomFacilityAdapter extends ArrayAdapter<Facility> {
    private Context context;
    private List<Facility> facilityList;

    public CustomFacilityAdapter(Context context, List<Facility> facilityList) {
        super(context, 0, facilityList);
        this.context = context;
        this.facilityList = facilityList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.facility_list_content, parent, false);
        }

        Facility facility = facilityList.get(position);

        TextView facilityName = convertView.findViewById(R.id.facility_name);
        TextView facilityLocation = convertView.findViewById(R.id.facility_location);
        TextView facilityCapacity = convertView.findViewById(R.id.facility_capacity);

        facilityName.setText(facility.getName());
        facilityLocation.setText(facility.getLocation());
        facilityCapacity.setText(String.format("Capacity: %s", facility.getCapacity()));

        return convertView;
    }
}
