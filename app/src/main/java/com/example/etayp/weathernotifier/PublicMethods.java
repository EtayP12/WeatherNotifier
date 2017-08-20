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

    public static void changeIcon(com.johnhiott.darkskyandroidlib.models.DataPoint point, ImageView imageToChange) {
        switch (point.getIcon()) {
            case "clear-day":
                imageToChange.setImageResource(R.drawable.clear_day_icon);
                break;
            case "clear-night":
                imageToChange.setImageResource(R.drawable.clear_night_icon);
                break;
            case "rain":
                imageToChange.setImageResource(R.drawable.rainy_day_icon);
                break;
            case "snow":
                imageToChange.setImageResource(R.drawable.snow_icon);
                break;
            case "sleet":
                imageToChange.setImageResource(R.drawable.snow_icon);
                break;
            case "wind":
                imageToChange.setImageResource(R.drawable.wind_icon);
                break;
            case "fog":
                imageToChange.setImageResource(R.drawable.fog_cloud_icon);
                break;
            case "cloudy":
                imageToChange.setImageResource(R.drawable.fog_cloud_icon);
                break;
            case "partly-cloudy-day":
                imageToChange.setImageResource(R.drawable.cloudy_day_icon);
                break;
            case "partly-cloudy-night":
                imageToChange.setImageResource(R.drawable.cloudy_night_icon);
                break;
            default:

                break;
        }
        imageToChange.setVisibility(View.VISIBLE);
    }
}
