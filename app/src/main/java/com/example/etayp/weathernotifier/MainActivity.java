//GitHub try

package com.example.etayp.weathernotifier;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.etayp.weathernotifier.items.RecyclerItems;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.gson.Gson;
import com.johnhiott.darkskyandroidlib.ForecastApi;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Stack;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity implements
        MainFragment.OnFragmentInteractionListener, NotificationSettingsFragment.OnFragmentInteractionListener
        , LocationsFragment.OnListFragmentInteractionListener, SplashFragment.OnFragmentTimeOutListener {


    private Address mAddress;

    HashMap<String, Address> addressHashMap = new HashMap<>();

    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;

    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    private FenceReceiver mFenceReceiver;
    private GoogleApiClient mApiClient;


    private final String FENCE_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION";

    private final String FENCE_KEY = "fence_key";

    private boolean activityIsActive = true;
    private AlarmManager alarmManager;
    private MainFragment mainFragment;
    private NotificationSettingsFragment notificationSettingsFragment;
    private LocationsFragment locationsFragment;
    private Stack<String> mFragmentStack;
    private boolean backWasPressed;
    private int[] updateTimeMillis;
    private Thread locationUpdateThread;
    private boolean firstUpdate = true;
    private SplashFragment splashFragment;
    private boolean connectionOngoing = true;
    private boolean removeSplashOnResume;
    private AlertDialog timeOutAlertDialog;
    private boolean notificationThreadNotActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ForecastApi.create(Constants.API_KEY2);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mFragmentStack = new Stack<>();

        mainFragment = new MainFragment();
        splashFragment = new SplashFragment();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            setupMainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, splashFragment, SplashFragment.class.getName());
            transaction.commit();
        }

        notificationSettingsFragment = new NotificationSettingsFragment();
        locationsFragment = new LocationsFragment();

        mResultReceiver = new AddressResultReceiver(new Handler());

        // for data saving and loading
        sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);

        addressesHashMapSetup();
        recyclerViewSetup();

        updateTimeMillis = getResources().getIntArray(R.array.update_times_millis);
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .enableAutoManage(this, 1, null)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        mFenceReceiver = new FenceReceiver();
                        registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
                        locationUpdateThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (connectionOngoing) {
                                    updateLocation();
                                    try {
                                        Thread.sleep(15000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        });
                        locationUpdateThread.start();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        unregisterReceiver(mFenceReceiver);
                    }
                })
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                } else {

                    Toast.makeText(this, "Application needs permission", Toast.LENGTH_LONG).show();
                    finish();

                }
            }
        }
    }

    private void updateLocation() {
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Awareness.SnapshotApi.getLocation(mApiClient).setResultCallback(new ResultCallback<LocationResult>() {
                @Override
                public void onResult(@NonNull LocationResult locationResult) {
                    mLastLocation = locationResult.getLocation();
                    if (mLastLocation != null && activityIsActive) {
                        startFetchAddressIntentService();
                    } else {
                        Log.d(TAG, "onResult: unable to get location");
                    }
                }
            });
        }
    }

    private void FetchWeatherResponse() {
        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(String.valueOf(mLastLocation.getLatitude()));
        request.setLng(String.valueOf(mLastLocation.getLongitude()));
        request.setUnits(Request.Units.SI);
        request.setLanguage(Request.Language.ENGLISH);

        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                Log.d(TAG, "success: received weather response");
                String temp = String.valueOf(weatherResponse.getCurrently().getTemperature());
                temp = temp.substring(0, temp.indexOf(".") + 2) + Constants.DEGREE;
                ((TextView) findViewById(R.id.temperature_value)).setText(temp);

                String app_temp = String.valueOf(weatherResponse.getCurrently().getApparentTemperature());
                app_temp = app_temp.substring(0, temp.indexOf(".") + 2) + Constants.DEGREE;
                ((TextView) findViewById(R.id.apparent_temperature_value)).setText(app_temp);

                String humidity = (int) (Double.valueOf(weatherResponse.getCurrently().getHumidity()) * 100) + Constants.PERCENT;
                ((TextView) findViewById(R.id.Humidity_value)).setText(humidity);
                ((TextView) findViewById(R.id.precip_probability_value)).setText(humidity);

                String wind = String.valueOf(Double.valueOf(weatherResponse.getCurrently().getWindSpeed()) * 1.609);
                wind = wind.substring(0, wind.indexOf(".") + 2);
                ((TextView) findViewById(R.id.wind_speed_value)).setText(wind);

                PublicMethods.changeIcon(weatherResponse.getCurrently().getIcon(), (ImageView) findViewById(R.id.current_icon), true);

                handleForecast(weatherResponse);

                //removes splash screen
                if (firstUpdate) {
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (activityIsActive) {
                                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                                setSupportActionBar(toolbar);
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                                            transaction.setCustomAnimations(R.anim.grow_from_middle, R.anim.shrink_to_middle);
                                transaction.replace(R.id.fragment_container, mainFragment, mainFragment.getClass().getName());
                                transaction.commit();
                                findViewById(R.id.app_bar).setVisibility(View.VISIBLE);
                                if (timeOutAlertDialog != null && timeOutAlertDialog.isShowing())
                                    timeOutAlertDialog.dismiss();
                            } else {
                                removeSplashOnResume = true;
                            }
                            splashFragment.cancelTimeOutTimer();
                            firstUpdate = false;
                        }
                    }, 1000);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure: ");
