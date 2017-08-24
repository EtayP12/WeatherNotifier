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

    public static void changeIcon(String icon, ImageView imageToChange) {
        switch (icon) {
            case "clear-day":
                imageToChange.setImageResource(R.drawable.icon_weather_clear_day);
                break;
            case "clear-night":
                imageToChange.setImageResource(R.drawable.icon_weather_clear_night);
                break;
            case "rain":
                imageToChange.setImageResource(R.drawable.icon_weather_rainy_day);
                break;
            case "snow":
                imageToChange.setImageResource(R.drawable.icon_weather_snow);
                break;
            case "sleet":
                imageToChange.setImageResource(R.drawable.icon_weather_snow);
                break;
            case "wind":
                imageToChange.setImageResource(R.drawable.icon_weather_wind);
                break;
            case "fog":
                imageToChange.setImageResource(R.drawable.icon_weather_fog_cloud);
                break;
            case "cloudy":
                imageToChange.setImageResource(R.drawable.icon_weather_fog_cloud);
                break;
            case "partly-cloudy-day":
                imageToChange.setImageResource(R.drawable.icon_weather_cloudy_day);
                break;
            case "partly-cloudy-night":
                imageToChange.setImageResource(R.drawable.icon_weather_cloudy_night);
                break;
            default:

                break;
        }
        imageToChange.setVisibility(View.VISIBLE);
    }
}
