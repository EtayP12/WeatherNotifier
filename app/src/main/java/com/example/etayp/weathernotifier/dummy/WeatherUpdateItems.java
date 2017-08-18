package com.example.etayp.weathernotifier.dummy;

import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by EtayP on 17-Aug-17.
 */

public class WeatherUpdateItems {

    public static final List<WeatherUpdateItems.WeatherUpdateItem> ITEMS = new ArrayList<>();

    public static final Map<String, WeatherUpdateItems.WeatherUpdateItem> ITEM_MAP = new HashMap<>();

    private static void addItem(WeatherUpdateItems.WeatherUpdateItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class WeatherUpdateItem{
        public String id;
        public WeatherResponse weatherResponse;
        public String location;

        public WeatherUpdateItem(String id, WeatherResponse weatherResponse, String location) {
            this.id = id;
            this.weatherResponse = weatherResponse;
            this.location = location;
            WeatherUpdateItems.addItem(this);
        }
    }
}
