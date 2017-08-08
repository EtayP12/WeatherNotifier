package com.example.etayp.weathernotifier;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

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
        WeatherResponse weatherResponse =
                (new Gson()).fromJson(
                        intent.getStringExtra(Constants.WEATHER_RESPONSE_DATA)
                        , WeatherResponse.class
                );
        String key = intent.getStringExtra(Constants.ADDRESS_ID);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
        Address address = (new Gson()).fromJson(sharedPreferences.getString(key, ""), Address.class);
        double temperature = weatherResponse.getHourly().getData().get(0).getTemperature();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle("Temperature in " + address.getAddressLine(address.getMaxAddressLineIndex() - 1) + ": ")
                        .setContentText(String.valueOf(temperature));
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(key), mBuilder.build());
    }
}
