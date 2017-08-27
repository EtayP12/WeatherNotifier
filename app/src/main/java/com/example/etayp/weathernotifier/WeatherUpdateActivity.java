package com.example.etayp.weathernotifier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.etayp.weathernotifier.dummy.WeatherUpdateItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
public class WeatherUpdateActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Type type = new TypeToken<List<WeatherUpdateItem>>() {
        }.getType();
        List<WeatherUpdateItem> WeatherUpdateItems =
                (new Gson()).fromJson(getIntent().getExtras().getString(Constants.WEATHER_UPDATE_ITEMS), type);
        setContentView(R.layout.activity_weather_update);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.weather_update_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new WeatherUpdateRecyclerViewAdapter(WeatherUpdateItems));
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}