//                                Toast.makeText(context, "Can't connect to dark-sky", Toast.LENGTH_LONG).show();
//                                finish();
            }
        });
    }

    private void setupMainFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, mainFragment, mainFragment.getClass().getName());
        transaction.addToBackStack(mainFragment.getClass().getName());
        mFragmentStack.add(mainFragment.getClass().getName());
        transaction.commit();
    }

    private void handleForecast(WeatherResponse weatherResponse) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, 0);

        int forecastItemsHandled = 0;
        LinearLayout forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        for (int i = 0; i < forecastLayout.getChildCount(); i++) {
            if (forecastLayout.getChildAt(i) instanceof LinearLayout) {
                LinearLayout forecastItem = (LinearLayout) forecastLayout.getChildAt(i);
                forecastItemsHandled++;
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);

                ((TextView) forecastItem.getChildAt(0))
                        .setText(DateUtils.formatDateTime(this, calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME)
                        );

                PublicMethods.changeIcon(weatherResponse.getHourly().getData().get(forecastItemsHandled).getIcon()
                        , (ImageView) forecastItem.getChildAt(1), false);

                String s = String.valueOf(weatherResponse.getHourly().getData().get(forecastItemsHandled).getTemperature());
                s = s.substring(0, s.indexOf(".") + 2) + Constants.DEGREE;
                ((TextView) forecastItem.getChildAt(2)).setText(s);
            }
        }
    }


    private void recyclerViewSetup() {
        for (int i = 1; i < addressHashMap.size() + 1; i++) {
            String key = String.valueOf(i);
            new RecyclerItems.RecyclerItem(key, addressHashMap.get(key).getLocality());
        }
    }

    private void addressesHashMapSetup() {
        SharedPreferences addressesSharedPreferences = getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
        int numberOfAddresses = addressesSharedPreferences.getInt(Constants.NUMBER_OF_ADDRESSES, 0);
        for (int i = 0; i < numberOfAddresses; i++) {
            addressHashMap.put(
                    String.valueOf(i + 1)
                    , PublicMethods.getSavedObjectFromPreference(this, Constants.ADDRESSES_PREFERENCE, String.valueOf(i + 1), Address.class)
            );
        }
    }

    @Override
    public void onFragmentTimeOut() {
        final Context context = this;
        connectionOngoing = false;
        runOnUiThread(new Runnable() {
            public void run() {
                timeOutAlertDialog = new AlertDialog.Builder(context).create();
                timeOutAlertDialog.setTitle("Connection time out");
                timeOutAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        splashFragment.startTimeOutTimer(context);
                        connectionOngoing = true;
                        locationUpdateThread.start();
                    }
                });
                timeOutAlertDialog.setCancelable(false);
                timeOutAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                timeOutAlertDialog.show();
                splashFragment.stopLoadingAnimation();
            }
        });
    }

    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the currentAddress string
            // or an error message sent from the intent service.
            if (resultCode == Constants.SUCCESS_RESULT) {
                switch (resultData.getInt(Constants.RECEIVE_TYPE_EXTRA)) {
                    case Constants.RECEIVE_TO_MAIN:
                        Address newAddress = resultData.getParcelable("currentAddress");
                        if (firstUpdate) {
                            mAddress = newAddress;
                            FetchWeatherResponse();
                        } else {
                            if ((newAddress.getLocality() != null && !newAddress.getLocality().matches(mAddress.getLocality()))) {
                                FetchWeatherResponse();
                            }
                            mAddress = newAddress;
                        }
                        displayAddressOutput(mAddress.getLocality() != null ? mAddress.getLocality() : "Current location");
                        break;
                    case Constants.RECEIVE_TO_FRAGMENT:
                        Address address = resultData.getParcelable("currentAddress");
                        addressHashMap.put(String.valueOf(RecyclerItems.ITEMS.size() + 1), address);
                        new RecyclerItems.RecyclerItem(
                                String.valueOf(RecyclerItems.ITEMS.size() + 1),
                                address.getLocality()
                        );
                        locationsFragment.getRecyclerViewAdapter().notifyItemInserted(RecyclerItems.ITEMS.size());
                        break;
                }
            } else {
                if (resultData.getInt(Constants.RECEIVE_TYPE_EXTRA) == Constants.RECEIVE_TO_FRAGMENT) {
                    Toast.makeText(getApplicationContext(), "No address found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (notificationThreadNotActive) notificationThreadSetup();
        saveAddressesToPreference();
        activityIsActive = false;
        if (mFenceReceiver != null) unregisterReceiver(mFenceReceiver);
    }

    private void notificationThreadSetup() {
        notificationThreadNotActive = false;
        final Intent intent = new Intent(this, NotificationSender.class);
        final Context context = this;
        Thread notificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    notificationThreadNotActive = true;
                    return;
                }
                Log.d(TAG, "run: NotificationThread is running");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (true) {
                    Log.d(TAG, "run: starting NotificationSender service");
                    intent.putExtra(Constants.ADDRESSES_HASH_MAP, (new Gson()).toJson(addressHashMap));
                    startService(intent);
                    try {
                        Thread.sleep(updateTimeMillis[sharedPreferences.getInt(Constants.UPDATE_TIME_SELECTION, 0)]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        notificationThread.start();
    }

    private void saveAddressesToPreference() {
        int numberOfAddresses = addressHashMap.size();
        SharedPreferences addressesSharedPreferences = getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = addressesSharedPreferences.edit();
        editor.clear().commit();
        for (int i = 0; i < numberOfAddresses; i++) {
            PublicMethods.saveObjectToSharedPreference(
                    this
                    , Constants.ADDRESSES_PREFERENCE
                    , String.valueOf(i + 1)
                    , addressHashMap.get(String.valueOf(i + 1))
            );
        }
        editor.putInt(Constants.NUMBER_OF_ADDRESSES, numberOfAddresses).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (removeSplashOnResume) {
            removeSplashOnResume = false;
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, mainFragment, mainFragment.getClass().getName());
            transaction.commit();
            findViewById(R.id.app_bar).setVisibility(View.VISIBLE);
        }
        activityIsActive = true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Location resultLocation = data.getExtras().getParcelable(Constants.NEW_LOCATION);
            if (resultLocation != null) {
                Intent intent = new Intent(this, FetchAddressIntentService.class);
                intent.putExtra(Constants.RECEIVER, mResultReceiver);
                intent.putExtra(Constants.LOCATION_DATA_EXTRA, resultLocation);
                intent.putExtra(Constants.RECEIVE_TYPE_EXTRA, Constants.RECEIVE_TO_FRAGMENT);
                startService(intent);
            } else {
                Toast.makeText(this, Constants.NO_LOCATION_SELECTED, Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == Constants.FAILURE_RESULT) {
            Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.settings) {
            switchFragments(notificationSettingsFragment);
            return true;
        }
        if (id == R.id.define_location) {
            switchFragments(locationsFragment);
            return true;
        }
        if (id == R.id.set_alarm_time) {
            makeAlarmDialogBox();
            return true;
        }
        if (id == R.id.debug) {
            Intent intent = new Intent(this, alarmReceiver.class);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis(), alarmPendingIntent);
        }


        return super.onOptionsItemSelected(item);
    }

    private void makeAlarmDialogBox() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Constants.SET_ALARM_TIME)
                .setCancelable(false).create();
        alertDialog.setView(getLayoutInflater().inflate(R.layout.alarm_alert_dialog_layout, null));
        alertDialog.setCustomTitle(getLayoutInflater().inflate(R.layout.alarm_alert_dialog_title, null));
        alertDialog.show();

        ((TimePicker) alertDialog.findViewById(R.id.timePicker))
                .setCurrentHour(sharedPreferences.getInt(Constants.ALARM_HOUR, 0));
        ((TimePicker) alertDialog.findViewById(R.id.timePicker))
                .setCurrentMinute(sharedPreferences.getInt(Constants.ALARM_MINUTE, 0));
        ((TimePicker) alertDialog.findViewById(R.id.timePicker))
                .setIs24HourView(DateFormat.is24HourFormat(this));
        final Switch alarmSwitch = (Switch) alertDialog.findViewById(R.id.alarmSwitch);
        alarmSwitch.setChecked(sharedPreferences.getBoolean(Constants.ALARM_IS_ACTIVE, false));

        alertDialog.findViewById(R.id.timePicker).setEnabled(alarmSwitch.isChecked());
        alertDialog.findViewById(R.id.alarmSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.findViewById(R.id.timePicker).setEnabled(alarmSwitch.isChecked());
            }
        });
        Intent intent = new Intent(this, alarmReceiver.class);
        final PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alertDialog.findViewById(R.id.set_alarm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (alarmSwitch.isChecked()) {
                    alarmManager.cancel(alarmPendingIntent);

                    editor.putInt(Constants.ALARM_HOUR, ((TimePicker) alertDialog.findViewById(R.id.timePicker)).getCurrentHour());
                    editor.putInt(Constants.ALARM_MINUTE, ((TimePicker) alertDialog.findViewById(R.id.timePicker)).getCurrentMinute());
                    editor.putBoolean(Constants.ALARM_IS_ACTIVE, true);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, ((TimePicker) alertDialog.findViewById(R.id.timePicker)).getCurrentHour());
                    calendar.set(Calendar.MINUTE, ((TimePicker) alertDialog.findViewById(R.id.timePicker)).getCurrentMinute());
                    calendar.set(Calendar.SECOND, 0);

                    if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }

                    alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmPendingIntent);

