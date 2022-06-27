package com.example.android_eula.fragments;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_eula.DashboardActivity;
import com.example.android_eula.R;
import com.example.android_eula.firewallminor.ActivityMain;
import com.example.android_eula.firewallminor.BlackHoleService;
import com.example.android_eula.firewallminor.Rule;
import com.example.android_eula.firewallminor.RuleAdapter;
import com.example.android_eula.firewallminor.Util;

import java.util.List;


public class VPN extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "Firewall.Main";

    private boolean running = false;
    private RuleAdapter adapter = null;
    private MenuItem searchItem = null;
    Button finish_btn;
    DashboardActivity activity;
    public RecyclerView rvApplication;
    Button close;
    TextView app_name, vpn_msg;
    ImageView app_img;
    LinearLayout layout_one, layout_vpn;

    private static final int REQUEST_VPN = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "Create");

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        // getContext().setTheme(prefs.getBoolean("dark_theme", false) ? R.style.AppThemeDark : R.style.AppTheme);

        super.onCreate(savedInstanceState);
        //requireActivity().setContentView(R.layout.main);

        activity = (DashboardActivity) getContext();

        running = true;

        setHasOptionsMenu(true);

        // Action bar
        View view = getLayoutInflater().inflate(R.layout.actionbar, null);
        //Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        //getSupportActionBar().setCustomView(view);

        // On/off switch
        SwitchCompat swEnabled = (SwitchCompat) view.findViewById(R.id.swEnabled);
        prefs.edit().putBoolean("enabled", true).apply();
        swEnabled.setChecked(prefs.getBoolean("enabled", false));

        //fillApplicationList();

        // Listen for connectivity updates
        IntentFilter ifConnectivity = new IntentFilter();
        ifConnectivity.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.registerReceiver(connectivityChangedReceiver, ifConnectivity);

        // Listen for added/removed applications
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        activity.registerReceiver(packageChangedReceiver, intentFilter);

        //swEnabled.toggle();
        swEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                swEnabled.setChecked(true);
            }
        });

        // Listen for preference changes
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Fill application list


        swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "Switch on");
                    Intent prepare = VpnService.prepare(requireContext());
                    if (prepare == null) {
                        Log.e(TAG, "Prepare done");
                        onActivityResult(REQUEST_VPN, RESULT_OK, null);
                    } else {
                        Log.i(TAG, "Start intent=" + prepare);
                        try {
                            startActivityForResult(prepare, REQUEST_VPN);
                        } catch (Throwable ex) {
                            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                            onActivityResult(REQUEST_VPN, RESULT_CANCELED, null);
                            Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.i(TAG, "Switch off");
                    prefs.edit().putBoolean("enabled", false).apply();
                    BlackHoleService.stop(requireContext());
                }
            }
        });

        //swEnabled.callOnClick();

        // Listen for preference changes
        /*prefs.registerOnSharedPreferenceChangeListener(this);

        // Fill application list
        fillApplicationList();

        // Listen for connectivity updates
        IntentFilter ifConnectivity = new IntentFilter();
        ifConnectivity.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityChangedReceiver, ifConnectivity);

        // Listen for added/removed applications
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(packageChangedReceiver, intentFilter);*/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_v_p_n, container, false);

        rvApplication = view.findViewById(R.id.rvApplication);
        finish_btn = view.findViewById(R.id.first_to_second);
        close = view.findViewById(R.id.finish_btn);
        vpn_msg = view.findViewById(R.id.vpn_msg);
        app_name = view.findViewById(R.id.app_name);
        app_img = view.findViewById(R.id.app_img);
        layout_one = view.findViewById(R.id.layout_first);
        layout_vpn = view.findViewById(R.id.layout_vpn);

        Log.d("rebootVPN1", String.valueOf(requireContext().getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 1)));

        fillApplicationList();

        if (requireContext().getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 1) == 0){

            Log.d("rebootVPN2", String.valueOf(requireContext().getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 1)));

            close.setVisibility(View.VISIBLE);
            vpn_msg.setVisibility(View.VISIBLE);
            app_name.setVisibility(View.VISIBLE);
            layout_one.setVisibility(View.GONE);
            layout_vpn.setVisibility(View.VISIBLE);

        }
        else {

            layout_one.setVisibility(View.VISIBLE);
            layout_vpn.setVisibility(View.GONE);
            close.setVisibility(View.GONE);
            vpn_msg.setVisibility(View.GONE);
            app_name.setVisibility(View.GONE);

        }

        Intent prepare = VpnService.prepare(requireContext());
        if (prepare == null) {
            Log.e(TAG, "Prepare done");
            onActivityResult(REQUEST_VPN, RESULT_OK, null);
        } else {
            Log.i(TAG, "Start intent=" + prepare);
            try {
                startActivityForResult(prepare, REQUEST_VPN);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                onActivityResult(REQUEST_VPN, RESULT_CANCELED, null);
                Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activity.askAdminPassword();
            }
        });

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requireContext().getSharedPreferences("reboot", Context.MODE_PRIVATE).edit().putInt("reboot", 0).apply();
                Log.d("rebootVPN3", String.valueOf(requireContext().getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 1)));
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.frag_cont, new Second_frag()).commit();
                //activity.setDefaultCosuPolicies(false);
                //stopLockTask();
                //activity.stopLockTask();
                //activity.finishActivity(1);
                //activity.finishAndRemoveTask();
                //finishAndRemoveTask();
            }
        });

        return view;
    }

    /*@Override
    public void onDestroy() {
        Log.i(TAG, "Destroy");
        //running = false;
        //PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        requireContext().unregisterReceiver(connectivityChangedReceiver);
        requireContext().unregisterReceiver(packageChangedReceiver);

        BlackHoleService.reload(null, requireContext());

        requireContext().getSharedPreferences("reboot", MODE_PRIVATE).edit().putInt("reboot", 0).apply();

        Log.d("reboot", String.valueOf(requireContext().getSharedPreferences("reboot", MODE_PRIVATE).getInt("reboot", 0)));

        super.onDestroy();
    }*/

    private BroadcastReceiver connectivityChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received " + intent);
            Util.logExtras(TAG, intent);
            //invalidateOptionsMenu();
        }
    };

    private BroadcastReceiver packageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received " + intent);
            Util.logExtras(TAG, intent);
            //fillApplicationList();
        }
    };

    private void fillApplicationList() {
        // Get recycler view
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String name) {
        Log.i(TAG, "Preference " + name + "=" + prefs.getAll().get(name));
        if ("enabled".equals(name)) {
            // Get enabled
            //boolean enabled = prefs.getBoolean(name, false);

            // Check switch state
            //SwitchCompat swEnabled = (SwitchCompat) getSupportActionBar().getCustomView().findViewById(R.id.swEnabled);
            //if (swEnabled.isChecked() != enabled)
              //  swEnabled.setChecked(enabled);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.main, menu);

        // Search
        searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null)
                    adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.getFilter().filter(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (adapter != null)
                    adapter.getFilter().filter(null);
                return true;
            }
        });

        //return true;

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        MenuItem network = menu.findItem(R.id.menu_network);
        network.setIcon(Util.isWifiActive(requireContext()) ? R.drawable.ic_network_wifi_white_24dp : R.drawable.ic_network_cell_white_24dp);

        MenuItem wifi = menu.findItem(R.id.menu_whitelist_wifi);
        wifi.setChecked(prefs.getBoolean("whitelist_wifi", true));

        MenuItem other = menu.findItem(R.id.menu_whitelist_other);
        other.setChecked(prefs.getBoolean("whitelist_other", true));

        MenuItem dark = menu.findItem(R.id.menu_dark);
        dark.setChecked(prefs.getBoolean("dark_theme", false));

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_network:
                Intent settings = new Intent(Util.isWifiActive(requireContext())
                        ? Settings.ACTION_WIFI_SETTINGS : Settings.ACTION_WIRELESS_SETTINGS);
                if (settings.resolveActivity(requireActivity().getPackageManager()) != null)
                    startActivity(settings);
                else
                    Log.w(TAG, settings + " not available");
                return true;

            case R.id.menu_refresh:
                fillApplicationList();
                return true;

            case R.id.menu_whitelist_wifi:
                prefs.edit().putBoolean("whitelist_wifi", !prefs.getBoolean("whitelist_wifi", true)).apply();
                fillApplicationList();
                BlackHoleService.reload("wifi", requireContext());
                return true;

            case R.id.menu_whitelist_other:
                prefs.edit().putBoolean("whitelist_other", !prefs.getBoolean("whitelist_other", true)).apply();
                fillApplicationList();
                BlackHoleService.reload("other", requireContext());
                return true;

            case R.id.menu_reset_wifi:
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.msg_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reset("wifi");
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;

            case R.id.menu_reset_other:
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.msg_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reset("other");
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;

            case R.id.menu_dark:
                prefs.edit().putBoolean("dark_theme", !prefs.getBoolean("dark_theme", false)).apply();
                requireActivity().recreate();
                return true;

            case R.id.menu_vpn_settings:
                // Open VPN settings
                Intent vpn = new Intent("android.net.vpn.SETTINGS");
                if (vpn.resolveActivity(requireActivity().getPackageManager()) != null)
                    startActivity(vpn);
                else
                    Log.w(TAG, vpn + " not available");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reset(String network) {
        SharedPreferences other = requireContext().getSharedPreferences(network, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = other.edit();
        for (String key : other.getAll().keySet())
            edit.remove(key);
        edit.apply();
        fillApplicationList();
        BlackHoleService.reload(network, requireContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VPN) {
            // Update enabled state
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            // prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();

            // Start service
            if (resultCode == RESULT_OK) {
                finish_btn.setVisibility(View.VISIBLE);
                BlackHoleService.start(requireContext());

            }
            else {
                finish_btn.setVisibility(View.GONE);
                //Toast.makeText(requireContext(), "You must enable VPN.", Toast.LENGTH_SHORT).show();
                Intent prepare = VpnService.prepare(requireContext());
                if (prepare == null) {
                    Log.e(TAG, "Prepare done");
                    onActivityResult(REQUEST_VPN, RESULT_OK, null);
                } else {
                    Log.i(TAG, "Start intent=" + prepare);
                    try {
                        startActivityForResult(prepare, REQUEST_VPN);
                    } catch (Throwable ex) {
                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                        onActivityResult(REQUEST_VPN, RESULT_CANCELED, null);
                        Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

}