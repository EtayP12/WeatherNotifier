package com.example.etayp.weathernotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by EtayP on 19-Aug-17.
 */

public class alarmReceiver extends BroadcastReceiver {
    private static final String TAG = "Alarm Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast toast = new Toast(context);
//        toast.setText("alarm");
//        toast.setDuration(Toast.LENGTH_LONG);
//        toast.show();
        Log.d(TAG, "onReceive: ");
    }
}
