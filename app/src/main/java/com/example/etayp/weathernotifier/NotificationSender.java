package com.example.etayp.weathernotifier;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.etayp.weathernotifier.items.WeatherUpdateItem;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by EtayP on 04-Aug-17.
 */

public class NotificationSender extends IntentService {

    List<WeatherUpdateItem> weatherUpdateItems = new ArrayList<>();

    private static final String TAG = "NotificationSender";
    private Address currentAddress;
    int numberOfSuccesses = 0;
    private int numberOfAddresses;
    private NotificationCompat.InboxStyle inboxStyle;
    private Intent notifyIntent;
    private NotificationCompat.Builder mBuilder;
    private boolean userSelectedOptionsApply = false;


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


        mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.icon_misc_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("New weather update")
                .setContentText("New weather update available");

        notifyIntent = new Intent(Intent.makeMainActivity(new ComponentName(this, WeatherUpdateActivity.class)));


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
            Address address = addressHashMap.get(addressKey);
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
        final Context context = this;
        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                WeatherUpdateItem item = new WeatherUpdateItem(String.valueOf(numberOfSuccesses), weatherResponse, address.getLocality());
                weatherUpdateItems.add(item);
                String toNotification = stringToNotification(item);
                if (toNotification != null) {
                    inboxStyle.addLine(toNotification);
                    userSelectedOptionsApply = true;
                }
                if (++numberOfSuccesses == numberOfAddresses) {
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
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                    // Sends a notification only if there is an anomaly in one of the criteria
                    // the user choose in one of the locations the user choose
                    if (userSelectedOptionsApply) {
                        notificationManager.notify(0, mBuilder.build());
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG, "Error while calling: " + retrofitError.getUrl());
            }
        });
    }

    private String stringToNotification(WeatherUpdateItem item) {

        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);
        boolean optionTemprature = sharedPreferences.getBoolean(Constants.OPTION_TEMPRATURE, true);
        boolean optionWind = sharedPreferences.getBoolean(Constants.OPTION_WIND, true);
        boolean optionHumidity = sharedPreferences.getBoolean(Constants.OPTION_HUMIDITY, true);
        boolean optionRain = sharedPreferences.getBoolean(Constants.OPTION_RAIN, true);

        if (optionRain) {
            if (Double.valueOf(item.weatherResponse.getCurrently().getPrecipProbability()) > 0.6) {
                String precipitation = String.valueOf(Double.valueOf(item.weatherResponse.getCurrently().getPrecipProbability()) * 100);
                precipitation = precipitation.substring(0, precipitation.indexOf(".")) + Constants.PERCENT;
                return "Probability for " + item.weatherResponse.getCurrently().getPrecipType() + " in " + item.location + ": " + precipitation;
            }
        }

        if (optionTemprature) {
            if (item.weatherResponse.getCurrently().getTemperature() > 35) {
                String temprature = (int) item.weatherResponse.getCurrently().getTemperature() + Constants.DEGREE;
                return "Temprature in " + item.location + ": " + temprature;
            }
        }

        if (optionHumidity) {
            if (Double.valueOf(item.weatherResponse.getCurrently().getHumidity()) > 0.6) {
                String humidity = (int) (Double.valueOf(item.weatherResponse.getCurrently().getHumidity()) * 100) + Constants.PERCENT;
                return "Humidity in " + item.location + ": " + humidity;
            }
        }

        if (optionWind) {
            if (Double.valueOf(item.weatherResponse.getCurrently().getWindSpeed()) > 35) {
                String wind = String.valueOf((int) (Double.valueOf(item.weatherResponse.getCurrently().getWindSpeed()) * 1.609));
                return "Wind speed in " + item.location + ": " + wind;
            }
        }

        return null;
    }
}
