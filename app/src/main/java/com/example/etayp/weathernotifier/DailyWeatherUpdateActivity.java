package com.example.etayp.weathernotifier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.etayp.weathernotifier.items.WeatherUpdateItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class DailyWeatherUpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_weather_update);

        Type type = new TypeToken<List<WeatherUpdateItem>>() {
        }.getType();

        List<WeatherUpdateItem> WeatherUpdateItems =
                (new Gson()).fromJson(getIntent().getExtras().getString(Constants.DAILY_WEATHER_ITEMS), type);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.daily_weather_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DailyUpdateRecyclerViewAdapter(WeatherUpdateItems));
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
