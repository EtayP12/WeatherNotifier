package com.example.etayp.weathernotifier;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.etayp.weathernotifier.dummy.WeatherUpdateItems;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EtayP on 17-Aug-17.
 */

public class WeatherUpdateAdapter extends RecyclerView.Adapter<WeatherUpdateAdapter.ViewHolder> {

    private final List<WeatherUpdateItems.WeatherUpdateItem> values;
    private ArrayList<ViewHolder> viewHolders = new ArrayList<>();

    public WeatherUpdateAdapter(List<WeatherUpdateItems.WeatherUpdateItem> items) {
        values = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_update_item, parent, false);
        WeatherUpdateAdapter.ViewHolder viewHolder = new WeatherUpdateAdapter.ViewHolder(view);
        viewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mApparent.setText(String.valueOf(values.get(position).weatherResponse.getCurrently().getApparentTemperature()));
        holder.mTemprature.setText(String.valueOf(values.get(position).weatherResponse.getCurrently().getTemperature()));
        holder.mHumidity.setText(String.valueOf(values.get(position).weatherResponse.getCurrently().getHumidity()));
        holder.mLocation.setText(values.get(position).location);
        switch (values.get(position).weatherResponse.getCurrently().getIcon()){
            case "clear-day":

                break;
            case "clear-night":

                break;
            case "rain":

                break;
            case "snow":

                break;
            case "sleet":

                break;
            case "wind":

                break;
            case "fog":

                break;
            case "cloudy":

                break;
            case "partly-cloudy-day":

                break;
            case "partly-cloudy-night":

                break;
            default:

                break;
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final View mView;
        final TextView mApparent;
        final TextView mTemprature;
        final TextView mHumidity;
        final ImageView mIcon;
        final TextView mLocation;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mApparent= (TextView) itemView.findViewById(R.id.update_apparent_value);
            mTemprature= (TextView) itemView.findViewById(R.id.update_real_temprature_value);
            mHumidity= (TextView) itemView.findViewById(R.id.update_humidity_value);
            mIcon = (ImageView) itemView.findViewById(R.id.update_weather_icon);
            mLocation = (TextView) itemView.findViewById(R.id.update_location_text);
        }
    }
}
