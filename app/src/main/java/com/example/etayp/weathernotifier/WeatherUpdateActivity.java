package com.example.etayp.weathernotifier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.etayp.weathernotifier.dummy.WeatherUpdateItems;

public class WeatherUpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_update);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.weather_update_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new WeatherUpdateRecyclerViewAdapter(WeatherUpdateItems.ITEMS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WeatherUpdateItems.clear();
    }
}
