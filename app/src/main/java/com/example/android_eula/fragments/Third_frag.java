package com.example.android_eula.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.android_eula.DashboardActivity;
import com.example.android_eula.R;


public class Third_frag extends Fragment {

    DashboardActivity activity;
    WebView eula_agree;
    Button third_to_forth;
    CheckBox agree;

    public Third_frag() {
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
        View view = inflater.inflate(R.layout.fragment_third_frag, container, false);

        activity = (DashboardActivity) getContext();

        activity.first_radio.setEnabled(false);
        activity.second_radio.setEnabled(false);
        activity.third_radio.setChecked(true);
        activity.third_radio.setEnabled(true);
        activity.third_radio.setClickable(false);

        activity.next.setText("Finish");

        third_to_forth = view.findViewById(R.id.third_to_forth);

        eula_agree = view.findViewById(R.id.eula_agree);

        agree = view.findViewById(R.id.checkBox);

        eula_agree.loadUrl("file:///android_asset/eula_agreement.html");

        third_to_forth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (agree.isChecked()) {

                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.frag_cont, new Fourth_frag()).commit();
                }
                else {

                    Toast.makeText(requireContext(), "Please check the agreement", Toast.LENGTH_SHORT).show();
                }

                //activity.askAdminPassword();
            }
        });

        return view;
    }
}