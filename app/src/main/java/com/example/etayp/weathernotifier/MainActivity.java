//GitHub try

package com.example.etayp.weathernotifier;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.etayp.weathernotifier.dummy.RecyclerItems;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements
        MainFragment.OnFragmentInteractionListener, NotificationSettingsFragment.OnFragmentInteractionListener
        , LocationsFragment.OnListFragmentInteractionListener {

    private String mAddressOutput;
    private Address mAddress;

    HashMap<String, Address> addressHashMap;

    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor sharedPreferencesEditor;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    private FenceReceiver mFenceReceiver;
    private GoogleApiClient mApiClient;

    Weather weather;
    private final String FENCE_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION";

    private final String FENCE_KEY = "fence_key";

    LocationsFragment locationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        addressHashMap = new HashMap<>();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            MainFragment mainFragment = new MainFragment();
            mainFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mainFragment).commit();
        }

        // for addresses
        mResultReceiver = new AddressResultReceiver(new Handler());

        // for data saving and loading
        sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();


        Context context = this;
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(Awareness.API)
                .enableAutoManage(this, 1, null)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        mFenceReceiver = new FenceReceiver();
                        registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        unregisterReceiver(mFenceReceiver);
                    }
                })
                .build();

        Awareness.SnapshotApi.getLocation(mApiClient).setResultCallback(new ResultCallback<com.google.android.gms.awareness.snapshot.LocationResult>() {
            @Override
            public void onResult(@NonNull com.google.android.gms.awareness.snapshot.LocationResult locationResult) {
                mLastLocation = locationResult.getLocation();
                startIntentService();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getWeather(mApiClient)
                    .setResultCallback(new ResultCallback<WeatherResult>() {
                        @Override
                        public void onResult(@NonNull WeatherResult weatherResult) {
                            if (!weatherResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get weather.");
                                return;
                            }
                            weather = weatherResult.getWeather();
                            weather.getConditions();
                            updateWeather();
                        }
                    });
        }

    }

    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            if (resultCode == Constants.SUCCESS_RESULT) {
                switch (resultData.getInt(Constants.RECEIVE_TYPE_EXTRA)) {
                    case Constants.RECEIVE_TO_MAIN:
                        mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
                        displayAddressOutput();
                        mAddress = resultData.getParcelable("address");
                        break;
                    case Constants.RECEIVE_TO_FRAGMENT:
                        Address address = resultData.getParcelable("address");
                        addressHashMap.put(String.valueOf(RecyclerItems.ITEMS.size() + 1), address);
                        new RecyclerItems.RecyclerItem(
                                String.valueOf(RecyclerItems.ITEMS.size() + 1),
                                address.getAddressLine(address.getMaxAddressLineIndex() - 1)
                        );
                        locationFragment.getRecyclerViewAdapter().notifyItemInserted(RecyclerItems.ITEMS.size());
                        break;
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        final Intent intent = new Intent(this, NotificationSender.class);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    startService(intent);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        unregisterReceiver(mFenceReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
    }

    private void updateWeather() {
        ((TextView) findViewById(R.id.temperature_value)).setText("" + weather.getTemperature(2));
        ((TextView) findViewById(R.id.Humidity_value)).setText("" + weather.getHumidity());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Location resultLocation = data.getExtras().getParcelable("NewLocation");
            Intent intent = new Intent(this, FetchAddressIntentService.class);
            intent.putExtra(Constants.RECEIVER, mResultReceiver);
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, resultLocation);
            intent.putExtra(Constants.ADDRESS_TYPE_EXTRA, Constants.WHOLE_ADDRESS);
            intent.putExtra(Constants.RECEIVE_TYPE_EXTRA, Constants.RECEIVE_TO_FRAGMENT);
            startService(intent);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.set_update_time) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Set update time")
                    .setCancelable(false).create();
            alertDialog.setView(getLayoutInflater().inflate(R.layout.alert_dialog_layout, null));
            alertDialog.show();
            final Spinner updateTimeSpinner = ((Spinner) alertDialog.findViewById(R.id.spinner));
            updateTimeSpinner
                    .setAdapter(ArrayAdapter.createFromResource(
                            this, R.array.update_times, android.R.layout.simple_spinner_dropdown_item)
                    );
            updateTimeSpinner
                    .setSelection(sharedPreferences.getInt("update time", 0));
            alertDialog.findViewById(R.id.set_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPreferencesEditor.putInt("update time"
                            , updateTimeSpinner.getSelectedItemPosition())
                            .commit();
                    alertDialog.dismiss();
                }
            });
            return true;
        }
        if (id == R.id.notification_usage) {
            NotificationSettingsFragment fragment = new NotificationSettingsFragment();
            changeFragment(fragment, true, true);
            return true;
        }
        if (id == R.id.define_location) {
            locationFragment = new LocationsFragment();
            changeFragment(locationFragment, true, true);
            return true;
        }


        return super.onOptionsItemSelected(item);
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

    private void changeFragment(Fragment frag, boolean saveInBackstack, boolean animate) {
        String backStateName = ((Object) frag).getClass().getName();

        try {
            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
                //fragment not in back stack, create it.
                FragmentTransaction transaction = manager.beginTransaction();

                if (animate) {
                    Log.d(TAG, "Change Fragment: animate");
                    transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }

                transaction.replace(R.id.fragment_container, frag, backStateName);

                if (saveInBackstack) {
                    Log.d(TAG, "Change Fragment: addToBackTack " + backStateName);
                    transaction.addToBackStack(backStateName);
                } else {
                    Log.d(TAG, "Change Fragment: NO addToBackTack");
                }

                transaction.commit();
            } else {
                // custom effect if fragment is already instanciated
            }
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Unable to commit fragment, could be activity as been killed in background. " + exception.toString());
        }
    }

    private void displayAddressOutput() {
        ((TextView) findViewById(R.id.location_value)).setText(mAddressOutput);
        ((TextView) findViewById(R.id.location_value)).setTextColor(Color.GREEN);
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        intent.putExtra(Constants.ADDRESS_TYPE_EXTRA, Constants.WHOLE_ADDRESS);
        startService(intent);
    }

    @Override
    public void onListFragmentInteraction(RecyclerItems.RecyclerItem item) {
        RecyclerItems.ITEMS.remove(item);
        RecyclerItems.ITEM_MAP.remove(item.id);
        addressHashMap.remove(item.id);
        int itemRemoved = Integer.parseInt(item.id);
        for (int i = itemRemoved; i <= RecyclerItems.ITEM_MAP.size(); i++) {
            RecyclerItems.ITEM_MAP.put(String.valueOf(i),RecyclerItems.ITEM_MAP.remove(String.valueOf(i+1)));
            addressHashMap.put(String.valueOf(i), addressHashMap.remove(String.valueOf(i+1)));
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