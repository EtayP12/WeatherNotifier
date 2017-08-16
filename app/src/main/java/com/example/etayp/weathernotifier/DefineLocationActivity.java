package com.example.etayp.weathernotifier;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DefineLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location location;
    private Location newLocation;
    private Address currentAddress;
    private Marker marker;
    private Address searchAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        location = getIntent().getParcelableExtra("Location");
        currentAddress = getIntent().getParcelableExtra("Address");
        findViewById(R.id.add_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("NewLocation", newLocation);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        final Context context = this;
        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSearch = ((EditText) findViewById(R.id.search_box)).getText().toString();
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(toSearch, 1);
                    if (!addresses.isEmpty()) {
                        searchAddress = addresses.get(0);
                        LatLng latLng = new LatLng(searchAddress.getLatitude(), searchAddress.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        if (marker != null) marker.remove();
                        marker = mMap.addMarker(new MarkerOptions().position(latLng)
                                .title(searchAddress.getLocality()));
                        newLocation = new Location("");
                        newLocation.setLatitude(searchAddress.getLatitude());
                        newLocation.setLongitude(searchAddress.getLongitude());
                    } else {
                        Toast toast = new Toast(context);
                        toast.makeText(context, "No address found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        if (location != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            String city = currentAddress.getLocality();
            mMap.addMarker(new MarkerOptions().position(currentLocation).title(city));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (marker != null) marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title("New location"));

                newLocation = new Location("");
                newLocation.setLatitude(latLng.latitude);
                newLocation.setLongitude(latLng.longitude);

                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }

}
