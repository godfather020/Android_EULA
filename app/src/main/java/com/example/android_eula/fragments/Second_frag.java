package com.example.android_eula.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.android_eula.DashboardActivity;
import com.example.android_eula.R;

public class Second_frag extends Fragment {

    DashboardActivity activity;
    private Button second_to_third;

    public Second_frag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second_frag, container, false);

        activity = (DashboardActivity) getContext();

        activity.first_radio.setEnabled(false);
        activity.second_radio.setEnabled(true);
        activity.second_radio.setChecked(true);
        activity.second_radio.setClickable(false);
        activity.third_radio.setEnabled(false);

        second_to_third = view.findViewById(R.id.second_to_third);

        second_to_third.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activity.getSupportFragmentManager().beginTransaction().replace(R.id.frag_cont, new Third_frag()).commit();
            }
        });

        return view;
    }
}