package com.example.android_eula.fragments;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_eula.DashboardActivity;
import com.example.android_eula.R;
import com.example.android_eula.firewallminor.ActivityMain;
import com.example.android_eula.firewallminor.BlackHoleService;
import com.example.android_eula.firewallminor.Rule;
import com.example.android_eula.firewallminor.RuleAdapter;
import com.example.android_eula.firewallminor.Util;

import java.util.List;

public class First_frag extends Fragment {

    public DashboardActivity activity;
    private Button first_to_second;
    private static final int REQUEST_VPN = 1;
    private MenuItem searchItem = null;
    private boolean running = false;
    private RuleAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first_frag, container, false);

        activity = (DashboardActivity) getContext();

        activity.first_radio.setChecked(true);
        activity.first_radio.setEnabled(true);
        activity.first_radio.setClickable(false);
        activity.second_radio.setEnabled(false);
        activity.third_radio.setEnabled(false);

        first_to_second = view.findViewById(R.id.first_to_second);

        first_to_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent prepare = VpnService.prepare(requireContext());
                if (prepare == null) {
                    Log.e("TAG", "Prepare done");
                    onActivityResult(REQUEST_VPN, RESULT_OK, null);
                } else {
                    Log.i("TAG", "Start intent=" + prepare);
                    try {
                        startActivityForResult(prepare, REQUEST_VPN);
                    } catch (Throwable ex) {
                        Log.e("TAG", ex.toString() + "\n" + Log.getStackTraceString(ex));
                        onActivityResult(REQUEST_VPN, RESULT_CANCELED, null);
                        Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                fillApplicationList();

                // Listen for connectivity updates
                IntentFilter ifConnectivity = new IntentFilter();
                ifConnectivity.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                activity.registerReceiver( connectivityChangedReceiver, ifConnectivity);

                // Listen for added/removed applications
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
                intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                intentFilter.addDataScheme("package");
                activity.registerReceiver(packageChangedReceiver, intentFilter);


                //activity.getSupportFragmentManager().beginTransaction().replace(R.id.frag_cont, new Second_frag()).commit();
            }
        });

        return view;
    }

    private BroadcastReceiver connectivityChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("TAG", "Received " + intent);
            Util.logExtras("TAG", intent);
            //invalidateOptionsMenu();
        }
    };

    private BroadcastReceiver packageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("TAG", "Received " + intent);
            Util.logExtras("TAG", intent);
            fillApplicationList();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VPN) {
            // Update enabled state
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            // prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();

            // Start service
            if (resultCode == RESULT_OK) {

                /*Intent intent = new Intent(this, ActivityMain.class);
                startActivity(intent);*/

                BlackHoleService.start(requireContext());
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.frag_cont, new Second_frag()).commit();
                //activity.setDefaultCosuPolicies(false);
                //this.finish();
            }
            else {

                Toast.makeText(requireContext(), "You must enable VPN.", Toast.LENGTH_SHORT).show();
                Intent prepare = VpnService.prepare(requireContext());
                if (prepare == null) {
                    Log.e("TAG", "Prepare done");
                    onActivityResult(REQUEST_VPN, RESULT_OK, null);
                } else {
                    Log.i("TAG", "Start intent=" + prepare);
                    try {
                        startActivityForResult(prepare, REQUEST_VPN);
                    } catch (Throwable ex) {
                        Log.e("TAG", ex.toString() + "\n" + Log.getStackTraceString(ex));
                        onActivityResult(REQUEST_VPN, RESULT_CANCELED, null);
                        Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        Log.i("TAG", "Destroy");
        //running = false;
        //PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        //activity.unregisterReceiver(connectivityChangedReceiver);
        //activity.unregisterReceiver(packageChangedReceiver);

        BlackHoleService.reload(null, requireContext());

        //activity.getSharedPreferences("reboot", MODE_PRIVATE).edit().putInt("reboot", 0).apply();

        //Log.d("reboot", String.valueOf(activity.getSharedPreferences("reboot", MODE_PRIVATE).getInt("reboot", 0)));

        super.onDestroy();
    }

    private void fillApplicationList() {
        // Get recycler view
        final RecyclerView rvApplication = (RecyclerView) requireView().findViewById(R.id.fake_rv);
        rvApplication.setHasFixedSize(true);
        rvApplication.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Get/set application list
        new AsyncTask<Object, Object, List<Rule>>() {
            @Override
            protected List<Rule> doInBackground(Object... arg) {
                return Rule.getRules(requireContext());
            }

            @Override
            protected void onPostExecute(List<Rule> result) {
                if (running) {
                    if (searchItem != null)
                        MenuItemCompat.collapseActionView(searchItem);
                    adapter = new RuleAdapter(result, requireContext());
                    //rvApplication.setAdapter(adapter);
                }
            }
        }.execute();
    }
}