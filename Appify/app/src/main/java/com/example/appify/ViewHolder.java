package com.example.appify;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
//This class is made using ChatGPT, "Help write the section that corresponds ONLY to sending the
// command (one button = 1 execution of a function) for one item.", 2024-11-04
public class ViewHolder {
    public class ViewHolder extends RecyclerView.ViewHolder {
        Button registerButton, denyButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            registerButton = itemView.findViewById(R.id.register_button);
            denyButton = itemView.findViewById(R.id.deny_button);
        }
    }
}
