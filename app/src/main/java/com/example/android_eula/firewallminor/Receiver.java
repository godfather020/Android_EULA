package com.example.android_eula.firewallminor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("TAG", "Received " + intent);
        Util.logExtras("TAG", intent);

        // Start service
        /*if (VpnService.prepare(context) == null)
            BlackHoleService.start(context);*/

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){

            //if (context.getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 1) == 0) {

                context.getSharedPreferences("reboot", Context.MODE_PRIVATE).edit().putInt("reboot", 1).apply();

                Log.d("reboot", String.valueOf(context.getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 0)));

                Intent intent1 = new Intent(context, ActivityMain.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            //}
        }

    }
}