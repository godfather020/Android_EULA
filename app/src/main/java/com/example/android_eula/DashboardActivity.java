package com.example.android_eula;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_eula.firewallminor.ActivityMain;
import com.example.android_eula.firewallminor.BlackHoleService;
import com.example.android_eula.firewallminor.Util;
import com.example.android_eula.fragments.First_frag;
import com.example.android_eula.fragments.VPN;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity implements KioskInterface {

    private TextView mTextMessage;
    private Button btn_call_first_activity;
    private Button btnkioskmode;
    public LinearLayout frag_cont;
    public RadioButton first_radio;
    public RadioButton second_radio;
    public RadioButton third_radio;
    public Button next;
    public static int enable = 0;
    KioskInterface callback;
    PackageManager p ;
    ComponentName componentName;
    private static final int REQUEST_VPN = 1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };
    private DevicePolicyManager mDevicePolicyManager;
    private ActivityManager am;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        p = getPackageManager();
        componentName = new ComponentName(this, DashboardActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        //p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        //p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Log.d("reboot", String.valueOf(getSharedPreferences("One", Context.MODE_PRIVATE).getBoolean("One", true)));

        getSharedPreferences("One", MODE_PRIVATE).edit().putBoolean("One", false).apply();

        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        mTextMessage = (TextView) findViewById(R.id.message);
        btnkioskmode=findViewById(R.id.btnkioskmode);

        first_radio = findViewById(R.id.radio1);
        second_radio = findViewById(R.id.radio2);
        third_radio = findViewById(R.id.radio3);
        next = findViewById(R.id.firstActivity);

        callback=(KioskInterface) this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE)
            {
                btnkioskmode.setText(R.string.button_txt_enable_kiosk);
            }
            else
            {
                btnkioskmode.setText(R.string.button_txt_disable_kiosk);
            }
        }

        btnkioskmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(am.getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE)
                    {
                        //askAdminPassword();
                        disableKioskMode(mDevicePolicyManager,am);
                    }
                    else
                    {
                        //CheckKioskModeDialog dialog= new CheckKioskModeDialog();
                        //dialog.show(getFragmentManager(),"KIOSK_MODE_DIALOG");
                        init();
                        checkDeviceOwner();
                    }
                }

            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        btn_call_first_activity=(Button) findViewById(R.id.btn_call_first_activity);
        btn_call_first_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DashboardActivity.this,FirstActivity.class);
                startActivity(intent);
            }
        });

        btnkioskmode.callOnClick();

        getSupportFragmentManager().beginTransaction().replace(R.id.frag_cont, new VPN()).commit();

    }

    @Override
    public void KioskSetupFinish() {
        btnkioskmode.setText("Disable Kiosk MODE");
    }


        public void askAdminPassword(){
           /* final Dialog dialog = new Dialog(DashboardActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.dialog_ask_admin_password);

            final EditText et_admin_password=(EditText) dialog.findViewById(R.id.et_admin_password);
            Button btn_proceed=(Button) dialog.findViewById(R.id.btn_proceed);
            Button btn_exit=(Button) dialog.findViewById(R.id.btn_exit);*/

            /*btn_proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if(!TextUtils.isEmpty(et_admin_password.getText()))
                    {
                        if(getResources().getString(R.string.device_admin_password).equalsIgnoreCase(et_admin_password.getText().toString()))
                        {*/

                            disableKioskMode(mDevicePolicyManager,am);
                           /* dialog.dismiss();
                        }
                        else
                        {
                            Toast.makeText(DashboardActivity.this, "Please enter valid password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            btn_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });



            dialog.show();*/

        }

    public void disableKioskMode(DevicePolicyManager devicePolicyManager,ActivityManager activityManager)
    {

    if(devicePolicyManager!=null && activityManager!=null){
        ComponentName mAdminComponentName = DeviceAdminReceiver.getComponentName(DashboardActivity.this);
            devicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, getPackageName());

        //devicePolicyManager.clearDeviceOwnerApp(getApplication().getPackageName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(activityManager.getLockTaskModeState()!=ActivityManager.LOCK_TASK_MODE_NONE)
            {
                btnkioskmode.setText(R.string.button_txt_enable_kiosk);
                //Intent intent = new Intent(this, ActivityMain.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(intent);

                /*Intent prepare = VpnService.prepare(DashboardActivity.this);
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
                        Toast.makeText(DashboardActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }

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
                setDefaultCosuPolicies(false);
                //BlackHoleService.reload(null, this);
                stopLockTask();
                finishAndRemoveTask();
                this.finish();

            }
        }

    }

    }

    /*private BroadcastReceiver connectivityChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("TAG", "Received " + intent);
            Util.logExtras("TAG", intent);
            invalidateOptionsMenu();
        }
    };*/

   /* private BroadcastReceiver packageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("TAG", "Received " + intent);
            Util.logExtras("TAG", intent);
            //fillApplicationList();
        }
    };*/

    //private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;
    private PackageManager mPackageManager;
    public void init(){
        mDevicePolicyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
        mPackageManager = this.getPackageManager();


    }

    public void checkDeviceOwner()
    {
        if ( mDevicePolicyManager.isDeviceOwnerApp(this.getApplicationContext().getPackageName()))
        {
            setupUserPolicy();

        } else
        {
            Toast.makeText(this,
                            "This app has not been given Device Owner privileges to manage this device and start lock task mode",Toast.LENGTH_SHORT)
                    .show();
            //dismiss();
        }
    }

    public void setupUserPolicy(){
        if(mDevicePolicyManager.isDeviceOwnerApp(this.getPackageName())){
            setDefaultCosuPolicies(true);
        }
        else {
            Toast.makeText(this,
                            "This app is not set as device owner and cannot start lock task mode",Toast.LENGTH_SHORT)
                    .show();
           // dismiss();
        }
    }
    public void setDefaultCosuPolicies(boolean active){
        // set user restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        // disable keyguard and status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);
        }

        // enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

        // set system update policy
        if (active){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                        SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                        null);
            }
        }

        // set this Activity as a lock task package

        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,
                active ? new String[]{this.getPackageName()} : new String[]{});

        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE)
            {
                if (active) {
                    this.startLockTask();
                }
            }
        }


        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            mDevicePolicyManager.addPersistentPreferredActivity(
                    mAdminComponentName, intentFilter, new ComponentName(
                            this.getPackageName(), DashboardActivity.class.getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                    mAdminComponentName, this.getPackageName());
        }

        callback.KioskSetupFinish();
        //getDialog().dismiss();
    }

    private void setUserRestriction(String restriction, boolean disallow){
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName,
                    restriction);
        }
    }

    private void enableStayOnWhilePluggedIn(boolean enabled){
        if (enabled) {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
                            | BatteryManager.BATTERY_PLUGGED_USB
                            | BatteryManager.BATTERY_PLUGGED_WIRELESS));
        } else {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    "0"
            );
        }
    }

    @Override
    public void onDestroy() {
        Log.i("TAG", "Destroy");
        //running = false;
        //PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        //unregisterReceiver(connectivityChangedReceiver);
        //unregisterReceiver(packageChangedReceiver);

        BlackHoleService.reload(null, this);

        //getSharedPreferences("reboot", MODE_PRIVATE).edit().putInt("reboot", 0).apply();

        //Log.d("reboot", String.valueOf(getSharedPreferences("reboot", MODE_PRIVATE).getInt("reboot", 0)));

        this.finishAndRemoveTask();

        super.onDestroy();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VPN) {
            // Update enabled state
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            // prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();

            // Start service
            if (resultCode == RESULT_OK) {

                Intent intent = new Intent(this, ActivityMain.class);
                startActivity(intent);

                BlackHoleService.start(this);
                setDefaultCosuPolicies(false);
                //this.finish();
            }
            else {

                Toast.makeText(this, "You must enable VPN.", Toast.LENGTH_SHORT).show();
                Intent prepare = VpnService.prepare(DashboardActivity.this);
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
                        Toast.makeText(DashboardActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }*/
}
