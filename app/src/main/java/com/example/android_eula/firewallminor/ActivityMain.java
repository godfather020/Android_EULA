package com.example.android_eula.firewallminor;

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
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_eula.DashboardActivity;
import com.example.android_eula.KioskInterface;
import com.example.android_eula.R;

import java.util.List;

public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, KioskInterface {
    private static final String TAG = "Firewall.Main";

    private boolean running = false;
    private RuleAdapter adapter = null;
    private MenuItem searchItem = null;
    private Button finish_btn;

    private static final int REQUEST_VPN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Create");

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(prefs.getBoolean("dark_theme", false) ? R.style.AppThemeDark : R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        DashboardActivity activity = new DashboardActivity();

        running = true;

        // Action bar
        View view = getLayoutInflater().inflate(R.layout.actionbar, null);
        //Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        //getSupportActionBar().setCustomView(view);

        // On/off switch
        SwitchCompat swEnabled = (SwitchCompat) view.findViewById(R.id.swEnabled);
        prefs.edit().putBoolean("enabled", true).apply();
        swEnabled.setChecked(prefs.getBoolean("enabled", false));

        finish_btn = findViewById(R.id.finish_btn);

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
        registerReceiver(packageChangedReceiver, intentFilter);

        Intent prepare = VpnService.prepare(ActivityMain.this);
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
                Toast.makeText(ActivityMain.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        }

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
                    Intent prepare = VpnService.prepare(ActivityMain.this);
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
                            Toast.makeText(ActivityMain.this, ex.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.i(TAG, "Switch off");
                    prefs.edit().putBoolean("enabled", false).apply();
                    BlackHoleService.stop(ActivityMain.this);
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

        if (getSharedPreferences("reboot", MODE_PRIVATE).getInt("reboot", 0) == 1){

            getSharedPreferences("reboot", MODE_PRIVATE).edit().putInt("reboot", 0).apply();
            //stopLockTask();
            this.finishAndRemoveTask();
        }

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //activity.setDefaultCosuPolicies(false);
                stopLockTask();
                //activity.stopLockTask();
                activity.finishActivity(1);
                activity.finishAndRemoveTask();
                finishAndRemoveTask();
            }
        });
        //this.finishAndRemoveTask();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroy");
        //running = false;
        //PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(connectivityChangedReceiver);
        unregisterReceiver(packageChangedReceiver);

        BlackHoleService.reload(null, this);

        getSharedPreferences("reboot", MODE_PRIVATE).edit().putInt("reboot", 0).apply();

        Log.d("reboot", String.valueOf(getSharedPreferences("reboot", MODE_PRIVATE).getInt("reboot", 0)));

        super.onDestroy();
    }

    private BroadcastReceiver connectivityChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received " + intent);
            Util.logExtras(TAG, intent);
            invalidateOptionsMenu();
        }
    };

    private BroadcastReceiver packageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received " + intent);
            Util.logExtras(TAG, intent);
            fillApplicationList();
        }
    };

    private void fillApplicationList() {
        // Get recycler view
        final RecyclerView rvApplication = (RecyclerView) findViewById(R.id.rvApplication);
        rvApplication.setHasFixedSize(true);
        rvApplication.setLayoutManager(new LinearLayoutManager(this));

        // Get/set application list
        new AsyncTask<Object, Object, List<Rule>>() {
            @Override
            protected List<Rule> doInBackground(Object... arg) {
                return Rule.getRules(ActivityMain.this);
            }

            @Override
            protected void onPostExecute(List<Rule> result) {
                if (running) {
                    if (searchItem != null)
                        MenuItemCompat.collapseActionView(searchItem);
                    adapter = new RuleAdapter(result, ActivityMain.this);
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
            boolean enabled = prefs.getBoolean(name, false);

            // Check switch state
            SwitchCompat swEnabled = (SwitchCompat) getSupportActionBar().getCustomView().findViewById(R.id.swEnabled);
            if (swEnabled.isChecked() != enabled)
                swEnabled.setChecked(enabled);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
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

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        MenuItem network = menu.findItem(R.id.menu_network);
        network.setIcon(Util.isWifiActive(this) ? R.drawable.ic_network_wifi_white_24dp : R.drawable.ic_network_cell_white_24dp);

        MenuItem wifi = menu.findItem(R.id.menu_whitelist_wifi);
        wifi.setChecked(prefs.getBoolean("whitelist_wifi", true));

        MenuItem other = menu.findItem(R.id.menu_whitelist_other);
        other.setChecked(prefs.getBoolean("whitelist_other", true));

        MenuItem dark = menu.findItem(R.id.menu_dark);
        dark.setChecked(prefs.getBoolean("dark_theme", false));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_network:
                Intent settings = new Intent(Util.isWifiActive(this)
                        ? Settings.ACTION_WIFI_SETTINGS : Settings.ACTION_WIRELESS_SETTINGS);
                if (settings.resolveActivity(getPackageManager()) != null)
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
                BlackHoleService.reload("wifi", this);
                return true;

            case R.id.menu_whitelist_other:
                prefs.edit().putBoolean("whitelist_other", !prefs.getBoolean("whitelist_other", true)).apply();
                fillApplicationList();
                BlackHoleService.reload("other", this);
                return true;

            case R.id.menu_reset_wifi:
                new AlertDialog.Builder(this)
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
                new AlertDialog.Builder(this)
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
                recreate();
                return true;

            case R.id.menu_vpn_settings:
                // Open VPN settings
                Intent vpn = new Intent("android.net.vpn.SETTINGS");
                if (vpn.resolveActivity(getPackageManager()) != null)
                    startActivity(vpn);
                else
                    Log.w(TAG, vpn + " not available");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reset(String network) {
        SharedPreferences other = getSharedPreferences(network, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = other.edit();
        for (String key : other.getAll().keySet())
            edit.remove(key);
        edit.apply();
        fillApplicationList();
        BlackHoleService.reload(network, ActivityMain.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VPN) {
            // Update enabled state
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
           // prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();

            // Start service
            if (resultCode == RESULT_OK) {
                BlackHoleService.start(this);

            }
            else {

                Toast.makeText(this, "You must enable VPN.", Toast.LENGTH_SHORT).show();
                Intent prepare = VpnService.prepare(ActivityMain.this);
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
                        Toast.makeText(ActivityMain.this, ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void KioskSetupFinish() {

    }
}
