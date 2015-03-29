package com.proloser.spotifypractice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
        } else {
            Intent startAlarm = new Intent(context, AlarmPlayer.class);
            startAlarm.putExtras(intent);
            context.startActivity(startAlarm);

            Log.d("AlarmReceiver", "Alarm triggered");
            Toast.makeText(context, "Alarm triggered", Toast.LENGTH_SHORT).show();
        }
    }

}