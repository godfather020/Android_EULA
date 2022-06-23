package com.example.android_eula.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.android_eula.DashboardActivity;
import com.example.android_eula.R;

public class Fourth_frag extends Fragment {

    DashboardActivity activity;
    Button finish;

    public Fourth_frag() {
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
        View view = inflater.inflate(R.layout.fragment_fourth_frag, container, false);

        activity = (DashboardActivity) getContext();

        finish = view.findViewById(R.id.finish);

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activity.askAdminPassword();
            }
        });

        return view;
    }
}