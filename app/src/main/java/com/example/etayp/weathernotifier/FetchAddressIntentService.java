package com.example.etayp.weathernotifier;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by EtayP on 30-Jul-17.
 */

public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "IntentService";
    protected ResultReceiver mReceiver;

    public FetchAddressIntentService(String name) {
        super(name);
    }

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        int addressType = intent.getIntExtra(Constants.ADDRESS_TYPE_EXTRA, 0);
        int receiveType = intent.getIntExtra(Constants.RECEIVE_TYPE_EXTRA, 0);

        // ...

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single currentAddress.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }
        // Handle case where no currentAddress was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage, null, receiveType);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the currentAddress lines using getAddressLine,
            // join them, and send them to the thread.
            switch (addressType) {
                case Constants.WHOLE_ADDRESS:
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }
                    break;
                case Constants.CITY_ONLY:
                    addressFragments.add(address.getLocality());
                    break;
            }

            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments), address, receiveType);
        }
    }

    private void deliverResultToReceiver(int resultCode, String message, Address address, int receiveType) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("currentAddress", address);
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        bundle.putInt(Constants.RECEIVE_TYPE_EXTRA, receiveType);
        mReceiver.send(resultCode, bundle);
    }
}
