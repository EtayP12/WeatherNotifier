package com.example.etayp.weathernotifier;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

/**
 * Created by EtayP on 04-Aug-17.
 */

public class NotificationSender extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationSender(String name) {
        super(name);
    }
    public NotificationSender() {
        super("NotificationSender");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1,mBuilder.build());
    }
}
