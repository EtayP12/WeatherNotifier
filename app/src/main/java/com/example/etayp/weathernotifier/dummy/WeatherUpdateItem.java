package com.example.etayp.weathernotifier.dummy;

import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

/**
 * Created by EtayP on 17-Aug-17.
 */

public class WeatherUpdateItem {

//    public static final List<WeatherUpdateItem.WeatherUpdateItem> ITEMS = new ArrayList<>();

//    private static void addItem(WeatherUpdateItem.WeatherUpdateItem item) {
//        ITEMS.add(item);
//    }

//    public static void clear() {
//        ITEMS.clear();
//    }

//    public static class WeatherUpdateItem{
        public String id;
        public WeatherResponse weatherResponse;
        public String location;

        public WeatherUpdateItem(String id, WeatherResponse weatherResponse, String location) {
            this.id = id;
            this.weatherResponse = weatherResponse;
            this.location = location;
//            WeatherUpdateItem.addItem(this);
        }
    }
//}
