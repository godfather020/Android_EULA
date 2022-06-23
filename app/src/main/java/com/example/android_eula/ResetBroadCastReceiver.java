package com.example.android_eula;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class ResetBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("reboot", String.valueOf(context.getSharedPreferences("One", Context.MODE_PRIVATE).getBoolean("One", true)));

        if (context.getSharedPreferences("One", Context.MODE_PRIVATE).getBoolean("One", true)) {

            Toast.makeText(context, "Receiver", Toast.LENGTH_SHORT).show();

            Intent intent1 = new Intent(context, DashboardActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
