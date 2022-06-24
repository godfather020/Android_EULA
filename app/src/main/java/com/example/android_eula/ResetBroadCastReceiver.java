package com.example.android_eula;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.example.android_eula.firewallminor.ActivityMain;

public class ResetBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("reboot", String.valueOf(context.getSharedPreferences("One", Context.MODE_PRIVATE).getBoolean("One", true)));

        if (context.getSharedPreferences("One", Context.MODE_PRIVATE).getBoolean("One", true)) {

            //Toast.makeText(context, "Receiver", Toast.LENGTH_SHORT).show();

            Intent intent1 = new Intent(context, DashboardActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){

            Log.d("rebootBrod1", String.valueOf(context.getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 0)));

            if (context.getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 1) == 0) {

            //context.getSharedPreferences("reboot", Context.MODE_PRIVATE).edit().putInt("reboot", 0).apply();

            Log.d("rebootBrod2", String.valueOf(context.getSharedPreferences("reboot", Context.MODE_PRIVATE).getInt("reboot", 0)));
            // BlackHoleService.reload(null, context);
                /*Uri uri = new Uri.Builder().scheme("rating").authority("call").build();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(uri);
                i.putExtra("call_ratings_for", 1);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);*/
            Intent intent1 = new Intent(context, DashboardActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
            }
        }
    }
}
