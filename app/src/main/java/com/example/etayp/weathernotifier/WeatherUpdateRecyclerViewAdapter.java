package com.example.etayp.weathernotifier;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.etayp.weathernotifier.dummy.WeatherUpdateItem;

import java.util.List;

/**
 * Created by EtayP on 17-Aug-17.
 */

class WeatherUpdateRecyclerViewAdapter extends RecyclerView.Adapter<WeatherUpdateRecyclerViewAdapter.ViewHolder> {

    private final List<WeatherUpdateItem> values;

    WeatherUpdateRecyclerViewAdapter(List<WeatherUpdateItem> items) {
        values = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_update, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mApparent.setText(String.valueOf(values.get(position).weatherResponse.getCurrently().getApparentTemperature()));
        holder.mTemprature.setText(String.valueOf(values.get(position).weatherResponse.getCurrently().getTemperature()));
        String humidity =Double.valueOf(values.get(position).weatherResponse.getCurrently().getHumidity())*100+Constants.PERCENT;
        holder.mHumidity.setText(humidity);
        holder.mLocation.setText(values.get(position).location);
        String wind = String.valueOf(Double.valueOf(values.get(position).weatherResponse.getCurrently().getWindSpeed()) * 1.609);
        holder.mWindSpeed.setText(wind.substring(0, wind.indexOf(".") + 2));
        PublicMethods.changeIcon(values.get(position).weatherResponse.getCurrently().getIcon(), holder.mIcon, false);
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
        final TextView mWindSpeed;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mApparent = (TextView) itemView.findViewById(R.id.update_apparent_value);
            mTemprature = (TextView) itemView.findViewById(R.id.update_real_temprature_value);
            mHumidity = (TextView) itemView.findViewById(R.id.update_humidity_value);
            mIcon = (ImageView) itemView.findViewById(R.id.update_weather_icon);
            mLocation = (TextView) itemView.findViewById(R.id.update_location_text);
            mWindSpeed = (TextView) itemView.findViewById(R.id.update_wind_value);
        }
    }
}
