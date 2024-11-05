package com.example.appify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.appify.Model.Entrant;
import com.example.appify.R;

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
            convertView = LayoutInflater.from(context).inflate(R.layout.entrant_list_content, parent, false);
        }

        Entrant entrant = entrantList.get(position);

        TextView entrantNameView = convertView.findViewById(R.id.entrant_name_text);
        TextView entrantStatusView = convertView.findViewById(R.id.entrant_status_text);

        entrantNameView.setText(entrant.getName());
        entrantStatusView.setText("temp");
        return convertView;
    }

}
