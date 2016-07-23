package com.example.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by user on 2/20/2016.
 */
public class UpgradeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentService = new Intent(context, MyService.class);
        context.startService(intentService);
        Toast.makeText(context, "UpgradeReceiver", Toast.LENGTH_LONG).show();
    }
}
