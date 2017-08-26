package com.example.etayp.weathernotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;

/**
 * Created by EtayP on 08-Aug-17.
 */

public class PublicMethods {
    public static void saveObjectToSharedPreference(Context context, String preferenceFileName, String serializedObjectKey, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    public static <GenericClass> GenericClass getSavedObjectFromPreference(Context context, String preferenceFileName, String preferenceKey, Class<GenericClass> classType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
    }

    public static void changeIcon(String icon, ImageView imageToChange, boolean bigIcon) {
        switch (icon) {
            case "clear-day":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_clear_day);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_clear_day);
                }
                break;
            case "clear-night":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_clear_night);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_clear_night);
                }
                break;
            case "rain":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_rainy_day);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_rainy_day);
                }
                break;
            case "snow":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_snow);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_snow);
                }
                break;
            case "sleet":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_snow);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_snow);
                }
                break;
            case "wind":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_wind);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_wind);
                }
                break;
            case "fog":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_fog_cloud);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_fog_cloud);
                }
                break;
            case "cloudy":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_fog_cloud);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_fog_cloud);
                }
                break;
            case "partly-cloudy-day":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_cloudy_day);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_cloudy_day);
                }
                break;
            case "partly-cloudy-night":
                if (bigIcon) {
                    imageToChange.setImageResource(R.drawable.big_icon_weather_cloudy_night);
                } else {
                    imageToChange.setImageResource(R.drawable.icon_weather_cloudy_night);
                }
                break;
            default:

                break;
        }
        imageToChange.setVisibility(View.VISIBLE);
    }
}
