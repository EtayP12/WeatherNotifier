package com.example.etayp.weathernotifier;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.etayp.weathernotifier.dummy.WeatherUpdateItems;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by EtayP on 04-Aug-17.
 */

public class NotificationSender extends IntentService {

    private static final String TAG = "NotificationSender";
    private Address currentAddress;
    int numberOfSuccesses = 0;
    private int numberOfAddresses;
    private NotificationCompat.InboxStyle inboxStyle;

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
        /*WeatherResponse weatherResponse =
                (new Gson()).fromJson(
                        intent.getStringExtra(Constants.WEATHER_RESPONSE_DATA)
                        , WeatherResponse.class
                );
        final String key = intent.getStringExtra(Constants.ADDRESS_ID);
        if (Objects.equals(key, "0")) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                currentAddress = geocoder.getFromLocation(weatherResponse.getLatitude(), weatherResponse.getLongitude(), 1).get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
            currentAddress = (new Gson()).fromJson(sharedPreferences.getString(key, ""), Address.class);
        }*/

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
//                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setSmallIcon(R.drawable.web_hi_res_512)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                        .setContentTitle("New weather update")
                        .setContentText("New weather update available");

        Intent notifyIntent =
                new Intent(Intent.makeMainActivity(new ComponentName(this, WeatherUpdateActivity.class)));
//        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(pendingIntent);

        final Context context = this;

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Type stringAddressHashMap = new TypeToken<HashMap<String, Address>>() {
        }.getType();
        HashMap<String, Address> addressHashMap;
        addressHashMap = (new Gson()).fromJson(intent != null ? intent.getStringExtra(Constants.ADDRESSES_HASH_MAP) : null, stringAddressHashMap);
        numberOfAddresses = addressHashMap != null ? addressHashMap.size() + 1 : 1;
        inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Weather update:");
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            try {
                                currentAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                handleAddress(mBuilder, numberOfAddresses, inboxStyle, currentAddress);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        assert addressHashMap != null;
        for (String addressKey : addressHashMap.keySet()) {
            final Address address = addressHashMap.get(addressKey);
            handleAddress(mBuilder, numberOfAddresses, inboxStyle, address);
        }
    }

    private void handleAddress(final NotificationCompat.Builder mBuilder, final int numberOfAddresses, final NotificationCompat.InboxStyle inboxStyle, final Address address) {
        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(String.valueOf(address.getLatitude()));
        request.setLng(String.valueOf(address.getLongitude()));
        request.setUnits(Request.Units.SI);
        request.setLanguage(Request.Language.ENGLISH);
//        request.addExcludeBlock(Request.Block.CURRENTLY);
        Log.d(TAG, "weatherRequestBuilder: request");
        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                inboxStyle.addLine("Temperature in "
                        + address.getLocality()
                        + ": "
                        + weatherResponse.getCurrently().getTemperature()
                        + "℃");
                new WeatherUpdateItems.WeatherUpdateItem(String.valueOf(numberOfSuccesses),weatherResponse,address.getLocality());
                if (++numberOfSuccesses == numberOfAddresses) {
                    mBuilder.setStyle(inboxStyle);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(0, mBuilder.build());
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG, "Error while calling: " + retrofitError.getUrl());
            }
        });
    }
}