//                        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),3000,alarmPendingIntent );
                } else {
                    editor.putBoolean(Constants.ALARM_IS_ACTIVE, false);
                    alarmManager.cancel(alarmPendingIntent);
                }
                editor.apply();
                alertDialog.dismiss();
            }
        });
    }

    private void switchFragments(final Fragment fragment) {
        final String fragName = fragment.getClass().getName();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (mFragmentStack.peek().matches(NotificationSettingsFragment.class.getName())
                && !sharedPreferences.getBoolean(Constants.OPTION_HUMIDITY, true)
                && !sharedPreferences.getBoolean(Constants.OPTION_RAIN, true)
                && !sharedPreferences.getBoolean(Constants.OPTION_WIND, true)
                && !sharedPreferences.getBoolean(Constants.OPTION_TEMPRATURE, true)
                ) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(Constants.NO_OPTION_SELECTED_TITLE);
            alertDialog.setMessage(Constants.NO_OPTION_SELECTED_TEXT);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, Constants.GOT_IT, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fragmentTransactionMaker(fragment, fragName, fragmentManager, transaction);
                    dialog.dismiss();
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, Constants.GO_BACK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        } else {
            fragmentTransactionMaker(fragment, fragName, fragmentManager, transaction);
        }
    }

    private void fragmentTransactionMaker(Fragment fragment, String fragName, FragmentManager fragmentManager, FragmentTransaction transaction) {
        if (!mFragmentStack.contains(fragName))

        {
            transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            transaction.hide(fragmentManager.findFragmentByTag(mFragmentStack.peek()));
            transaction.add(R.id.fragment_container, fragment, fragName);
            transaction.addToBackStack(fragName);
            mFragmentStack.add(fragName);
        } else

        {
            if (!mFragmentStack.peek().equals(fragName)) {
                transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                transaction.addToBackStack(fragName);
            }
            transaction.hide(fragmentManager.findFragmentByTag(mFragmentStack.peek()));
            mFragmentStack.add(mFragmentStack.remove(mFragmentStack.indexOf(fragName)));
            transaction.show(fragment);
        }
        transaction.commit();
    }


    public Location getLastKnownLocation() {
        return mLastLocation;
    }

    public Address getLastKnownAddress() {
        return mAddress;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    private void displayAddressOutput(String locality) {
        ((TextView) findViewById(R.id.location_value)).setText(locality);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(mFragmentStack.peek());
        if (fragment instanceof MainFragment) {
            if (!backWasPressed) {
                Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
                backWasPressed = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        backWasPressed = false;
                    }
                }).start();

            } else {
                finish();
            }
        } else {
            if (fragment instanceof NotificationSettingsFragment) {
                if (!sharedPreferences.getBoolean(Constants.OPTION_HUMIDITY, true)
                        && !sharedPreferences.getBoolean(Constants.OPTION_RAIN, true)
                        && !sharedPreferences.getBoolean(Constants.OPTION_WIND, true)
                        && !sharedPreferences.getBoolean(Constants.OPTION_TEMPRATURE, true)
                        ) {
                    final android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
                    alertDialog.setTitle("No option selected");
                    alertDialog.setMessage("If you don't select any option, Weather Notifier wont sent you notifications");
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Got it", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.popBackStack(MainFragment.class.getName(), 0);
                            mFragmentStack.clear();
                            mFragmentStack.add(mainFragment.getClass().getName());
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Go back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                } else {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.popBackStack(MainFragment.class.getName(), 0);
                    mFragmentStack.clear();
                    mFragmentStack.add(mainFragment.getClass().getName());
                }
            } else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack(MainFragment.class.getName(), 0);
                mFragmentStack.clear();
                mFragmentStack.add(mainFragment.getClass().getName());
            }
        }
    }

    protected void startFetchAddressIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @Override
    public void onListFragmentInteraction(RecyclerItems.RecyclerItem item) {
        RecyclerItems.ITEMS.remove(item);
        RecyclerItems.ITEM_MAP.remove(item.id);
        addressHashMap.remove(item.id);
        int itemRemoved = Integer.parseInt(item.id);
        for (int i = itemRemoved; i <= RecyclerItems.ITEM_MAP.size(); i++) {
            RecyclerItems.ITEM_MAP.put(String.valueOf(i), RecyclerItems.ITEM_MAP.remove(String.valueOf(i + 1)));
            addressHashMap.put(String.valueOf(i), addressHashMap.remove(String.valueOf(i + 1)));
        }
    }

    private class FenceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "true";
                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "false";
                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "unknown";
                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
            }
        }
    }
}