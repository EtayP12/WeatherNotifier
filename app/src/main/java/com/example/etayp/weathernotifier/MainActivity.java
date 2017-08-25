//GitHub try

package com.example.etayp.weathernotifier;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.etayp.weathernotifier.dummy.RecyclerItems;
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
import java.util.List;
import java.util.Stack;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity implements
        MainFragment.OnFragmentInteractionListener, NotificationSettingsFragment.OnFragmentInteractionListener
        , LocationsFragment.OnListFragmentInteractionListener {


    private Address mAddress;

    HashMap<String, Address> addressHashMap;

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
    private Thread thread;
    private AlarmManager alarmManager;
    private MainFragment mainFragment;
    private NotificationSettingsFragment notificationSettingsFragment;
    private LocationsFragment locationsFragment;
    private Stack<String> mFragmentStack;
    private boolean backWasPressed;
    private int[] updateTimeMillis;
    private Thread locationUpdateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ForecastApi.create(Constants.API_KEY2);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mFragmentStack = new Stack<>();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            mainFragment = new MainFragment();
            mainFragment.setArguments(getIntent().getExtras());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, mainFragment, mainFragment.getClass().getName());
            transaction.addToBackStack(mainFragment.getClass().getName());
            mFragmentStack.add(mainFragment.getClass().getName());
            transaction.commit();
        }

        notificationSettingsFragment = new NotificationSettingsFragment();
        locationsFragment = new LocationsFragment();

        // for addresses
        mResultReceiver = new AddressResultReceiver(new Handler());
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
                                while (true) {
                                    updateLocation();
                                    try {
//                                        Thread.sleep((long) updateTimeMillis[sharedPreferences.getInt(Constants.UPDATE_TIME_SELECTION, 0)]);
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

    private void updateLocation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            Awareness.SnapshotApi.getLocation(mApiClient).setResultCallback(new ResultCallback<LocationResult>() {
                @Override
                public void onResult(@NonNull LocationResult locationResult) {
                    mLastLocation = locationResult.getLocation();
                    if (mLastLocation != null) {
                        startIntentService();
                        RequestBuilder weather = new RequestBuilder();

                        Request request = new Request();
                        request.setLat(String.valueOf(mLastLocation.getLatitude()));
                        request.setLng(String.valueOf(mLastLocation.getLongitude()));
                        request.setUnits(Request.Units.SI);
                        request.setLanguage(Request.Language.ENGLISH);

                        weather.getWeather(request, new Callback<WeatherResponse>() {
                            @Override
                            public void success(WeatherResponse weatherResponse, Response response) {
                                String temp = String.valueOf(weatherResponse.getCurrently().getTemperature());
                                ((TextView) findViewById(R.id.temperature_value)).setText(
                                        temp.substring(0, temp.indexOf(".") + 2) + Constants.DEGREE
                                );
                                String app_temp = String.valueOf(weatherResponse.getCurrently().getApparentTemperature());
                                ((TextView) findViewById(R.id.apparent_temperature_value)).setText(
                                        app_temp.substring(0, temp.indexOf(".") + 2) + Constants.DEGREE
                                );
                                ((TextView) findViewById(R.id.Humidity_value)).setText(
                                        (int) (Double.valueOf(weatherResponse.getCurrently().getHumidity()) * 100) + Constants.PERCENT
                                );
                                ((TextView) findViewById(R.id.precip_probability_value)).setText(
                                        (int) (Double.valueOf(weatherResponse.getCurrently().getPrecipProbability()) * 100) + Constants.PERCENT
                                );
                                String wind = String.valueOf(Double.valueOf(weatherResponse.getCurrently().getWindSpeed()) * 1.609);
                                ((TextView) findViewById(R.id.wind_speed_value)).setText(
                                        wind.substring(0, wind.indexOf(".") + 2)
                                );
                                PublicMethods.changeIcon(weatherResponse.getCurrently().getIcon(), (ImageView) findViewById(R.id.current_icon));

                                handleForecast(weatherResponse);

                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    } else {
                        Log.d(TAG, "onResult: unable to get location");
                    }
                }
            });
        }
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
                        , (ImageView) forecastItem.getChildAt(1));
                String s = String.valueOf(weatherResponse.getHourly().getData().get(forecastItemsHandled).getTemperature());
                ((TextView) forecastItem.getChildAt(2))
                        .setText(
                                s.substring(0, s.indexOf(".") + 2)
                                        + Constants.DEGREE
                        );
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
        addressHashMap = new HashMap<>();
        SharedPreferences addressesSharedPreferences = getSharedPreferences(Constants.ADDRESSES_PREFERENCE, MODE_PRIVATE);
        int numberOfAddresses = addressesSharedPreferences.getInt(Constants.NUMBER_OF_ADDRESSES, 0);
        for (int i = 0; i < numberOfAddresses; i++) {
            addressHashMap.put(
                    String.valueOf(i + 1)
                    , PublicMethods.getSavedObjectFromPreference(this, Constants.ADDRESSES_PREFERENCE, String.valueOf(i + 1), Address.class)
            );
        }
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
                        mAddress = resultData.getParcelable("currentAddress");
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
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        activityIsActive = false;
        saveAddressesToPreference();
        final long selectedUpdateTime = (long) updateTimeMillis[sharedPreferences.getInt(Constants.UPDATE_TIME_SELECTION, 0)];
        final Intent intent = new Intent(this, NotificationSender.class);
        final Context context = this;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Log.d(TAG, "run: pausing");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!activityIsActive && isBackgroundRunning(context)) {
                    Log.d(TAG, "run: background");
                    intent.putExtra(Constants.ADDRESSES_HASH_MAP, (new Gson()).toJson(addressHashMap));
                    startService(intent);
                    try {
                        Thread.sleep(selectedUpdateTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        if (mFenceReceiver != null) {
            unregisterReceiver(mFenceReceiver);
        }
    }

    public static boolean isBackgroundRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        //If your app is the process in foreground, then it's not in running in background
                        return false;
                    }
                }
            }
        }


        return true;
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
//        registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
        activityIsActive = true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Location resultLocation = data.getExtras().getParcelable("NewLocation");
            if (resultLocation != null) {
                Intent intent = new Intent(this, FetchAddressIntentService.class);
                intent.putExtra(Constants.RECEIVER, mResultReceiver);
                intent.putExtra(Constants.LOCATION_DATA_EXTRA, resultLocation);
                intent.putExtra(Constants.RECEIVE_TYPE_EXTRA, Constants.RECEIVE_TO_FRAGMENT);
                startService(intent);
            }
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

    private void switchFragments(Fragment fragment) {
        String fragName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (!mFragmentStack.contains(fragName)) {
            transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            transaction.hide(fragmentManager.findFragmentByTag(mFragmentStack.peek()));
            transaction.add(R.id.fragment_container, fragment, fragName);
            transaction.addToBackStack(fragName);
            mFragmentStack.add(fragName);
        } else {
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
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(MainFragment.class.getName(), 0);
            mFragmentStack.clear();
            mFragmentStack.add(mainFragment.getClass().getName());
        }
    }

    protected void startIntentService() {
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