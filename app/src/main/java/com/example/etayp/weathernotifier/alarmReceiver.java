package com.example.etayp.weathernotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.etayp.weathernotifier.dummy.WeatherUpdateItem;
import com.google.gson.Gson;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by EtayP on 19-Aug-17.
 */

public class alarmReceiver extends BroadcastReceiver {
    private static final String TAG = "Alarm Receiver";
    private NotificationCompat.Builder mBuilder;
    private HashMap<Object, Address> addressHashMap;
    private List<WeatherUpdateItem> weatherUpdateItems;
    private int numberOfSuccesses = 0;
    private Intent notifyIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Alarm received");
        addressesHashMapSetup(context);
        mBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.web_hi_res_512)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentTitle("Daily weather update")
                .setContentText("Daily weather update available");

    }

    private void addressesHashMapSetup(Context context) {
        addressHashMap = new HashMap<>();
        SharedPreferences addressesSharedPreferences = context.getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
        int numberOfAddresses = addressesSharedPreferences.getInt(Constants.NUMBER_OF_ADDRESSES, 0);
        for (int i = 0; i < numberOfAddresses; i++) {
            addressHashMap.put(
                    String.valueOf(i + 1)
                    , PublicMethods.getSavedObjectFromPreference(context, Constants.ADDRESSES_PREFERENCE, String.valueOf(i + 1), Address.class)
            );
        }
    }

    private void handleAddress(final NotificationCompat.Builder mBuilder
            , final int numberOfAddresses
            , final NotificationCompat.InboxStyle inboxStyle
            , final Address address, final Context context) {

        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(String.valueOf(address.getLatitude()));
        request.setLng(String.valueOf(address.getLongitude()));
        request.setUnits(Request.Units.SI);
        request.setLanguage(Request.Language.ENGLISH);
//        request.addExcludeBlock(Request.Block.CURRENTLY);
        Log.d(TAG, "handleAddress: request");
        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                inboxStyle.addLine("Temperature in "
                        + address.getLocality()
                        + ": "
                        + weatherResponse.getCurrently().getTemperature()
                        + Constants.DEGREE);
                weatherUpdateItems.add(new WeatherUpdateItem(String.valueOf(numberOfSuccesses), weatherResponse, address.getLocality()));
                if (++numberOfSuccesses == numberOfAddresses) {
                    sendNotification(mBuilder, inboxStyle, context);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG, "Error while calling: " + retrofitError.getUrl());
            }
        });
    }

    private void sendNotification(NotificationCompat.Builder mBuilder, NotificationCompat.InboxStyle inboxStyle, Context context) {
        mBuilder.setStyle(inboxStyle);
        notifyIntent.putExtra(Constants.WEATHER_UPDATE_ITEMS, (new Gson()).toJson(weatherUpdateItems));
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
    }
}
