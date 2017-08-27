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
 * Created by EtayP on 27-Aug-17.
 */

class DailyUpdateRecyclerViewAdapter extends RecyclerView.Adapter<DailyUpdateRecyclerViewAdapter.ViewHolder> {

    private List<WeatherUpdateItem> values;


    DailyUpdateRecyclerViewAdapter(List<WeatherUpdateItem> values) {
        this.values = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mLocation.setText(values.get(position).location);
        holder.mDescription.setText(values.get(position).weatherResponse.getHourly().getSummary());
        PublicMethods.changeIcon(
                values.get(position).weatherResponse.getHourly().getIcon()
                , holder.mIcon
                , false
        );
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final View mView;
        final TextView mLocation;
        final TextView mDescription;
        final ImageView mIcon;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLocation = (TextView) itemView.findViewById(R.id.daily_weather_location);
            mDescription = (TextView) itemView.findViewById(R.id.daily_weather_description);
            mIcon = (ImageView) itemView.findViewById(R.id.daily_weather_icon);
        }
    }
}
